package verification;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;

import java.util.List;

class BasicRMCTeacher extends RMCTeacher {
    protected Automata relevantStates;
    protected FiniteStateSets finiteStates;
    protected int explicitExplorationDepth;

    BasicRMCTeacher(int numLetters, Automata I, Automata F, EdgeWeightedDigraph T, FiniteStateSets finiteStateSets, int explicitExplorationDepth) {
        super(numLetters, I, F, T);
        this.relevantStates = AutomataConverter.getComplement(F);
        this.finiteStates = finiteStateSets;
        this.explicitExplorationDepth = explicitExplorationDepth;
    }

    public boolean isAccepted(List<Integer> word) {
        return finiteStates.isReachable(word);
    }

    public boolean isCorrectLanguage(Automata hyp,
                                     List<List<Integer>> posCEX,
                                     List<List<Integer>> negCEX) {
        LOGGER.debug("found hypothesis, size " + hyp.getStates().length);

        // first test: are initial states contained?
        SubsetChecking s1 = new SubsetChecking(I, hyp);
        List<Integer> w = s1.check();
        if (w != null) {
            LOGGER.debug("I not contained: " + w);
            posCEX.add(w);
            return false;
        }

        // second test: are concrete unreachable configurations excluded?
        for (int l = 0; l <= explicitExplorationDepth; ++l) {
            SubsetChecking s2 =
                    new SubsetChecking(AutomataConverter.getWordAutomaton(hyp, l),
                            finiteStates.getReachableStateAutomaton(l));
            List<Integer> w2 = s2.check();
            if (w2 != null) {
                LOGGER.debug("not reachable: " + w2);
                negCEX.add(w2);
                return false;
            }
//                for (List<Integer> w3 : AutomataConverter.getWords(hyp, l)) {
//                    if (!finiteStates.isReachable(w3)) {
//                        LOGGER.debug("not reachable: " + w3);
//                        negCEX.add(w3);
//                        return false;
//                    }
//                }
        }

        // third test: is the invariant inductive?
        InductivenessChecking s2 = new InductivenessChecking(hyp, relevantStates,
                T, getNumLetters());
        List<List<Integer>> xy = s2.check();
        if (xy != null) {
            LOGGER.debug("inductiveness failed: " + xy);
            if (finiteStates.isReachable(xy.get(0)))
                posCEX.add(xy.get(1));
            else
                negCEX.add(xy.get(0));
            return false;
        }
        return true;
    }

    /**
     * Try to find a word that should not be accepted
     */
    private List<Integer> findStringNotAcceptedBy(Automata aut, int maxLen) {
        for (int l = explicitExplorationDepth + 1; l <= maxLen; ++l) {
            SubsetChecking s3 =
                    new SubsetChecking(AutomataConverter.getWordAutomaton(aut, l),
                            finiteStates.getReachableStateAutomaton(l));
            List<Integer> w = s3.check();
            if (w != null) return w;
        }
        return null;
    }
}
