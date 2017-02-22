package learning;

import common.Timer;
import common.finiteautomata.Automata;

import java.util.List;

public abstract class Teacher {

    private final int numLetters;

    public Teacher(int numLetters) {
        this.numLetters = numLetters;
    }

    abstract public boolean isAccepted(List<Integer> word)
            throws Timer.TimeoutException;

    abstract public boolean isCorrectLanguage(Automata sol, CounterExample cex)
            throws Timer.TimeoutException;

    public final int getNumLetters() {
        return numLetters;
    }
}