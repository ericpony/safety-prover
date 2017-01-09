package main;

import common.Utility;
import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import common.finiteautomata.lstar.LStar;
import encoding.ISatSolverFactory;
import encoding.SatSolver;
import grammar.Yylex;
import grammar.parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import verification.*;
import visitor.AllVisitorImpl;
import visitor.RegularModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ISatSolverFactory SOLVER_FACTORY =
            //MinisatSolver.FACTORY;            // Minisat
            SatSolver.FACTORY;                // Sat4j
    //LingelingSolver.FACTORY;          // Lingeling

    private final static boolean verifySolutions = false;

    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No input, doing nothing");
            return;
        }

        String fileName = args[0];
        RegularModel problem = parse(fileName);

        if (problem.getLogLevel() <= 0)
            Configurator.setRootLevel(Level.ERROR);
        else if (problem.getLogLevel() == 1)
            Configurator.setRootLevel(Level.INFO);
        else
            Configurator.setRootLevel(Level.ALL);

        writeInputProblem(problem);

        determize(problem);

        FiniteStateSets finiteStates = new FiniteStateSets(problem.getNumberOfLetters(),
                problem.getI(), problem.getB(),
                problem.getT(),
                problem.getLabelToIndex());

        //verifyFiniteInstances(problem, problem.getExplicitChecksUntilLength());

        if (problem.getPrecomputedInv()) {
            Teacher teacher = new BasicRMCTeacher(problem.getNumberOfLetters(),
                    problem.getI(), problem.getB(), problem.getT(),
                    finiteStates, 5);
            Automata invariant = LStar.inferWith(teacher);
            Map<Integer, String> indexToLabel = problem.getIndexToLabel();
            System.err.print("\nL-star successfully found an invariant!\n");
            System.out.println("VERDICT: Bad configurations are not reachable from every " +
                    (problem.getCloseInitStates() ? "reachable" : "initial") + " configuration.");
            System.out.println();
            System.out.println("// Configurations visited in the game are contained in");
            System.out.println(invariant.prettyPrint("Invariant", indexToLabel));

        } else if (false && problem.getCloseInitStates() && !problem.getAlwaysMonolithic()) {
            IncrementalVerifier verifier =
                    new IncrementalVerifier(problem, SOLVER_FACTORY,
                            problem.getUseRankingFunctions(),
                            problem.getPrecomputedInv(),
                            verifySolutions);
            verifier.setup();
            verifier.verify();
        } else {
            MonolithicVerifier verifier = new MonolithicVerifier
                    (problem, SOLVER_FACTORY, problem.getUseRankingFunctions());
            verifier.verify();
        }
    }

    public static RegularModel parse(String fileName) {
        RegularModel problem;
        try {
            problem = parseFromReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return problem;
    }

    private static RegularModel parseFromReader(Reader reader) {
        parser p;
        Yylex l = new Yylex(reader);
        p = new parser(l);

        try {
            grammar.Absyn.ModelRule parse_tree = p.pModelRule();

            RegularModel problem = new RegularModel();
            parse_tree.accept(new AllVisitorImpl(), problem);

            LOGGER.info("Parse Succesful!");
            return problem;
        } catch (Throwable e) {

            String error = ("At line " + String.valueOf(l.line_num()) + ", near \"" + l.buff() + "\" :\n") +
                    ("     " + e.getMessage());
            throw new RuntimeException(error);
        }
    }

    /**
     * Determinizes all components of a problem
     *
     * @param[in,out] problem  The problem to determinize
     */
    private static void determize(RegularModel problem) {
        EdgeWeightedDigraph player2 = problem.getT();
        if (!VerificationUltility.isDFA(player2, problem.getNumberOfLetters())) {
            player2 = VerificationUltility.toDFA(problem.getT(), problem.getNumberOfLetters());
            problem.setT(player2);
        }

        Automata I0 = problem.getI();
        if (!I0.isDFA()) {
            I0 = AutomataConverter.toDFA(I0);
            problem.setI(I0);
        }

        Automata F = problem.getB();
        if (!F.isDFA()) {
            F = AutomataConverter.toDFA(F);
            problem.setB(F);
        }
    }

    public static void verifyFiniteInstances(RegularModel problem, int sizeBound) {
        final FiniteStateSets finiteStates =
                new FiniteStateSets(problem.getNumberOfLetters(),
                        problem.getI(), problem.getB(),
                        problem.getT(),
                        problem.getLabelToIndex());
        for (int s = 0; s <= sizeBound; ++s) {
            System.out.println("Verifying system instance for length " + s + " ... ");
//	    finiteStates.verifyInstance(s, problem.getCloseInitStates());
            finiteStates.verifyInstanceSymbolically(s, problem.getCloseInitStates());
        }
    }

    public static void writeInputProblem(RegularModel problem) {
        try {
            Utility.writeOut(Utility.toDot(problem.getI(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonI0.dot");
            Utility.writeOut(Utility.toDot(problem.getB(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonF.dot");
//            Utility.writeOut(Utility.toDot(problem.getPlayer1(),
//                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP1.dot");
            Utility.writeOut(Utility.toDot(problem.getT(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP2.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

// vim: tabstop=4
