package learning;

import common.Timer;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import common.finiteautomata.language.InclusionCheckingImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LStarTest {
    // simple test for the Lstar algorithm
    public static void main(String[] args)
            throws Timer.TimeoutException {
        final Automata sol = new Automata(0, 4, 2);
        final Set<Integer> accept = new HashSet<Integer>();
        accept.add(3);
        sol.setAcceptingStateIds(accept);

        sol.addTrans(0, 0, 0);
        sol.addTrans(1, 0, 1);
        sol.addTrans(2, 0, 2);
        sol.addTrans(3, 0, 3);

        sol.addTrans(0, 1, 1);
        sol.addTrans(1, 1, 2);
        sol.addTrans(2, 1, 3);
        sol.addTrans(3, 1, 0);

        final Teacher teacher = new Teacher(2) {
            public boolean isAccepted(List<Integer> word) {
                System.out.println(word + " -> " + sol.accepts(word));
                return sol.accepts(word);
            }

            public boolean isCorrectLanguage(Automata hyp, CounterExample cex) {
                System.out.println();
                System.out.println("Hypothesis:");
                System.out.println(hyp);

                InclusionCheckingImpl ic = new InclusionCheckingImpl();

                List<Integer> ex = ic.findCounterExample(hyp, AutomataUtility.toCompleteDFA(sol));
                if (ex != null) {
                    System.out.println("negative cex: " + ex);
                    cex.addNegative(ex);
                    return false;
                }

                ex = ic.findCounterExample(sol, AutomataUtility.toCompleteDFA(hyp));
                if (ex != null) {
                    System.out.println("positive cex: " + ex);
                    cex.addPositive(ex);
                    return false;
                }

                return true;
            }
        };
        final LStarLearner lstar = new LStarLearner();
        lstar.setTeacher(teacher);
        lstar.solve();
    }
}
