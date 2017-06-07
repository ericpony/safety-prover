
package verification;

import common.Timer;
import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import encoding.ISatSolverFactory;
import learning.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import visitor.RegularModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class IncrementalVerifier {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ISatSolverFactory SOLVER_FACTORY;
    private final boolean useRankingFunctions;

    private static final int initialFiniteExplorationBound = 3;
    // try to find a progress relation that covers as many
    // configurations as possible
    private static final boolean maximiseProgressRelations = true;
    // try to find a progress relation with as many transitions (and
    // accepting states) as possible
    private static final boolean maximiseTransducer = false;
    private static final boolean eliminateMultipleConfigurations = true;
    private static final int maxStoredRelationNum = 5;
    private static final int finiteVerificationBound = 6;

    private final boolean parallelise;
    private final boolean exploreTransducersParallel;

    private final boolean verifySolutions;
    private final boolean closeUnderRotation;
    private final List<Integer> rotationStartLetters;
    private final boolean preComputeReachable;

    private final RegularModel problem;

    private Automata player1Configs;
    private Automata winningStates;
    private int sosBound;
    private FiniteStateSets finiteStates;
    private Automata systemInvariant;
    private int explorationBound;

    private int exploredBoundSofar;
    private List<Configuration> configurationsUpToBound;

    private List<Automata> chosenBs;
    private List<EdgeWeightedDigraph> chosenTs;
    private List<EdgeWeightedDigraph> distinctRelations;

    private static class Configuration implements Comparable<Configuration> {
        public final List<Integer> word;
        public final int rank;

        public Configuration(List<Integer> word, int rank) {
            this.word = word;
            this.rank = rank;
        }

        public int compareTo(Configuration that) {
            return this.rank - that.rank;
        }

        public String toString() {
            return "(" + word + ", " + rank + ")";
        }
    }

    public IncrementalVerifier(RegularModel problem,
                               ISatSolverFactory SOLVER_FACTORY,
                               boolean useRankingFunctions,
                               boolean preComputeReachable,
                               boolean verifySolutions) {
        this.problem = problem;
        this.SOLVER_FACTORY = SOLVER_FACTORY;
        this.useRankingFunctions = useRankingFunctions;
        this.preComputeReachable = preComputeReachable;
        if (problem.getSymmetries().contains("rotation")) {
            this.closeUnderRotation = true;
            this.rotationStartLetters = null;
        } else {
            List<Integer> startLetters = null;

            for (String s : problem.getSymmetries())
                if (s.startsWith("rotation_")) {
                    startLetters = new ArrayList<Integer>();
                    final String[] letters = s.split("_");
                    for (int i = 1; i < letters.length; ++i)
                        startLetters.add(Integer.parseInt(letters[i]));
                    break;
                }

            this.rotationStartLetters = startLetters;
            this.closeUnderRotation = startLetters != null;
        }
        this.verifySolutions = verifySolutions;

        if (problem.getParLevel() <= 0) {
            parallelise = false;
            exploreTransducersParallel = false;
        } else if (problem.getParLevel() == 1) {
            parallelise = true;
            exploreTransducersParallel = false;
        } else {
            parallelise = true;
            exploreTransducersParallel = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setup()
            throws Timer.TimeoutException {
        player1Configs = VerificationUtility.computeDomain(problem.getPlayer1(),
                problem.getNumberOfLetters());
        winningStates = problem.getB();

        sosBound = problem.getMaxNumOfStatesTransducer() * problem.getMaxNumOfStatesTransducer() +
                problem.getMaxNumOfStatesAutomaton() * problem.getMaxNumOfStatesAutomaton();

        finiteStates = new FiniteStateSets(problem.getI(), problem.getT(), problem.getB());

        if (preComputeReachable) {
            Learner learner = new LStarLearner();
            Teacher teacher = new BasicRMCTeacher(problem.getNumberOfLetters(),
                    problem.getI(), problem.getB(), problem.getT());
            systemInvariant = new MonolithicLearning(learner, teacher).infer();
        } else {
            systemInvariant = AutomataUtility.getUniversalAutomaton(problem.getNumberOfLetters());
        }

        explorationBound = initialFiniteExplorationBound;

        chosenBs = new ArrayList<Automata>();
        chosenTs = new ArrayList<EdgeWeightedDigraph>();
        distinctRelations = new ArrayList<EdgeWeightedDigraph>();

        exploredBoundSofar = 0;
        configurationsUpToBound = new ArrayList<Configuration>();
    }

    ////////////////////////////////////////////////////////////////////////////

    private void setupExploredConfigurations() {

    }

    ////////////////////////////////////////////////////////////////////////////

    public boolean verify() {
        LOGGER.info("Constructing disjunctive advice bits");

        mainLoop:
        while (true) {

            setupExploredConfigurations();

            for (int configNum = 0; configNum < configurationsUpToBound.size(); ) {
                final Configuration config = configurationsUpToBound.get(configNum);
                final int rank = config.rank;
                LOGGER.debug("checking configuration " + config.word + ", rank " +
                        rank + " ...");

                final boolean coveredConfig = winningStates.accepts(config.word);
                if (coveredConfig) {
                    LOGGER.debug("already covered");
                    ++configNum;
                } else {
                    LOGGER.debug("not covered, extending progress relation");

                    //                if (!distinctRelations.isEmpty() && reuseProgressRelations())
                    //                    continue;

                    final List<List<Integer>> elimWords =
                            new ArrayList<List<Integer>>();
                    elimWords.add(config.word);

                    if (eliminateMultipleConfigurations) {
                        for (int i = configNum + 1;
                             i < configurationsUpToBound.size() && configurationsUpToBound.get(i).rank == rank;
                             ++i) {
                            if (!winningStates.accepts(configurationsUpToBound.get(i).word))
                                elimWords.add(configurationsUpToBound.get(i).word);
                        }
                    }

                    LOGGER.debug("trying to rank one of " + elimWords);

                    final CountDownLatch finishLatch = new CountDownLatch(1);

                    final List<ProgressBuilder> builders = new ArrayList<ProgressBuilder>();
                    final List<Thread> builderThreads = new ArrayList<Thread>();

                    // builders for reusing old relations
                    int num = 0;
                    for (EdgeWeightedDigraph relation : distinctRelations) {
                        List<List<Integer>> extraWords = new ArrayList<List<Integer>>();


                        if (!extraWords.isEmpty()) {
                            LOGGER.debug("relation #" + num + " can rank " + extraWords);
                            final ProgressBuilder builder =
                                    new ReusingRelationBuilder(finishLatch, relation, num, extraWords,
                                            problem.getMaxNumOfStatesAutomaton());
                            builders.add(builder);
                            builderThreads.add(new Thread(builder));
                        }

                        ++num;
                    }

                    // builders for constructing new relations
                    for (int n = exploreTransducersParallel ?
                            1 : problem.getMaxNumOfStatesTransducer();
                         n <= problem.getMaxNumOfStatesTransducer();
                         ++n) {
                        final ProgressBuilder newRelationBuilder =
                                new ProgressRelationBuilder(finishLatch, elimWords, n);
                        builders.add(newRelationBuilder);
                        builderThreads.add(new Thread(newRelationBuilder));
                    }

                    //                computeProgressRelation(elimWords);

                    try {
                        if (parallelise) {
                            for (Thread t : builderThreads)
                                t.start();

                            finishLatch.await();

                            // stop all threads
                            for (ProgressBuilder builder : builders)
                                builder.stopBuilding();
                            for (Thread t : builderThreads)
                                t.join();
                        } else {
                            // run the threads one by one
                            for (int i = 0; i < builders.size(); ++i) {
                                builderThreads.get(i).start();
                                builderThreads.get(i).join();
                                if (builders.get(i).finished)
                                    break;
                            }
                        }

                        boolean oneDone = false;
                        for (ProgressBuilder builder : builders) {
                            if (builder.finished) {
                                builder.copyBackResults();
                                oneDone = true;
                                break;
                            }
                        }

                        if (!oneDone)
                            throw new RuntimeException
                                    ("Could not extend advice bit further");
                    } catch (InterruptedException e) {
                        LOGGER.error("interrupted");
                    }
                }
            }

            LOGGER.info("all reachable configurations up to length " + explorationBound +
                    " are covered");

            // check whether we have found a solution that covers the
            // complete game graph
            if (checkConvergence())
                break mainLoop;
        } // mainLoop

        printResult();

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////

    private abstract class ProgressBuilder implements Runnable {
        private final CountDownLatch finishLatch;
        public boolean finished = false;

        public ProgressBuilder(CountDownLatch finishLatch) {
            this.finishLatch = finishLatch;
        }

        protected void callFinished() {
            finished = true;
            finishLatch.countDown();
        }

        protected boolean stopped = false;

        public abstract void copyBackResults();

        public void stopBuilding() {
            stopped = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compute a progress relation and set that eliminate at least one
     * of the given words.
     */
    private class ProgressRelationBuilder extends ProgressBuilder {
        private final List<List<Integer>> elimWords;
        private final int maxNumStatesTransducer;

        private Automata localInvariant;

        private ReachabilityChecking checking = null;
        private Automata B = null;
        private EdgeWeightedDigraph transducer = null;

        public ProgressRelationBuilder(CountDownLatch finishLatch,
                                       List<List<Integer>> elimWords,
                                       int maxNumStatesTransducer) {
            super(finishLatch);
            this.elimWords = elimWords;
            this.localInvariant = systemInvariant;
            this.maxNumStatesTransducer = maxNumStatesTransducer;
        }

        public void run() {
            LOGGER.debug("computing new progress relation for one of " + elimWords);
            try {
                OldCounterExamples oldCEs = new OldCounterExamples();

                sosLoop:
                for (int fixedSOS = 1;
                     fixedSOS <= sosBound;
                     ++fixedSOS) {
                    for (int numStateTransducer = problem.getMinNumOfStatesTransducer();
                         numStateTransducer <= maxNumStatesTransducer;
                         numStateTransducer++) {
                        for (int numStateAutomata = problem.getMinNumOfStatesAutomaton();
                             numStateAutomata <= problem.getMaxNumOfStatesAutomaton();
                             numStateAutomata++) {

                            if (stopped) {
                                LOGGER.debug("stopped");
                                return;
                            }

                            final int sos =
                                    numStateTransducer * numStateTransducer +
                                            numStateAutomata * numStateAutomata;

                            if (sos != fixedSOS)
                                continue;

                            checking =
                                    createReachabilityChecking(useRankingFunctions,
                                            numStateAutomata,
                                            numStateTransducer,
                                            oldCEs,
                                            localInvariant);

                            checking.setup();
                            checking.addDisjBMembershipConstraint(elimWords);

                            if (checking.findNextSolution() != null) {
                                B = checking.getInvariant();

                                // can the solution be made more general?
                                if (maximiseProgressRelations)
                                    while (true) {
                                        final List<List<Integer>> remElimWords =
                                                new ArrayList<List<Integer>>();
                                        for (List<Integer> w : elimWords) {
                                            if (B.accepts(w))
                                                checking.addBMembershipConstraint(w);
                                            else
                                                remElimWords.add(w);
                                        }

                                        if (remElimWords.isEmpty())
                                            break;

                                        LOGGER.debug("trying to cover also one of " + remElimWords);

                                        checking.addDisjBMembershipConstraint(remElimWords);
                                        if (checking.findNextSolution() != null) {
                                            B = checking.getInvariant();
                                        } else
                                            break;
                                    }

                                if (stopped) {
                                    LOGGER.debug("stopped");
                                    return;
                                }
                                LOGGER.debug("found new progress relation!");
                                callFinished();
                                return;
                            }
                        }
                    }
                }
            } catch (Timer.TimeoutException e) {
            }
            LOGGER.debug("giving up");
        }

        public void copyBackResults() {
            // augment the set of winning states and continue
            // with the next configuration
            augmentWinningStates(checking, B, transducer);
            distinctRelations.add(0, transducer);

            while (distinctRelations.size() > maxStoredRelationNum)
                distinctRelations.remove(distinctRelations.size() - 1);

            LOGGER.debug("new progress relation: " + transducer);

            LOGGER.info("storing " + distinctRelations.size() +
                    " progress relations for reuse");

            systemInvariant = localInvariant;
        }

        public void stopBuilding() {
            super.stopBuilding();
            if (checking != null)
                checking.stopChecking();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compute a regular set B for which the given progress relation
     * eliminates at least one of the given words.
     */
    private class ReusingRelationBuilder extends ProgressBuilder {
        private final EdgeWeightedDigraph relation;
        private final int relationNum;
        private final List<List<Integer>> elimWords;
        private final int maxNumStatesAutomaton;

        private Automata localInvariant;

        private ReachabilityChecking checking = null;
        private Automata B = null;

        public ReusingRelationBuilder(CountDownLatch finishLatch,
                                      EdgeWeightedDigraph relation,
                                      int relationNum,
                                      List<List<Integer>> elimWords,
                                      int maxNumStatesAutomaton) {
            super(finishLatch);
            this.relation = relation;
            this.relationNum = relationNum;
            this.elimWords = elimWords;
            this.localInvariant = systemInvariant;
            this.maxNumStatesAutomaton = maxNumStatesAutomaton;
        }

        public void run() {
            LOGGER.debug("reusing relation for one of " + elimWords);
            try {
                OldCounterExamples oldCEs = new OldCounterExamples();

                for (int numStateAutomata = 1;
                     numStateAutomata <= maxNumStatesAutomaton;
                     numStateAutomata++) {

                    if (stopped) {
                        LOGGER.debug("stopped");
                        return;
                    }

                    checking =
                            createReachabilityChecking(false, numStateAutomata,
                                    relation.getNumVertices(), oldCEs,
                                    systemInvariant);

                    checking.setup();
                    checking.addDisjBMembershipConstraint(elimWords);
                    checking.fixTransducer(relation);


                    if (checking.findNextSolution() != null) {
                        LOGGER.debug("could reuse progress relation!");
                        callFinished();
                        return;
                    }
                }
            } catch (Timer.TimeoutException e) {
            }
            LOGGER.debug("giving up");
        }

        public void copyBackResults() {
            // augment the set of winning states and continue
            // with the next configuration
            augmentWinningStates(checking, checking.getInvariant(), relation);

            // move successful relation to the beginning
            distinctRelations.remove(relationNum);
            distinctRelations.add(0, relation);

            systemInvariant = localInvariant;
        }

        public void stopBuilding() {
            super.stopBuilding();
            if (checking != null)
                checking.stopChecking();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private ReachabilityChecking createReachabilityChecking
            (boolean useRF, int numStateAutomata, int numStateTransducer,
             OldCounterExamples oldCEs, Automata systemInvariant) {
        LOGGER.debug("Transducer states: " + numStateTransducer +
                ", automaton states: " + numStateAutomata);
        ReachabilityChecking checking =
                new ReachabilityChecking(useRF, false, false, SOLVER_FACTORY);
        checking.setAutomataNumStates(numStateAutomata);
        checking.setNumLetters(problem.getNumberOfLetters());
        checking.setB(problem.getB());
        checking.setI(problem.getI());
        checking.setT(problem.getT());
        checking.setIndexToLabel(problem.getIndexToLabel());
        checking.setOldCounterExamples(oldCEs);
        checking.setTransducerNumStates(numStateTransducer);

        return checking;
    }

    private void augmentWinningStates(ReachabilityChecking checking,
                                      Automata B,
                                      EdgeWeightedDigraph transducer) {
        // augment the set of winning states and continue
        // with the next configuration

        Automata BClosure;
        if (closeUnderRotation) {
            if (rotationStartLetters == null)
                BClosure =
                        AutomataUtility.closeUnderRotation(B);
            else
                BClosure =
                        AutomataUtility.closeUnderRotation(B, rotationStartLetters);
        } else {
            BClosure = B;
        }

        winningStates =
                AutomataUtility.minimise
                        (AutomataUtility.getUnion(winningStates, BClosure));

        chosenBs.add(B);
        chosenTs.add(transducer);

        LOGGER.info("found (Bi, Ti) pair: # transducer states: " +
                transducer.getNumVertices() +
                ", # automaton states: " +
                B.getStates().length);
        LOGGER.info("extending winning set, now have " +
                chosenBs.size() + " (Bi, Ti) pairs");
    }

    ////////////////////////////////////////////////////////////////////////////

    private boolean checkConvergence() {
        try {
            checkConvergence:
            while (true) {
                SubsetChecking checking =
                        new SubsetChecking
                                (AutomataUtility.getIntersection(systemInvariant,
                                        player1Configs),
                                        winningStates);
                List<Integer> cex = checking.check();
                if (cex == null)
                    return true;

                if (finiteStates.isReachable(cex)) {
                    assert (cex.size() > explorationBound);
                    explorationBound = cex.size();
                    LOGGER.info("now checking configurations up to length " + explorationBound);
                    break checkConvergence;
                } else {
                    LOGGER.debug("" + cex + " is not reachable, strengthening invariant");

                    OldCounterExamples oldCEs = new OldCounterExamples();
                    Automata newInv = null;
                    Automata knownInv =
                            AutomataUtility.getIntersection
                                    (systemInvariant,
                                            AutomataUtility.getComplement(problem.getB()));
                    for (int num = 1; num < 20 && newInv == null; ++num) {
                        RelativeInvariantSynth invSynth =
                                new RelativeInvariantSynth(SOLVER_FACTORY,
                                        problem.getNumberOfLetters(),
                                        problem.getI(), knownInv,
                                        problem.getPlayer1(),
                                        problem.getT(),
                                        cex, oldCEs, num);
                        newInv = invSynth.infer();
                    }

                    systemInvariant =
                            AutomataUtility.getIntersection(systemInvariant, newInv);

                    assert (systemInvariant.isDFA());

                    if (!systemInvariant.isCompleteDFA()) {
                        systemInvariant =
                                AutomataUtility.toCompleteDFA(systemInvariant);
                    }

                    systemInvariant =
                            AutomataUtility.toMinimalDFA(systemInvariant);

                    LOGGER.debug("new system invariant is " + systemInvariant);
                }
            } // checkConvergence
        } catch (Timer.TimeoutException e) {
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////

    // verify that the computed progress relations actually solve the
    // game, for configurations of length len
    private void verifyResults(int len) {
        /*
        final EdgeWeightedDigraph player1 = problem.getPlayer1();
        final EdgeWeightedDigraph player2 = problem.getT();
        final int numLetters = problem.getNumberOfLetters();

        final Set<List<Integer>> p2winning =
                new HashSet<List<Integer>>();
        p2winning.addAll(AutomataConverter.getWords(problem.getB(), len));

        for (int i = 0; i < chosenBs.size(); ++i) {
            final Automata B = chosenBs.get(i);
            final EdgeWeightedDigraph T = chosenTs.get(i);

            boolean changed = true;
            while (changed) {
                changed = false;

                addLoop:
                for (List<Integer> w :
                        AutomataConverter.getWords(B, len))
                    if (!p2winning.contains(w)) {
                        final List<List<Integer>> wImage =
                                AutomataConverter.getWords
                                        (AutomataConverter.getImage
                                                        (w, player1, numLetters),
                                                len);

                        if (wImage.isEmpty())
                            continue;

                        for (List<Integer> v : wImage) {
                            boolean isRankable = false;
                            for (List<Integer> u :
                                    AutomataConverter.getWords
                                            (AutomataConverter.getImage
                                                            (v, player2, numLetters),
                                                    len))
                                if (p2winning.contains(u) &&
                                        B.accepts(u) &&
                                        AutomataConverter.getImage(u, T,
                                                numLetters)
                                                .accepts(w)) {
                                    isRankable = true;
                                    break;
                                }
                            if (!isRankable)
                                continue addLoop;
                        }

                        p2winning.add(w);
                        if (closeUnderRotation) {
                            // also add rotated versions
                            List<Integer> w2 = new ArrayList<Integer>();
                            w2.addAll(w);
                            for (int j = 0; j < len; ++j) {
                                w2.add(w2.get(0));
                                w2.remove(0);
                                if (rotationStartLetters == null ||
                                        rotationStartLetters.contains(w2.get(0)))
                                    p2winning.add(new ArrayList<Integer>(w2));
                            }
                        }
                        changed = true;
                    }
            }

            for (List<Integer> w : AutomataConverter.getWords(B, len))
                if (player1Configs.accepts(w) && finiteStates.isReachable(w))
                    if (!p2winning.contains(w))
                        throw new RuntimeException("(B" + i + ", T" + i +
                                ") is incorrect, not winning: " + w);
        }

        for (List<Integer> w : finiteStates.getReachableStates(len))
            if (player1Configs.accepts(w))
                if (!p2winning.contains(w))
                    throw new RuntimeException
                            ("Solution is incorrect: don't know how to win from " +
                                    w);
    */
    }

    ////////////////////////////////////////////////////////////////////////////

    private void printResult() {
        LOGGER.info("FINISHED");

        Map<Integer, String> indexToLabel = problem.getIndexToLabel();

        System.out.println("VERDICT: Player 2 can win from every reachable configuration");
        System.out.println();

        System.out.println("// Approximation of reachable states");
        System.out.println(systemInvariant.prettyPrint("A", indexToLabel));

        System.out.println("// States from which T 2 can move and win");
        System.out.println(winningStates.prettyPrint("W", indexToLabel));

        System.out.println("// Progress relations" +
                (closeUnderRotation ? " (all to be closed under rotation)" : ""));

        for (int i = 0; i < chosenBs.size(); ++i) {
            System.out.println(chosenBs.get(i).prettyPrint("B" + i, indexToLabel));
            System.out.println(chosenTs.get(i).prettyPrint("T" + i, indexToLabel, indexToLabel));
        }
        System.out.println();

        System.out.println("// Assumptions made (but not checked):");
        System.out.println("// * players move in alternation");
        System.out.println("// * from every reachable non-terminal configuration, exactly one");
        System.out.println("//   of the players can make a move");
        if (closeUnderRotation)
            System.out.println("// * the game is symmetric under rotation");

        System.out.println();

        if (verifySolutions)
            for (int len = 0; len <= finiteVerificationBound; ++len) {
                System.out.print("// Verifying solution for configurations of " +
                        "length " + len + " ... ");
                verifyResults(len);
                System.out.println("done");
            }
    }

}

// vim: tabstop=4
