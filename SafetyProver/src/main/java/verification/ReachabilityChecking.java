package verification;

import common.DOTPrinter;
import common.Timer;
import common.Tuple;
import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import elimination.CEElimination;
import elimination.TransitivityPairSet;
import encoding.*;
import learning.NoInvariantException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.FileNotFoundException;
import java.util.*;

public class ReachabilityChecking {

    private static final Logger LOGGER = LogManager.getLogger();
    private Map<Integer, String> indexToLabel = new HashMap<>();
    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    private final boolean closeUnderTransitions;
    private final boolean checkInitSubset;
    private final boolean lexicographicOrder;

    private int transducerNumStates;
    private int automataNumStates;
    private int numLetters;

    private ISatSolver solver;

    private Automata I;
    private Automata B;
    private EdgeWeightedDigraph T;

    private OldCounterExamples oldCounterExamples;

    private AutomataEncoding automataBEncoding = null;
    private TransducerEncoding transducerEncoding = null;
    private TransitivityPairSet transitivitySet = null;

    private final CEElimination ceElimination;

    private int round = 0;

    private boolean stopped = false;

    public void stopChecking() {
        stopped = true;
    }

    public ReachabilityChecking(boolean lexOrder,
                                boolean closeUnderTransitions,
                                boolean checkInitSubset,
                                ISatSolverFactory solverFactory) {
        solver = solverFactory.spawnSolver();
        this.lexicographicOrder = lexOrder;
        this.closeUnderTransitions = closeUnderTransitions;
        this.checkInitSubset = checkInitSubset;
        this.ceElimination = new CEElimination(solver);
    }

    public void setup() {
        automataBEncoding = new AutomataEncoding(solver, automataNumStates, numLetters);
        try {
            LOGGER.debug("Encoding automaton");
            automataBEncoding.encode();
            if (lexicographicOrder) {
                RankingFunction rankingFunctionEncoding = new RankingFunction(solver, transducerNumStates, numLetters);
                LOGGER.debug("Encoding ranking function");
                rankingFunctionEncoding.encode();
            } else {
                transducerEncoding = new TransducerEncoding(solver, transducerNumStates, numLetters);
                transitivitySet = new TransitivityPairSet(transducerEncoding);
                LOGGER.debug("Encoding transducer");
                transducerEncoding.encode();
            }

            updateWithOldCE();

        } catch (ContradictionException e) {
            // nothing
        } catch (TimeoutException e) {
            throw new RuntimeException("timeout");
        }
    }

    private Automata invariant = null;

    public Automata getInvariant() {
        return invariant;
    }

    public Automata findNextSolution()
            throws Timer.TimeoutException {
        boolean unsat = true;
        boolean success = false;

        try {
            while (solver.isSatisfiable()) {
                round += 1;
                LOGGER.debug("Satisfiable, round " + round + ", clause num " + solver.getClauseNum());

                if (stopped) {
                    LOGGER.debug("stopped");
                    return null;
                }

                unsat = false;
                Set<Integer> modelPosVars = solver.positiveModelVars();

                invariant = BoolValToAutomaton.toAutomata(modelPosVars, automataBEncoding);
                assert (invariant.isDFA());
                LOGGER.debug("Guess an invariant:");
                LOGGER.debug(invariant);

                Timer.tick();

                ////////////////////////////////////////////////////////
                if (checkInitSubset) {
                    SubsetChecking l0 = new SubsetChecking(I, invariant);
                    List<Integer> w = l0.check();
                    if (w != null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Invariant does't contain all initial configurations! Counterexample:");
                            LOGGER.debug(NoInvariantException.getLabeledWord(w));
                        }
                        ceElimination.ce0Elimination(automataBEncoding, w);
                        oldCounterExamples.addL0B(w);
                        continue;
                    }
                }

                Timer.tick();

                ////////////////////////////////////////////////////////
                if (closeUnderTransitions) {
                    InductivenessChecking l1 = new InductivenessChecking(
                            invariant, T, numLetters);
                    Tuple<List<Integer>> xy = l1.check();
                    if (xy != null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Invariant is not inductive! Counterexample:");
                            LOGGER.debug(NoInvariantException.getLabeledWord(xy.x)
                                    + " implies " + NoInvariantException.getLabeledWord(xy.y));
                        }
                        ceElimination.ce1Elimination(automataBEncoding, xy);
                        oldCounterExamples.addL1(xy);
                        continue;
                    }
                }

                Timer.tick();

                ////////////////////////////////////////////////////////
                // Check if invariant and bad states are disjoint
                Automata inter = AutomataUtility.getIntersection(invariant, B);
                List<Integer> cex = AutomataUtility.findSomeShortestWord(inter);
//                List<Integer> cex = new SubsetChecking(F, AutomataConverter.getComplement(automatonB)).check();
                if (cex != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Invariant contain bad configurations! Counterexample:");
                        LOGGER.debug(NoInvariantException.getLabeledWord(cex));
                    }
                    solver.addClause(new int[]{-automataBEncoding.acceptWord(cex)});
                    continue;
                }

