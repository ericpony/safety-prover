package verification;

import common.Utility;
import common.VerificationUltility;
import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import elimination.CEElimination;
import elimination.TransitivityPairSet;
import encoding.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.FileNotFoundException;
import java.util.*;

public class ReachabilityChecking {
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<String, Integer> labelToIndex = new HashMap<String, Integer>();
    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    private final boolean closeUnderTransitions;
    private final boolean checkI0Subset;
    private final boolean lexicographicOrder;

    private int transducerNumStates;
    private int automataNumStates;
    private int numLetters;

    private ISatSolver solver;
    private ISatSolverFactory solverFactory;

    private Automata I0;
    private Automata F;
    private Automata winningStates;
    private Automata systemInvariant;
    private EdgeWeightedDigraph player1;
    private EdgeWeightedDigraph player2;

    private OldCounterExamples oldCounterExamples;
    private FiniteStateSets finiteStates;

    private AutomataEncoding automataBEncoding = null;
    private TransducerEncoding transducerEncoding = null;
    private RankingFunction rankingFunctionEncoding = null;
    private TransitivityPairSet transitivitySet = null;

    private final CEElimination ceElimination;

    private int round = 0;

    private boolean stopped = false;

    public void stopChecking() {
        stopped = true;
    }

    public ReachabilityChecking(boolean lexOrder,
                                boolean closeUnderTransitions,
                                boolean checkI0Subset,
                                ISatSolverFactory solverFactory) {
        solver = solverFactory.spawnSolver();
        this.solverFactory = solverFactory;
        this.lexicographicOrder = lexOrder;
        this.closeUnderTransitions = closeUnderTransitions;
        this.checkI0Subset = checkI0Subset;
        this.ceElimination = new CEElimination(solver);
    }

