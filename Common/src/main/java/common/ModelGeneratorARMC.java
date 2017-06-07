package common;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.State;
import visitor.RegularModel;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class ModelGeneratorARMC {
    final Map<Integer, String> indexToLabel;
    final RegularModel model;
    final String rootLabel;
    final String name;

    public ModelGeneratorARMC(RegularModel model, String name) {
        this.model = model;
        this.name = name;
        Map<Integer, String> map = model.getIndexToLabel();
        indexToLabel = new HashMap<>(map.size() + 1);
        // sanitize labels for OCaml
        for (Integer index : map.keySet()) {
            String label = map.get(index);
            if (Character.isDigit(label.charAt(0)))
                indexToLabel.put(index, getUnusedLabel(label, map));
            else
                indexToLabel.put(index, label);
        }
        indexToLabel.put(Automata.EPSILON_LABEL, getUnusedLabel("eps", map));
        rootLabel = getUnusedLabel("_", indexToLabel);
    }

    private String getString(Automata aut) {
        StringBuilder sb = new StringBuilder();
        State[] states = aut.getStates();
        sb.append("States");
        for (State s : states) {
            sb.append(" s").append(s.getId());
        }
        sb.append("\nFinal States");
        for (int sid : aut.getAcceptingStateIds()) {
            sb.append(" s").append(sid);
        }
        sb.append("\nTransitions " + rootLabel + "->s" + aut.getInitStateId());
        for (State state : states) {
            String s = "(s" + state.getId() + ")";
            for (int i : state.getOutgoingLabels()) {
                for (int t : state.getDestIds(i)) {
                    String label = indexToLabel.get(i);
                    if (label == null) throw new IllegalStateException("Invalid label index!");
                    sb.append(" ").append(label).append(s).append("->s").append(t);
                }
            }
        }
        return sb.toString();
    }

    private String getString(EdgeWeightedDigraph trans) {
        StringBuilder sb = new StringBuilder();
        sb.append("([");
        for (int i = 0; i < trans.getNumVertices(); i++) {
            sb.append("\"s").append(i).append("\";");
        }
        sb.setCharAt(sb.length() - 1, ']');
        sb.append(",\n[");
        for (int sid : trans.getDestVertices()) {
            sb.append("\"s").append(sid).append("\";");
        }
        sb.setCharAt(sb.length() - 1, ']');
        sb.append(",\n[(\"" + rootLabel + "\", [], \"" + rootLabel + "\", \"s" + trans.getSourceVertex() + "\")");
        for (DirectedEdge e : trans.getEdges()) {
            DirectedEdgeWithInputOutput edge = (DirectedEdgeWithInputOutput) e;
            sb.append("; (\"").append(indexToLabel.get(edge.getInput()))
                    .append("\", [\"s").append(edge.from()).append("\"], \"")
                    .append(indexToLabel.get(edge.getOutput())).append("\", \"s")
                    .append(edge.to()).append("\")");
        }
        sb.append("])");
        return sb.toString();
    }

    private String getUnusedLabel(String label, Map<Integer, String> indexToLabel) {
        StringBuilder sb = new StringBuilder(label);
        while (indexToLabel.containsValue(sb.toString())) sb.insert(0, '_');
        return sb.toString();
    }

    public void printTo(Writer out) throws IOException {
        StringBuilder sb1 = new StringBuilder(indexToLabel.size() * 15);
        StringBuilder sb2 = new StringBuilder(sb1.capacity());
        String dummyState = getUnusedLabel("p", indexToLabel);
        sb2.append("States " + dummyState + "\nFinal States " + dummyState + "\nTransitions");
        for (String label : indexToLabel.values()) {
            sb1.append(label).append(":1 ");
            sb2.append(' ').append(label).append("(").append(dummyState).append(")->").append(dummyState);
        }
        sb2.append(" " + rootLabel + "->" + dummyState);
        String nameS = name.replaceAll("-", "_").toLowerCase(); // ocaml convertion
        String sigma_str = "let sigma_str = \"" + sb1.toString() + rootLabel + ":0\" in\n";
        String init_str = "let init_str = \"" + getString(model.getI()) + "\" in\n";
        String bad_str = "let bad_str = \"" + getString(model.getB()) + "\" in\n";
        String sigstar_str = "let sigstar_str = \"" + sb2.toString() + "\" in\n";
        String tau_str = "let tau_str = " + getString(model.getT()) + " in\n";
        String preamble = "open Taml;;\n" + "open Dxn;;\n" + "open Colapsing_v3;;\n" + "let " + nameS + " _ = \n";
        String execution = "print_string \"Checking model " + name + "...\";\n" +
                "(* atrmc_strpres_bwcomp_bwcoll_allstpred sigma_str init_str tau_str bad_str [ bad_str ]; *)\n" +
                "(* atrmc_strpres_fwcomp_bwcoll_allstpred sigma_str init_str tau_str bad_str [ bad_str ]; *)\n" +
                "(* atrmc_strpres_fwcomp_bwcoll_allstpred sigma_str init_str tau_str bad_str [ sigstar_str ]; *)\n" +
                "atrmc_strpres_bwcomp_bwcoll_allstpred sigma_str init_str tau_str bad_str [ sigstar_str ];\n" +
                "();;\n" + nameS + "();;";
        out.write(preamble);
        out.write(sigma_str);
        out.write(init_str);
        out.write(tau_str);
        out.write(bad_str);
        out.write(sigstar_str);
        out.write(execution);
        out.close();
//        System.out.println(sigma_str);
//        System.out.println(init_str);
//        System.out.println(bad_str);
//        System.out.println(sigstar_str);
//        System.out.println(tau_str);
    }
}