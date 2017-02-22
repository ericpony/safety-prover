package learning;

import common.Timer;
import common.finiteautomata.Automata;

import java.util.List;

public class MonolithicLearning {
    private Learner learner;
    private Teacher teacher;
    private int numMembershipQueries = 0;
    private int numEquivalenceQueries = 0;
    private int maxCounterexampleSize = 0;
    private int maxQuerySize = 0;

    public MonolithicLearning(Learner learner, Teacher teacher) {
        this.learner = learner;
        this.teacher = teacher;
    }

    public Automata infer()
            throws Timer.TimeoutException {
        learner.setTeacher(new TeacherDecorator());
        learner.setup();
        return learner.solve();
    }

    public int getNumMembershipQueries() {
        return numMembershipQueries;
    }

    public int getNumEquivalenceQueries() {
        return numEquivalenceQueries;
    }

    public int getMaxQuerySize() {
        return maxQuerySize;
    }

    public int getMaxCounterexampleSize() {
        return maxCounterexampleSize;
    }

    class TeacherDecorator extends Teacher {

        public TeacherDecorator() {
            super(teacher.getNumLetters());
        }

        public boolean isAccepted(List<Integer> word)
                throws Timer.TimeoutException {
            numMembershipQueries++;
            maxQuerySize = Math.max(word.size(), maxQuerySize);
            return teacher.isAccepted(word);
        }

        public boolean isCorrectLanguage(Automata sol, CounterExample cex)
                throws Timer.TimeoutException {
            numEquivalenceQueries++;
            boolean isCorrect = teacher.isCorrectLanguage(sol, cex);
            maxCounterexampleSize =
                    Math.max(cex.get().size(), maxCounterexampleSize);
            return isCorrect;
        }
    }
}
