package main;

import common.TimbukPrinter;
import common.Timer;
import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import common.finiteautomata.language.InclusionCheckingImpl;
import de.libalf.LibALFFactory;
import encoding.ISatSolverFactory;
import encoding.SatSolver;
import grammar.Yylex;
import grammar.parser;
import learning.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import verification.MonolithicVerifier;
import visitor.AllVisitorImpl;
import visitor.RegularModel;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public enum Mode {
        HOMEBREW,
        FIXEDPOINT,
        ANGLUIN,
        SAT_CEGAR
    }

    public enum Task {
        CONVERT_FOR_ARTMC,
        CHECK_SAFETY
    }

    private static final ISatSolverFactory SOLVER_FACTORY =
            //MinisatSolver.FACTORY;    // Minisat
            SatSolver.FACTORY;          // Sat4j
    //LingelingSolver.FACTORY;          // Lingeling

    /// directory name of the output
    final static String OUTPUT_DIR = "output";
    final static int timeout = 60;  // 60 seconds
    static Mode mode = Mode.HOMEBREW;
    static Task task = Task.CHECK_SAFETY;

    public static void main(String[] args) {
        Configurator.setRootLevel(Level.OFF);
        if (args.length < 1) {
            System.err.println("No input, doing nothing.");
            return;
        }
        ArrayList<File> modelFiles = new ArrayList<>();
        for (String arg : args) {
            if (arg.charAt(0) == '-') {
                String option = arg.substring(arg.charAt(1) == '-' ? 2 : 1);
                switch (option) {
                    case "convert":
                        task = Task.CONVERT_FOR_ARTMC;
                        break;
                    case "debug":
                        Configurator.setRootLevel(Level.DEBUG);
                        break;
                    case "verbose":
                        Configurator.setRootLevel(Level.INFO);
                        break;
                    case "sat":
                        mode = Mode.SAT_CEGAR;
                        break;
                    case "fixpoint":
                        mode = Mode.FIXEDPOINT;
                        break;
                }
            } else {
                File in = new File(arg);
                if (in.isFile()) {
                    modelFiles.add(in);
                } else if (in.isDirectory()) {
                    modelFiles.addAll(Arrays.asList(in.listFiles()));
                } else {
                    System.err.println("Input file is not a valid model.");
                    return;
                }
            }
        }
        if (modelFiles.size() == 0) return;

        Timer.setMilliTimeout(timeout * 1000);
        for (File modelFile : modelFiles) {
            if (!modelFile.isFile()) continue;
            String modelFileName = modelFile.getName();
            char c = modelFileName.charAt(0);
            if (c == '_' || c == '.') continue;
            modelFile = modelFile.getAbsoluteFile();
            switch (task) {
                case CHECK_SAFETY:
                    System.out.println("Checking " + modelFileName + "...");
                    RegularModel model = parse(modelFile.getPath());
                    try {
                        long elapsedTime = checkModel(model, modelFileName, mode);
                        int millisec = (int) (elapsedTime / 1e6);
                        System.out.println("Elapsed time for " + modelFileName + " : " + millisec + " ms.");
                    } catch (Timer.TimeoutException e) {
                        System.out.println("Timeout proving " + modelFileName + " within " + timeout + " seconds.");
                    } catch (SatInvariantNotFoundException e) {
                        System.out.println(e);
                    }
                    break;
                case CONVERT_FOR_ARTMC:
                    String modelName = modelFileName.split("\\.")[0];
                    String outputFileName = modelName + ".ml";
                    System.out.print("\nConverting from " + modelFileName + " to " + outputFileName + "...");
                    try {
                        Writer out = new BufferedWriter(new FileWriter(
                                modelFile.getParent() + File.separatorChar + outputFileName
                        ), 10000);
                        TimbukPrinter printer =
                                new TimbukPrinter(parse(modelFile.getPath()), modelName);
                        printer.printTo(out);
                        System.out.println("done.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }


    static long checkModel(RegularModel model, String name, Mode mode)
            throws Timer.TimeoutException {
//        if (model.getLogLevel() <= 0)
//            Configurator.setRootLevel(Level.ERROR);
//        else if (model.getLogLevel() == 1)
//            Configurator.setRootLevel(Level.INFO);
//        else
//            Configurator.setRootLevel(Level.ALL);

        determize(model);

        Timer.start();
        Map<Integer, String> indexToLabel = model.getIndexToLabel();
        NoInvariantException.setIndexToLabelMapping(indexToLabel);
        Automata invariant = null;
        //if(problem.getPrecomputedInv()) {
        switch (mode) {
            case HOMEBREW:
                Learner learner = new LStarLearner();
                Teacher teacher = new BasicRMCTeacher(model.getNumberOfLetters(),
                        model.getI(), model.getB(), model.getT());
                invariant = MonolithicLearning.inferWith(learner, teacher);
                break;
            case ANGLUIN:
                learner = new LibALFLearner(LibALFFactory.Algorithm.ANGLUIN);
                teacher = new BasicRMCTeacher(model.getNumberOfLetters(),
                        model.getI(), model.getB(), model.getT());
                invariant = MonolithicLearning.inferWith(learner, teacher);
                break;
            case FIXEDPOINT:
                invariant = findReachabilitySet(model.getI(), model.getT(), indexToLabel);
                break;
            case SAT_CEGAR:
                MonolithicVerifier verifier = new MonolithicVerifier
                        (model, SOLVER_FACTORY, model.getUseRankingFunctions());
                invariant = verifier.verify();
                break;
        }
        Timer.stop();
        if (invariant == null) {
            System.err.print("\nTimeout in proving safety for " + name + "\n");
        } else {
            System.err.println("\nSuccessfully found an invariant for " + name);
            System.err.println("#S : " + invariant.getNumStates() + ", #T : " + invariant.getNumTransitions() + "\n");

            invariant = AutomataUtility.pruneUnreachableStates(AutomataUtility.toDFA(invariant));
            invariant = AutomataUtility.toMinimalDFA(AutomataUtility.toCompleteDFA(invariant));
            LOGGER.debug("VERDICT: Bad configurations are not reachable from any " +
                    (model.getCloseInitStates() ? "reachable" : "initial") + " configuration.");
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
            Automata post = AutomataUtility.minimiseAcyclic(
                    VerificationUtility.getImage(newConfig, T));

            //LOGGER.debug(post.prettyPrint("Post", indexToLabel));

            Timer.tick();

            newConfig = AutomataUtility.minimiseAcyclic(
                    AutomataUtility.getDifference(post, reachable));

            Timer.tick();

            LOGGER.debug("reachable " + reachable.getStates().length + ", new " + newConfig.getStates().length);

            if (new InclusionCheckingImpl().isSubSetOf(
                    newConfig, AutomataUtility.toCompleteDFA(reachable)))
                break;

            Timer.tick();

            reachable = AutomataUtility.minimiseAcyclic(
                    AutomataUtility.getUnion(reachable, post));

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

        if (!VerificationUtility.isDFA(T, problem.getNumberOfLetters())) {
            T = VerificationUtility.toDFA(problem.getT(), problem.getNumberOfLetters());
            problem.setT(T);
        }

        Automata I = problem.getI();
        if (!I.isDFA()) {
            I = AutomataUtility.toDFA(I);
            problem.setI(I);
        }

        Automata B = problem.getB();
        if (!B.isDFA()) {
            B = AutomataUtility.toDFA(B);
            problem.setB(B);
        }
    }
}
// vim: tabstop=4
