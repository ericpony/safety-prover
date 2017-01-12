package verification;

import common.VerificationUltility;
import common.finiteautomata.Automata;
import encoding.ISatSolverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import visitor.RegularModel;

public class MonolithicVerifier {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ISatSolverFactory SOLVER_FACTORY;
    private final boolean useRankingFunctions;

    private static final boolean useGlobalSystemInvariant = false;

    private final RegularModel problem;

    public MonolithicVerifier(RegularModel problem,
                              ISatSolverFactory SOLVER_FACTORY,
                              boolean useRankingFunctions) {
        this.problem = problem;
        this.SOLVER_FACTORY = SOLVER_FACTORY;
        this.useRankingFunctions = useRankingFunctions;
    }

    public boolean verify() {
        LOGGER.info("Constructing monolithic advice bits");

        int bound = problem.getMaxNumOfStatesTransducer() * problem.getMaxNumOfStatesTransducer() +
                problem.getMaxNumOfStatesAutomaton() * problem.getMaxNumOfStatesAutomaton();

        if (problem.getCloseInitStates()) {
            bound += problem.getMaxNumOfInitStatesAutomaton() *
                    problem.getMaxNumOfInitStatesAutomaton();
        }

        OldCounterExamples oldCEs = new OldCounterExamples();
        FiniteStateSets finiteStates = new FiniteStateSets(problem.getI(), problem.getT(), problem.getB());

        Automata systemInvariant = null;
        if (useGlobalSystemInvariant) {
            systemInvariant = VerificationUltility.getUniversalAutomaton(problem.getNumberOfLetters());
        }

        for (int fixedSOS = 1; fixedSOS <= bound; ++fixedSOS) {
            for (int numStateTransducer = problem.getMinNumOfStatesTransducer();
                 numStateTransducer <= problem.getMaxNumOfStatesTransducer();
                 numStateTransducer++) {
                for (int numStateAutomata = problem.getMinNumOfStatesAutomaton();
                     numStateAutomata <= problem.getMaxNumOfStatesAutomaton();
                     numStateAutomata++) {
                    for (int numInitStateAutomata = problem.getMinNumOfInitStatesAutomaton();
                         numInitStateAutomata <= problem.getMaxNumOfInitStatesAutomaton();
                         numInitStateAutomata++) {

                        int sos = numStateTransducer * numStateTransducer +
                                numStateAutomata * numStateAutomata;

                        if (problem.getCloseInitStates()) {
                            sos += numInitStateAutomata * numInitStateAutomata;
                        }

                        if (sos != fixedSOS) continue;

                        LOGGER.info("Advice bit size: # transducer states: " +
                                numStateTransducer + ", # automaton states: " + numStateAutomata);

                        ReachabilityChecking checking = new ReachabilityChecking(
                                useRankingFunctions,
                                problem.getCloseInitStates(),
                                true,
                                SOLVER_FACTORY);
                        checking.setAutomataNumStates(numStateAutomata);
                        checking.setF(problem.getB());
                        checking.setWinningStates(problem.getB());
                        checking.setI0(problem.getI());
                        checking.setNumLetters(problem.getNumberOfLetters());
//                        checking.setPlayer1(problem.getPlayer1());
                        checking.setPlayer2(problem.getT());
                        checking.setTransducerNumStates(numStateTransducer);
                        checking.setLabelToIndex(problem.getLabelToIndex());
                        checking.setOldCounterExamples(oldCEs);
                        checking.setFiniteStateSets(finiteStates);
                        checking.setSystemInvariant(systemInvariant);

                        checking.setup();

                        if (checking.findNextSolution(true)) return true;

                        systemInvariant = checking.getSystemInvariant();
                    }
                }
            }
        }
        return false;
    }
}

// vim: tabstop=4
