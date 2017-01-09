package verification;

import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;

import java.util.List;

public class BasicRMCTeacher extends RMCTeacher {

    protected Automata relevantStates;
    protected FiniteStateSets finiteStates;
    protected int explicitExplorationDepth;

    public BasicRMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T, FiniteStateSets finiteStateSets, int explicitExplorationDepth) {
        super(numLetters, I, B, T);
        this.relevantStates = AutomataConverter.getComplement(B);
        this.finiteStates = finiteStateSets;
        this.explicitExplorationDepth = explicitExplorationDepth;
    }

    public boolean isAccepted(List<Integer> word) {
        return !B.accepts(word) && finiteStates.isReachable(word);
    }

    public boolean isCorrectLanguage(Automata hyp,
                                     List<List<Integer>> posCEX,
                                     List<List<Integer>> negCEX) {
        LOGGER.debug("found hypothesis, size " + hyp.getStates().length);

        List<Integer> cex;
        SubsetChecking sc;
        // first test: are initial states contained?
        sc = new SubsetChecking(I, hyp);
        cex = sc.check();
        if (cex != null) {
            LOGGER.debug("A configuration in I is not contained in hypothesis: " + cex);
            posCEX.add(cex);
            return false;
        }

        // second test: are bad configurations excluded?
        Automata lang = VerificationUltility.getIntersection(hyp, B);
        cex = AutomataConverter.getSomeShortestWord(lang);
        if (cex != null) {
            negCEX.add(cex);
            return false;
        }

        // third test: are concrete unreachable configurations excluded?
        for (int l = 0; l <= explicitExplorationDepth; ++l) {
            sc = new SubsetChecking(
                    AutomataConverter.getWordAutomaton(hyp, l),
                    finiteStates.getReachableStateAutomaton(l));
            cex = sc.check();
            if (cex != null) {
                LOGGER.debug("A configuration should not be contained in hypothesis: " + cex);
                negCEX.add(cex);
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

        // fourth test: is the invariant inductive?
        InductivenessChecking ic = new InductivenessChecking(hyp, relevantStates, T, getNumLetters());
        List<List<Integer>> xy = ic.check();
        if (xy != null) {
            LOGGER.debug("Hypothesis is not inductive: " + xy);
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
