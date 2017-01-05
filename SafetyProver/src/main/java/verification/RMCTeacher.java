package verification;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;

import java.util.List;

public abstract class RMCTeacher extends Teacher {
    protected final Automata I;
    protected final Automata F;
    protected final EdgeWeightedDigraph T;

    public RMCTeacher(int numLetters, Automata I, Automata F, EdgeWeightedDigraph T) {
        super(numLetters);
        this.I = I;
        this.F = F;
        this.T = T;
    }

    abstract public boolean isAccepted(List<Integer> word);

    abstract public boolean isCorrectLanguage(Automata sol, List<List<Integer>> posCEX, List<List<Integer>> negCEX);
}