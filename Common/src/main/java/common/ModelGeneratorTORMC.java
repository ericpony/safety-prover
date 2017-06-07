package common;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.State;
import visitor.RegularModel;

import java.io.IOException;
import java.io.Writer;

public class ModelGeneratorTORMC {
    final RegularModel model;
    final String name;

    public ModelGeneratorTORMC(RegularModel model, String name) {
        System.err.println("Num of labels: " + model.getNumberOfLetters());
        this.model = model;
        this.name = name;
    }

    private String getString(Automata aut) {
        StringBuilder sb = new StringBuilder();
        State[] states = aut.getStates();
        sb.append("\n\tautomaton *a = auto_new_empty(1); register uint4 i; "
                + "uint4 states[" + states.length + "]; uint1 label[1];\n");
        sb.append("\tfor(i = 0; i < " + states.length
                + "; i++){" + "auto_add_new_state(a, states + i);}\n");
        sb.append("\tauto_add_new_i_state(a, states["
                + aut.getInitStateId() + "]);\n");
        for (int i : aut.getAcceptingStateIds()) {
            sb.append("\tauto_mark_accepting_state(a, states[" + i + "]);\n");
        }
        for (State state : states) {
            int s = state.getId();
            for (int label : state.getOutgoingLabels()) {
                sb.append("\tlabel[0] = " + label + " & 0xff;\n");
                for (int t : state.getDestIds(label)) {
                    sb.append("\tauto_add_new_transition(a, states[" + s + "], states[" + t + "], "
                            + (label == Automata.EPSILON_LABEL ? 0 : 1) + ", label);\n");
                }
            }
        }
        sb.append("\tauto_determinize(a); auto_minimize(a); return a;\n");
        return sb.toString();
    }

