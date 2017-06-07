package verification;

import common.Timer;
import common.finiteautomata.Automata;
import encoding.ISatSolverFactory;
import learning.SatInvariantNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import visitor.RegularModel;

public class MonolithicVerifier {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ISatSolverFactory SOLVER_FACTORY;
    private final boolean useRankingFunctions;
    private final RegularModel problem;

    public MonolithicVerifier(RegularModel problem,
                              ISatSolverFactory SOLVER_FACTORY,
                              boolean useRankingFunctions) {
        this.problem = problem;
        this.SOLVER_FACTORY = SOLVER_FACTORY;
        this.useRankingFunctions = useRankingFunctions;
    }

    public Automata verify()
            throws Timer.TimeoutException {
        LOGGER.info("Constructing monolithic advice bits");

        int bound = problem.getMaxNumOfStatesTransducer() * problem.getMaxNumOfStatesTransducer() +
                problem.getMaxNumOfStatesAutomaton() * problem.getMaxNumOfStatesAutomaton();

        if (problem.getCloseInitStates()) {
            bound += problem.getMaxNumOfInitStatesAutomaton() *
                    problem.getMaxNumOfInitStatesAutomaton();
        }

        OldCounterExamples oldCEs = new OldCounterExamples();

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
                        checking.setB(problem.getB());
                        checking.setI(problem.getI());
                        checking.setT(problem.getT());
                        checking.setAutomataNumStates(numStateAutomata);
                        checking.setNumLetters(problem.getNumberOfLetters());
                        checking.setTransducerNumStates(numStateTransducer);
                        checking.setIndexToLabel(problem.getIndexToLabel());
                        checking.setOldCounterExamples(oldCEs);
                        checking.setup();
                        Automata invariant = checking.findNextSolution();
                        if (invariant != null) return invariant;
                    }
                }
            }
        }
        SatInvariantNotFoundException ex = new SatInvariantNotFoundException();
        ex.setMaxNumOfStatesTransducer(problem.getMaxNumOfStatesTransducer());
        ex.setMaxNumOfStatesAutomaton(problem.getMaxNumOfStatesAutomaton());
        ex.setMaxNumOfInitStatesAutomaton(problem.getMaxNumOfInitStatesAutomaton());
        ex.setMinNumOfStatesTransducer(problem.getMinNumOfStatesTransducer());
        ex.setMinNumOfStatesAutomaton(problem.getMinNumOfStatesAutomaton());
        ex.setMinNumOfInitStatesAutomaton(problem.getMinNumOfInitStatesAutomaton());
        throw ex;
    }
}

// vim: tabstop=4
