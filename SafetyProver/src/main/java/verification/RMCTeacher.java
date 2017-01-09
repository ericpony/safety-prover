package verification;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;

import java.util.List;

public abstract class RMCTeacher extends Teacher {

    protected final Automata I;
    protected final Automata B;
    protected final EdgeWeightedDigraph T;

    public RMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T) {
        super(numLetters);
        this.I = I;
        this.B = B;
        this.T = T;
    }

    abstract public boolean isAccepted(List<Integer> word);

    abstract public boolean isCorrectLanguage(Automata sol, List<List<Integer>> posCEX, List<List<Integer>> negCEX);
}