    public void setup() {
        automataBEncoding = new AutomataEncoding(solver, automataNumStates, numLetters);
        try {
            LOGGER.debug("Encoding automaton");
            automataBEncoding.encode();
            if (lexicographicOrder) {
                rankingFunctionEncoding = new RankingFunction(solver, transducerNumStates, numLetters);
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

    private Automata automatonB = null;

    public Automata getAutomatonB() {
        return automatonB;
    }

    private EdgeWeightedDigraph transducer = null;

    public EdgeWeightedDigraph getTransducer() {
        return transducer;
    }

    public boolean findNextSolution(boolean printResult) {
        boolean unsat = true;
        boolean success = false;

        try {
            solverLoop:
            while (solver.isSatisfiable()) {
                round += 1;
                LOGGER.debug("Satisfiable, round " + round + ", clause num " + solver.getClauseNum());

                if (stopped) {
                    LOGGER.debug("stopped");
                    return false;
                }

                unsat = false;
                Set<Integer> modelPosVars = solver.positiveModelVars();

                automatonB = BoolValToAutomaton.toAutomata(modelPosVars, automataBEncoding);
                assert (automatonB.isDFA());
                LOGGER.debug("Guess an invariant:");
                LOGGER.debug(automatonB);

                ////////////////////////////////////////////////////////
                if (checkI0Subset) {
                    SubsetChecking l0 = new SubsetChecking(I0, automatonB);
                    List<Integer> w = l0.check();
                    if (w != null) {
                        LOGGER.debug("Invariant does't contain all initial configurations! Counterexample:");
                        LOGGER.debug(w);
                        ceElimination.ce0Elimination(automataBEncoding, w);
                        oldCounterExamples.addL0B(w);
                        continue;
                    }
                }
                ////////////////////////////////////////////////////////
                if (closeUnderTransitions) {
                    Automata aut = automatonB;
                    Automata complementF = AutomataConverter.getComplement(F);
                    InductivenessChecking l1 = new InductivenessChecking(aut, complementF, player2, numLetters);
                    List<List<Integer>> xy = l1.check();
                    if (xy != null) {
                        LOGGER.debug("Invariant is not inductive! Counterexample:");
                        LOGGER.debug(xy);
                        ceElimination.ce1Elimination(automataBEncoding, xy);
                        oldCounterExamples.addL1(xy);
                        continue;
                    }
                }
                ////////////////////////////////////////////////////////
                // Check if invariant and bad states are disjoint
                Automata inter = VerificationUltility.getIntersection(automatonB, F);
                List<Integer> cex = AutomataConverter.getSomeShortestWord(inter);
//                List<Integer> cex = new SubsetChecking(F, AutomataConverter.getComplement(automatonB)).check();
                if (cex != null) {
                    if (cex.size() == 0) cex.add(Automata.EPSILON_LABEL);
                    LOGGER.debug("Invariant contain bad configurations! Counterexample:");
                    LOGGER.debug(cex);
                    solver.addClause(new int[]{-automataBEncoding.acceptWord(cex)});
                    continue;
                }

                // otherwise we are finished!
                success = true;
                if (printResult) {
                    Map<Integer, String> indexToLabel = new HashMap<Integer, String>();
                    for (Map.Entry<String, Integer> entry : labelToIndex.entrySet())
                        indexToLabel.put(entry.getValue(), entry.getKey());

                    System.out.println("VERDICT: Bad configurations are not reachable from every " +
                            (closeUnderTransitions ? "reachable" : "initial") + " configuration.");
                    System.out.println();
                    System.out.println("// Configurations visited in the game are contained in");
                    System.out.println(automatonB.prettyPrint("B", indexToLabel));

                    if (systemInvariant != null) {
                        System.out.println("// System invariant");
                        System.out.println(systemInvariant.prettyPrint("I", indexToLabel));
                    }
                    //write to dot
//                  writeToDot(automatonB, transducer);
                }
                break;
            }
        } catch (ContradictionException e) {
            // nothing
            LOGGER.debug(e);
        } catch (TimeoutException e) {
            LOGGER.debug(e);
            throw new RuntimeException("timeout");
        }

        if (success) {
            return true;
        } else {
            LOGGER.debug("No more models exist.");
        }
        if (unsat) LOGGER.debug("Unsatisfiable!");

        return false;
    }

    private void strengthenSystemInvariant(List<Integer> x) {
        OldCounterExamples oldCEs = new OldCounterExamples();
        Automata newInv = null;
        Automata knownInv = VerificationUltility.getIntersection(systemInvariant, AutomataConverter.getComplement(F));

        for (int num = 1; num < 20 && newInv == null; ++num) {
            RelativeInvariantSynth invSynth = new RelativeInvariantSynth
                    (solverFactory, numLetters, I0, knownInv, player1, player2, x, oldCEs, num);
            newInv = invSynth.infer();
        }

        systemInvariant = VerificationUltility.getIntersection(systemInvariant, newInv);

        assert (systemInvariant.isDFA());

        if (!systemInvariant.isCompleteDFA()) {
            systemInvariant = AutomataConverter.toCompleteDFA(systemInvariant);
        }

        systemInvariant = AutomataConverter.toMinimalDFA(systemInvariant);

        LOGGER.debug("new system invariant is " + systemInvariant);
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
        assert (relation.V() == transducerNumStates);
        assert (relation.getInitState() == 0);

        try {
            for (int s1 = 1; s1 <= transducerNumStates; ++s1)
                for (int s2 = 1; s2 <= transducerNumStates; ++s2)
                    for (int l1 = 0; l1 < numLetters; ++l1)
                        for (int l2 = 0; l2 < numLetters; ++l2) {
                            // check whether this transition exists in the
                            // automaton
                            boolean found = false;
                            for (DirectedEdge edge : relation.adj(s1 - 1)) {
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

            final Set<Integer> accepting = relation.getAcceptingStates();
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
        assert (relation.V() == transducerNumStates);
        assert (relation.getInitState() == 0);

        List<Integer> unsetVariables = new ArrayList<Integer>();

        try {
            for (int s1 = 1; s1 <= transducerNumStates; ++s1)
                for (int s2 = 1; s2 <= transducerNumStates; ++s2)
                    for (int l1 = 0; l1 < numLetters; ++l1)
                        for (int l2 = 0; l2 < numLetters; ++l2) {
                            // check whether this transition exists in the
                            // automaton
                            boolean found = false;
                            for (DirectedEdge edge : relation.adj(s1 - 1)) {
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

            final Set<Integer> accepting = relation.getAcceptingStates();
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

    private void updateWithOldCE()
            throws ContradictionException {
        //update old counter example

        LOGGER.debug("Updating encoding with old counter examples...");
        for (List<List<Integer>> ce : oldCounterExamples.getProgressCEs()) {
            ceElimination.ce4Elimination(automataBEncoding, transducerEncoding,
                    transitivitySet, rankingFunctionEncoding, ce, winningStates, player2);
        }

        for (List<List<Integer>> ce : oldCounterExamples.getTransitivityCEs()) {
            ceElimination.ce3Elimination(transducerEncoding, transitivitySet, ce);
        }

        for (List<Integer> ce : oldCounterExamples.getL0B()) {
            ceElimination.ce0Elimination(automataBEncoding, ce);
        }

        for (List<List<Integer>> ce : oldCounterExamples.getL1()) {
            ceElimination.ce1Elimination(automataBEncoding, ce);
        }
    }

    private void writeToDot(Automata automatonB,
                            EdgeWeightedDigraph transducer) {
        try {
            Utility.writeOut(Utility.toDot(automatonB, labelToIndex),
                    OUTPUT_DIR + "/automatonB.dot");
            Utility.writeOut(Utility.toDot(transducer, labelToIndex),
                    OUTPUT_DIR + "/transducerOrder.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getTransducerNumStates() {
        return transducerNumStates;
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

    public int getAutomataNumStates() {
        return automataNumStates;
    }

    public void setAutomataNumStates(int automataNumStates) {
        this.automataNumStates = automataNumStates;
    }

    public Automata getI0() {
        return I0;
    }

    public void setI0(Automata i0) {
        I0 = i0;
    }

    public Automata getF() {
        return F;
    }

    public void setF(Automata f) {
        F = f;
    }

    public void setWinningStates(Automata f) {
        winningStates = f;
    }

    public EdgeWeightedDigraph getPlayer2() {
        return player2;
    }

    public void setPlayer2(EdgeWeightedDigraph player2) {
        this.player2 = player2;
    }

    public Map<String, Integer> getLabelToIndex() {
        return labelToIndex;
    }

    public void setLabelToIndex(Map<String, Integer> labelToIndex) {
        this.labelToIndex = labelToIndex;
    }

    public void setOldCounterExamples(OldCounterExamples oldCounterExamples) {
        this.oldCounterExamples = oldCounterExamples;
    }

    public void setFiniteStateSets(FiniteStateSets finiteStates) {
        this.finiteStates = finiteStates;
    }

    public Automata getSystemInvariant() {
        return systemInvariant;
    }

    public void setSystemInvariant(Automata inv) {
        systemInvariant = inv;
    }

}

// vim: tabstop=4
