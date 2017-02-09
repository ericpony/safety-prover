package main;

import common.Utility;
import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import common.finiteautomata.language.InclusionCheckingImpl;
import de.libalf.LibALFFactory;
import encoding.ISatSolverFactory;
import encoding.SatSolver;
import grammar.Yylex;
import grammar.parser;
import learning.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import verification.MonolithicVerifier;
import visitor.AllVisitorImpl;
import visitor.RegularModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class Main {
    //    private static final Logger LOGGER = LogManager.getLogger();
    public enum Mode {
        HOMEBREW,
        FIXEDPOINT,
        ANGLUIN,
        SAT_CEGAR
    }

    private static final ISatSolverFactory SOLVER_FACTORY =
            //MinisatSolver.FACTORY;    // Minisat
            SatSolver.FACTORY;          // Sat4j
    //LingelingSolver.FACTORY;          // Lingeling

    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No input, doing nothing.");
            return;
        }
        File in = new File(args[0]);
        File[] modelFiles;
        if (in.isFile()) {
            modelFiles = new File[]{in};
        } else if (in.isDirectory()) {
            modelFiles = in.listFiles();
        } else {
            System.err.println("Input file is not a valid model.");
            return;
        }

        final int timeout = 60;  // 60 seconds
        final Mode mode = Mode.FIXEDPOINT;
//        LOGGER.setLevel(Level.OFF);

        Timer.setMilliTimeout(timeout * 1000);
        for (File modelFile : modelFiles) {
            if (!modelFile.isFile()) continue;
            char c = modelFile.getName().charAt(0);
            if (c == '_' || c == '.') continue;
            System.out.println("Checking " + modelFile.getName() + "...");
            try {
                long elapsedTime = checkModel(modelFile.getAbsolutePath(), mode);
                int millisec = (int) (elapsedTime / 1e6);
                System.out.println("Elapsed time for " + modelFile.getName() + " : " + millisec + " ms.");
            } catch (Timer.TimeoutException e) {
                System.out.println("Timeout proving " + modelFile.getName() + " within " + timeout + " seconds.");
            } catch (SatInvariantNotFoundException e) {
                System.out.println(e);
            }
        }
    }

    static long checkModel(String filePath, Mode mode)
            throws Timer.TimeoutException {
        RegularModel problem = parse(filePath);
        String fileName = new File(filePath).getName();
        if (problem.getLogLevel() <= 0)
            Configurator.setRootLevel(Level.ERROR);
        else if (problem.getLogLevel() == 1)
            Configurator.setRootLevel(Level.INFO);
        else
            Configurator.setRootLevel(Level.ALL);

        writeInputProblem(problem);

        determize(problem);

        Map<Integer, String> indexToLabel = problem.getIndexToLabel();
        NoInvariantException.setIndexToLabelMapping(indexToLabel);
        Automata invariant = null;
        //if(problem.getPrecomputedInv()) {
        Timer.start();
        switch (mode) {
            case HOMEBREW:
                Learner learner = new LStarLearner();
                Teacher teacher = new BasicRMCTeacher(problem.getNumberOfLetters(),
                        problem.getI(), problem.getB(), problem.getT());
                invariant = MonolithicLearning.inferWith(learner, teacher);
                break;
            case ANGLUIN:
                learner = new LibALFLearner(LibALFFactory.Algorithm.ANGLUIN);
                teacher = new BasicRMCTeacher(problem.getNumberOfLetters(),
                        problem.getI(), problem.getB(), problem.getT());
                invariant = MonolithicLearning.inferWith(learner, teacher);
                break;
            case FIXEDPOINT:
                invariant = findReachabilitySet(problem.getI(), problem.getT(), indexToLabel);
                break;
            case SAT_CEGAR:
                MonolithicVerifier verifier = new MonolithicVerifier
                        (problem, SOLVER_FACTORY, problem.getUseRankingFunctions());
                invariant = verifier.verify();
                break;
        }
        Timer.stop();
        if (invariant == null) {
            System.err.print("\nTimeout in proving safety for " + fileName + "\n");
        } else {
            invariant = AutomataConverter.toMinimalDFA(invariant);
            invariant = AutomataConverter.pruneUnreachableStates(invariant);
            System.err.print("\nSuccessfully found an invariant for " + fileName + "\n");
            LOGGER.debug("VERDICT: Bad configurations are not reachable from any " +
                    (problem.getCloseInitStates() ? "reachable" : "initial") + " configuration.");
            LOGGER.debug("\n");
            LOGGER.debug("// Configurations visited by the program are contained in");
            LOGGER.debug(invariant.prettyPrint("Invariant", indexToLabel));
        }
        return Timer.getElapsedTime();
    }

    static Automata findReachabilitySet(Automata I, EdgeWeightedDigraph T, Map<Integer, String> indexToLabel)
            throws Timer.TimeoutException {
        Automata reachable = I;
        Automata newConfig = I;
        LOGGER.debug(I.prettyPrint("Initial", indexToLabel));
        while (true) {
            Automata post = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getImage(newConfig, T));

            post = AutomataConverter.toMinimalDFA(post);
            post = AutomataConverter.pruneUnreachableStates(post);
            LOGGER.debug(post.prettyPrint("Post", indexToLabel));

            Timer.tick();

//            newConfig = AutomataConverter.minimiseAcyclic(
//                    VerificationUltility.getDifference(post, reachable));
            newConfig = post;

            Timer.tick();

            LOGGER.debug("reachable " + reachable.getStates().length + ", new " + newConfig.getStates().length);

            if (new InclusionCheckingImpl().isSubSetOf(
                    newConfig, AutomataConverter.toCompleteDFA(reachable)))
                break;

            Timer.tick();

            reachable = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getUnion(reachable, post));

            Timer.tick();
        }
        return reachable;
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
        EdgeWeightedDigraph T = problem.getT();

        if (!VerificationUltility.isDFA(T, problem.getNumberOfLetters())) {
            T = VerificationUltility.toDFA(problem.getT(), problem.getNumberOfLetters());
            problem.setT(T);
        }

        Automata I = problem.getI();
        if (!I.isDFA()) {
            I = AutomataConverter.toDFA(I);
            problem.setI(I);
        }

        Automata B = problem.getB();
        if (!B.isDFA()) {
            B = AutomataConverter.toDFA(B);
            problem.setB(B);
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
