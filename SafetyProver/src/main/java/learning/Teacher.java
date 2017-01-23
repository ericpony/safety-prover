package learning;

import common.finiteautomata.Automata;

import java.util.List;

public abstract class Teacher {

    private final int numLetters;

    public Teacher(int numLetters) {
        this.numLetters = numLetters;
    }

    abstract public boolean isAccepted(List<Integer> word);

    abstract public boolean isCorrectLanguage(Automata sol, CounterExample cex);

    public final int getNumLetters() {
        return numLetters;
    }
}