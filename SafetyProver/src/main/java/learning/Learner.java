package learning;

import common.Timer;
import common.finiteautomata.Automata;

public abstract class Learner {
    private Teacher teacher;

    protected abstract void setup()
            throws Timer.TimeoutException;

    public abstract Automata solve()
            throws Timer.TimeoutException;

    public int getNumLetters() {
        return teacher.getNumLetters();
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
