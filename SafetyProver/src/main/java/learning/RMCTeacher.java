package learning;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;

public abstract class RMCTeacher extends Teacher {

    private final Automata I;
    private final Automata B;
    private final EdgeWeightedDigraph T;

    public RMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T) {
        super(numLetters);
        this.I = I;
        this.B = B;
        this.T = T;
    }

    public Automata getInitialStates() {
        return I;
    }

    public Automata getBadStates() {
        return B;
    }

    public EdgeWeightedDigraph getTransition() {
        return T;
    }
}