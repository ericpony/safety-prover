package common;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.State;
import visitor.RegularModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;


public class DOTPrinter {

    // used to denote the point marking beginning of the arrow denoting initial
    // node for dot output
    static final String DOT_INIT_NODE_NUM = "\"6b19f55c2212\"";

    public static void saveModel(RegularModel problem, String outputDir) {
        try {
            DOTPrinter.writeOut(DOTPrinter.getString(problem.getI(),
                    problem.getIndexToLabel()), outputDir + File.separator + "init.dot");
            DOTPrinter.writeOut(DOTPrinter.getString(problem.getB(),
                    problem.getIndexToLabel()), outputDir + File.separator + "bad.dot");
            DOTPrinter.writeOut(DOTPrinter.getString(problem.getT(),
                    problem.getIndexToLabel()), outputDir + File.separator + "trans.dot");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getString(Automata automata, Map<Integer, String> indexToLabel) {
        final String NEW_LINE = "\n";
        final String SPACE = " ";
        StringBuilder sb = new StringBuilder();
        sb.append("digraph finite_state_machine {");
        sb.append(NEW_LINE);
        sb.append("rankdir=LR;");
        sb.append(NEW_LINE);
        sb.append("size=\"8,5\"");
        sb.append(NEW_LINE);
        sb.append("node [shape = doublecircle]; ");

        for (int accepting : automata.getAcceptingStateIds()) {
            sb.append(accepting);
            sb.append(SPACE);
        }
        sb.append(";");
        sb.append(NEW_LINE);
        sb.append("node [shape = circle];");
        sb.append(NEW_LINE);
        for (State state : automata.getStates()) {
            for (int i = Automata.EPSILON_LABEL; i < automata.getNumLabels(); i++) {
                String label = (i == Automata.EPSILON_LABEL) ? "" : indexToLabel.get(i);
                Set<Integer> nexts = state.getDestIds(i);
                for (Integer next : nexts) {
                    sb.append(state.getId() + " -> " + next + " [ label = \"" + label + "\" ];");
                    sb.append(NEW_LINE);
                }
            }
        }
        // denote the initial state (special value for the beginning of input arrow)
        sb.append(DOT_INIT_NODE_NUM + "[shape = point];");
        sb.append(NEW_LINE);
        sb.append(DOT_INIT_NODE_NUM + " -> " + automata.getInitStateId() + ";");
        sb.append(NEW_LINE);
        sb.append("}");
        return sb.toString();
    }

    public static String getString(EdgeWeightedDigraph transducer, Map<Integer, String> indexToLabel) {
        final String NEW_LINE = "\n";
        final String SPACE = " ";
        StringBuilder sb = new StringBuilder();

        sb.append("digraph finite_state_machine {");
        sb.append(NEW_LINE);
        sb.append("rankdir=LR;");
        sb.append(NEW_LINE);
        sb.append("size=\"8,5\"");
        sb.append(NEW_LINE);
        sb.append("node [shape = doublecircle]; ");
        for (int accepting : transducer.getDestVertices()) {
            sb.append(accepting);
            sb.append(SPACE);
        }
        sb.append(";");
        sb.append(NEW_LINE);
        sb.append("node [shape = circle];");
        sb.append(NEW_LINE);

        for (DirectedEdge edge : transducer.getEdges()) {
            DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
            String edgeLab;
            if ((tempEdge.getInput() == common.finiteautomata.Automata.EPSILON_LABEL) &&
                    (tempEdge.getOutput() == common.finiteautomata.Automata.EPSILON_LABEL)) {
                edgeLab = "";
            } else {
                String inputLabel = indexToLabel.get(tempEdge.getInput());
                String outputLabel = indexToLabel.get(tempEdge.getOutput());
                edgeLab = inputLabel + "/" + outputLabel;
            }

            sb.append(tempEdge.from() + " -> " + tempEdge.to() + " [ label = \"" + edgeLab + "\" ];");
            sb.append(NEW_LINE);
        }

        // denote the initial state (special value for the beginning of input arrow)
        sb.append(DOT_INIT_NODE_NUM + " [shape = point ];");
        sb.append(NEW_LINE);
        sb.append(DOT_INIT_NODE_NUM + " -> " + transducer.getSourceVertex() + ";");
        sb.append(NEW_LINE);

        sb.append("}");

        return sb.toString();
    }

    public static void writeOut(String content, String fileName)
            throws FileNotFoundException {
        File file = new File(fileName);
        file.getAbsoluteFile().getParentFile().mkdirs();

        PrintWriter writer;
        writer = new PrintWriter(file);
        writer.write(content);
        writer.close();
    }
}

// vim: tabstop=4
