package verification;

import common.finiteautomata.Automata;

import java.util.List;

public abstract class Teacher {

    private int numLetters;

    public Teacher(int numLetters) {
        this.numLetters = numLetters;
    }

    abstract public boolean isAccepted(List<Integer> word);

    abstract public boolean isCorrectLanguage(Automata sol, List<List<Integer>> posCEX, List<List<Integer>> negCEX);

    public final int getNumLetters() {
        return numLetters;
    }
}