package verification;


import common.Tuple;
import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;

import java.util.List;

public class InductivenessChecking {
    private Automata aut;
    private EdgeWeightedDigraph trans;
    private int numLetters;

    /**
     * Make sure that I, label starting from 1
     */
    public InductivenessChecking(Automata aut,
                                 EdgeWeightedDigraph digraph,
                                 int numLetters) {
        this.aut = aut;
        this.trans = digraph;
        this.numLetters = numLetters;
    }

    public Tuple<List<Integer>> check() {
        final Automata img = VerificationUtility.getImage(aut, trans);
        final Automata bad = AutomataUtility.getDifference(img, aut);
        final List<Integer> point = AutomataUtility.findSomeShortestWord(bad);

        if (point == null) {
            return null;
        } else {
            final Automata preImage =
                    VerificationUtility.getPreImage(point, trans, numLetters);
            final List<Integer> prePoint =
                    AutomataUtility.findSomeWord(
                            AutomataUtility.getIntersectionLazily(preImage, aut));
            return new Tuple<>(prePoint, point);
        }
    }
}