    private String getString(EdgeWeightedDigraph trans) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\tautomaton *a = auto_new_empty(2); register uint4 i; "
                + "uint4 states[" + trans.getNumVertices() + "]; uint1 label[2];\n");
        sb.append("\tfor(i = 0; i < " + trans.getNumVertices()
                + "; i++){" + "auto_add_new_state(a, states + i);}\n");
        sb.append("\tauto_add_new_i_state(a, states["
                + trans.getSourceVertex() + "]);\n");
        for (int i : trans.getDestVertices()) {
            sb.append("\tauto_mark_accepting_state(a, states[" + i + "]);\n");
        }
        for (DirectedEdge e : trans.getEdges()) {
            DirectedEdgeWithInputOutput edge = (DirectedEdgeWithInputOutput) e;
            int i = edge.getInput();
            int o = edge.getOutput();
            if ((i != o) && (i == Automata.EPSILON_LABEL || o == Automata.EPSILON_LABEL)) {
                throw new RuntimeException("Illegal transition!");
            }
            sb.append("\tlabel[0] = " + i + " & 0xff;\n");
            sb.append("\tlabel[1] = " + o + " & 0xff;\n");
            sb.append("\tauto_add_new_transition(a, states["
                    + edge.from() + "], states[" + edge.to() + "], "
                    + (o == Automata.EPSILON_LABEL ? 0 : 1) + ", label);\n");
        }
        sb.append("\treturn a;\n");
        return sb.toString();
    }

    private String getIdentityRel() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\tautomaton *a = auto_new_empty(2); register uint4 i; "
                + "\tuint4 states[1]; uint1 label[2];\n"
                + "\tauto_add_new_state(a, states);\n"
                + "\tauto_add_new_i_state(a, states[0]);\n"
                + "\tauto_mark_accepting_state(a, states[0]);\n");
        for (Integer label : model.getIndexToLabel().keySet()) {
            sb.append("\tlabel[0] = " + label + " & 0xff;\n");
            sb.append("\tlabel[1] = label[0];\n");
            sb.append("\tauto_add_new_transition(a, states[0], states[0], "
                    + (label == Automata.EPSILON_LABEL ? 0 : 1) + ", label);\n");
        }
        sb.append("\treturn a;\n");
        return sb.toString();
    }

    private String getTransRel() {
        StringBuilder sb = new StringBuilder();
        sb.append("automaton *a = identity_rel();\n"
                + "\tautomaton *b = transition();\n"
                + "\tautomaton *r = auto_union(a, b);\n"
                + "\tauto_free(a); auto_free(b);\n"
                + "\tauto_determinize(r);\n"
                + "\tauto_minimize(r);\n"
                + "\treturn r;\n");
        return sb.toString();
    }

    public void printTo(Writer out) throws IOException {
        StringBuilder sb = new StringBuilder("#include<stdio.h>\n"
                + "#include<ctype.h>\n"
                + "#include\"auto-io-dots.h\"\n"
                + "#include\"auto-io-print.h\"\n"
                + "#include\"lash-diag.h\"\n"
                + "#include\"lash-auto.h\"\n"
                + "#include\"composition.h\"\n"
                + "#include\"extrapolate.h\"\n"
                + "#include\"increase.h\"\n"
                + "#include\"auto.h\"\n"
                + "#include\"datastruct.h\"\n"
                + "#include\"resource.h\"\n"
                + "#include\"auto-serialize.h\"\n"
                + "#include\"auto-difference.h\"\n"
                + "#include\"increase.h\"\n");
        sb.append("automaton *init_state(){ " + getString(model.getI()) + "}\n");
        sb.append("automaton *bad_state(){ " + getString(model.getB()) + "}\n");
        sb.append("automaton *transition(){ " + getString(model.getT()) + "}\n");
        sb.append("automaton *identity_rel(){ " + getIdentityRel() + " }\n");
        sb.append("automaton *trans_rel() {" + getTransRel() + "}\n");
        sb.append("list_auto * fonction(automaton * a1, automaton * a2, "
                + "list_auto * current, uint4 j, uint4 k, uint4 l)\n"
                + "{\n"
                + "    uint4 nb_comb, i;\n"
                + "    list_auto * inter,*inter2;\n"
                + "    automaton * a3,*a4,*a;\n"
                + "\n"
                + "    if (current -> next == NULL) {\n"
                + "        inter = resr__new_object(list_auto);\n"
                + "        inter -> next = NULL;\n"
                + "        a = current -> element_auto;\n"
                + "        inter -> element_auto = compose_minimal(a, a2);\n"
                + "        current -> next = inter;\n"
                + "        current = inter;\n"
                + "    } else {\n"
                + "        current = current -> next;\n"
                + "    }\n"
                + "    return current;\n}\n"
                + "int main() {\n"
                + "    int res = lash_init(); /* needed to initialize lash */\n"
                + "    automaton *init = init_state();\n"
                + "    automaton *trans = trans_rel();\n"
                + "    automaton *bad = bad_state();\n"
                + "\n"
                + "    list_auto *list = resr__new_object(list_auto);\n"
                + "    list->element_auto = init;\n"
                + "    list->next = NULL;\n"
                + "    \n"
                + "    fprintf(stdout, \"Initial state size: %d\\n\", auto_nb_states(init));\n"
                + "    fprintf(stdout, \"Transducer size: %d\\n\", auto_nb_states(trans));\n"
                + "    \n"
                + "    auto_iterate *structure_axel = initial_data(init, trans, list, fonction, NULL, 0, 0);\n"
                + "    lash_perror(\"initiate\");\n"
                + "    automaton *result = iterate_sequence(structure_axel);\n"
                + "    printf(\"\\n\");\n"
                + "    lash_perror(\"iterate\");\n"
                + "\n"
                + "/* TODO: Check inductiveness of result */\n"
                + "\n"
                + "    res = auto_empty_intersection(result, bad);\n"
                + "\n"
                + "    if (res != -1) {\n"
                + "        if (res) {\n"
                + "            printf(\"\\n==> Safety is proved!\\n\\n\");\n"
                + "        }\n"
                + "        else {\n"
                + "            printf(\"\\n==> Nothing proved.\\n\\n\");\n"
                + "        }\n"
                + "    }\n"
                + "    else {\n"
                + "        lash_perror(\"error\");\n"
                + "    }\n"
                + "    auto_print(result);\n"
                + "    /* LASH statistics. */\n"
                + "    printf(\"\\n### statistics ###\\n\");\n"
                + "    printf(\"residual memory : %lu byte(s).\\n\", lash_get_mem_usage());\n"
                + "    printf(\"max memory      : %lu byte(s).\\n\", lash_get_max_mem_usage());\n"
                + "    printf(\"### end ###\\n\");\n"
                + "    auto_free(bad);\n"
                + "    auto_free(result);\n"
                + "    auto_iterate_free(structure_axel);\n"
                + "    lash_end();\n"
                + "    return 0;\n"
                + "}");
        out.write(sb.toString());
        out.close();
    }
}