package learning;

import common.finiteautomata.Automata;

public class MonolithicLearning {
    public static Automata inferWith(Learner learner, Teacher teacher) {
        learner.setTeacher(teacher);
        return learner.solve();
    }
}
