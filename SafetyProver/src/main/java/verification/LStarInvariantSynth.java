package verification;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;


public class LStarInvariantSynth {

    private Teacher teacher;

    public LStarInvariantSynth(int numLetters,
                               Automata I0,
                               Automata F,
                               EdgeWeightedDigraph player2,
                               FiniteStateSets finiteStates,
                               int explicitExplorationDepth) {
        this.teacher = new BasicRMCTeacher(numLetters, I0, F, player2, finiteStates, explicitExplorationDepth);
    }

    public void setTeacher(Teacher t) {
        this.teacher = t;
    }
}
