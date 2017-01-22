package learning;

import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import common.finiteautomata.language.InclusionCheckingImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LStarTest {
    // simple test for the Lstar algorithm
    public static void main(String[] args) {
        final Automata sol = new Automata(0, 4, 2);
        final Set<Integer> accept = new HashSet<Integer>();
        accept.add(3);
        sol.setAcceptingStates(accept);

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

            public boolean isCorrectLanguage(Automata hyp,
                                             List<List<Integer>> posCEX,
                                             List<List<Integer>> negCEX) {
                System.out.println();
                System.out.println("Hypothesis:");
                System.out.println(hyp);

                InclusionCheckingImpl ic = new InclusionCheckingImpl();

                List<Integer> cex = ic.findCounterExample(hyp, AutomataConverter.toCompleteDFA(sol));
                if (cex != null) {
                    System.out.println("negative cex: " + cex);
                    negCEX.add(cex);
                    return false;
                }

                cex = ic.findCounterExample(sol, AutomataConverter.toCompleteDFA(hyp));
                if (cex != null) {
                    System.out.println("positive cex: " + cex);
                    posCEX.add(cex);
                    return false;
                }

                return true;
            }
        };
        final LStar lstar = new LStar();
        lstar.setTeacher(teacher);
        lstar.solve();
    }
}
