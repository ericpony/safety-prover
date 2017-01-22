package learning;

import common.finiteautomata.Automata;

public abstract class Learner {
    private Teacher teacher;

    protected abstract void setup();

    public abstract Automata solve();

    public Teacher getTeacher() {
        return teacher;
    }

    public int getNumLetters() {
        return teacher.getNumLetters();
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        setup();
    }
}