                Timer.tick();

                // otherwise we are finished!
                success = true;
                break;
            }
        } catch (ContradictionException e) {
            // nothing
            LOGGER.debug(e);
        } catch (org.sat4j.specs.TimeoutException e) {
            LOGGER.debug(e);
            throw new RuntimeException("timeout");
        }

        if (success) {
            return invariant;
        } else {
            LOGGER.debug("No more models exist.");
        }
        if (unsat) LOGGER.debug("Unsatisfiable!");
        return null;
    }

    public void addBMembershipConstraint(List<Integer> word) {
        try {
            ceElimination.ce0Elimination(automataBEncoding, word);
            oldCounterExamples.addL0B(word);
        } catch (ContradictionException e) {
            // nothing
        }
    }

    public void addBNonMembershipConstraint(List<Integer> word) {
        try {
            solver.addClause(new int[]{-automataBEncoding.acceptWord(word)});
        } catch (ContradictionException e) {
            // nothing
        }
    }

    public void addDisjBMembershipConstraint(List<List<Integer>> words) {
        try {
            int[] clause = new int[words.size()];

            for (int i = 0; i < words.size(); ++i) {
                //		WordAcceptance wordAcceptance =
                //		    new WordAcceptance(automataBEncoding);
                //		clause[i] = wordAcceptance.encode(words.get(i));
                clause[i] = automataBEncoding.acceptWord(words.get(i));
            }

            solver.addClause(clause);

        } catch (ContradictionException e) {
            // nothing
        }
    }

    public void fixTransducer(EdgeWeightedDigraph relation) {
        assert (!lexicographicOrder);
        assert (relation.getNumVertices() == transducerNumStates);
        assert (relation.getSourceVertex() == 0);

        try {
            for (int s1 = 1; s1 <= transducerNumStates; ++s1)
                for (int s2 = 1; s2 <= transducerNumStates; ++s2)
                    for (int l1 = 0; l1 < numLetters; ++l1)
                        for (int l2 = 0; l2 < numLetters; ++l2) {
                            // check whether this transition exists in the
                            // automaton
                            boolean found = false;
                            for (DirectedEdge edge : relation.getIncidentEdges(s1 - 1)) {
                                DirectedEdgeWithInputOutput ioEdge =
                                        (DirectedEdgeWithInputOutput) edge;
                                if (ioEdge.to() == s2 - 1 &&
                                        ioEdge.getInput() == l1 &&
                                        ioEdge.getOutput() == l2)
                                    found = true;
                            }

                            solver.addClause(new int[]{(found ? 1 : -1) *
                                    transducerEncoding.getTransBoolVar
                                            (s1, l1, l2, s2)});
                        }

            final Set<Integer> accepting = relation.getDestVertices();
            for (int s = 1; s <= transducerNumStates; ++s) {
                boolean a = accepting.contains(s - 1);
                solver.addClause(new int[]{(a ? 1 : -1) *
                        transducerEncoding.getIndexZVar(s)});
            }
        } catch (ContradictionException e) {
            // nothing
        }
    }

    public void assertLargerTransducer(EdgeWeightedDigraph relation) {
        assert (!lexicographicOrder);
        assert (relation.getNumVertices() == transducerNumStates);
        assert (relation.getSourceVertex() == 0);

        List<Integer> unsetVariables = new ArrayList<Integer>();

        try {
            for (int s1 = 1; s1 <= transducerNumStates; ++s1)
                for (int s2 = 1; s2 <= transducerNumStates; ++s2)
                    for (int l1 = 0; l1 < numLetters; ++l1)
                        for (int l2 = 0; l2 < numLetters; ++l2) {
                            // check whether this transition exists in the
                            // automaton
                            boolean found = false;
                            for (DirectedEdge edge : relation.getIncidentEdges(s1 - 1)) {
                                DirectedEdgeWithInputOutput ioEdge =
                                        (DirectedEdgeWithInputOutput) edge;
                                if (ioEdge.to() == s2 - 1 &&
                                        ioEdge.getInput() == l1 &&
                                        ioEdge.getOutput() == l2)
                                    found = true;
                            }

                            final int var =
                                    transducerEncoding.getTransBoolVar(s1, l1, l2, s2);

                            if (found)
                                solver.addClause(new int[]{var});
                            else
                                unsetVariables.add(var);
                        }

            final Set<Integer> accepting = relation.getDestVertices();
            for (int s = 1; s <= transducerNumStates; ++s) {
                boolean a = accepting.contains(s - 1);
                final int var = transducerEncoding.getIndexZVar(s);
                if (a)
                    solver.addClause(new int[]{var});
                else
                    unsetVariables.add(var);
            }

            if (!unsetVariables.isEmpty()) {
                int[] clause = new int[unsetVariables.size()];
                for (int i = 0; i < clause.length; ++i)
                    clause[i] = unsetVariables.get(i);
                solver.addClause(clause);
            }
        } catch (ContradictionException e) {
            // nothing
        }
    }

    private void updateWithOldCE() throws ContradictionException {
        //update old counter example

        LOGGER.debug("Updating encoding with old counter examples...");

        for (List<List<Integer>> ce : oldCounterExamples.getTransitivityCEs()) {
            ceElimination.ce3Elimination(transducerEncoding, transitivitySet, ce);
        }

        for (List<Integer> ce : oldCounterExamples.getL0B()) {
            ceElimination.ce0Elimination(automataBEncoding, ce);
        }

        for (Tuple<List<Integer>> ce : oldCounterExamples.getL1()) {
            ceElimination.ce1Elimination(automataBEncoding, ce);
        }
    }

    private void writeToDot(Automata invariant,
                            EdgeWeightedDigraph transducer) {
        try {
            DOTPrinter.writeOut(DOTPrinter.getString(invariant, indexToLabel),
                    OUTPUT_DIR + "/invariant.dot");
            DOTPrinter.writeOut(DOTPrinter.getString(transducer, indexToLabel),
                    OUTPUT_DIR + "/transducer.dot");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setTransducerNumStates(int transducerNumStates) {
        this.transducerNumStates = transducerNumStates;
    }

    public int getNumLetters() {
        return numLetters;
    }

    public void setNumLetters(int numLetters) {
        this.numLetters = numLetters;
    }

    public void setAutomataNumStates(int automataNumStates) {
        this.automataNumStates = automataNumStates;
    }

    public Automata getI() {
        return I;
    }

    public void setI(Automata init) {
        I = init;
    }

    public Automata getB() {
        return B;
    }

    public void setB(Automata bad) {
        B = bad;
    }

    public EdgeWeightedDigraph getT() {
        return T;
    }

    public void setT(EdgeWeightedDigraph trans) {
        T = trans;
    }

    public void setIndexToLabel(Map<Integer, String> indexToLabel) {
        this.indexToLabel = indexToLabel;
    }

    public void setOldCounterExamples(OldCounterExamples oldCounterExamples) {
        this.oldCounterExamples = oldCounterExamples;
    }
}

// vim: tabstop=4
