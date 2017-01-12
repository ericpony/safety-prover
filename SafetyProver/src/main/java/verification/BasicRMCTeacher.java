package verification;

import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;

import java.util.List;

class NoInvariantException extends RuntimeException {
    public NoInvariantException() {
        super("Invariant does not exist!");
    }
}

public class BasicRMCTeacher extends RMCTeacher {

    //public static final Logger LOGGER = LogManager.getLogger();
    protected Automata relevantStates;
    protected FiniteStateSets finiteStates;
    protected int explicitExplorationDepth;

    public BasicRMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T, int explicitExplorationDepth) {
        super(numLetters, I, B, T);
        this.relevantStates = AutomataConverter.getComplement(B);
        this.explicitExplorationDepth = explicitExplorationDepth;
        this.finiteStates = new FiniteStateSets(I, T, B);
    }

    public boolean isAccepted(List<Integer> word) {
        boolean isReachable = finiteStates.isReachable(word);
        boolean isBad = getBadStates().accepts(word);
        LOGGER.debug("membership query: " + word);
        if (isReachable && isBad) throw new NoInvariantException();
        return !isBad;
        //return !getBadStates().accepts(word);
    }

    public boolean isCorrectLanguage(Automata hyp,
                                     List<List<Integer>> posCEX,
                                     List<List<Integer>> negCEX) {
        LOGGER.debug("found hypothesis, size " + hyp.getStates().length);
        List<Integer> cex;
        SubsetChecking sc;

        // first test: are initial states contained?
        sc = new SubsetChecking(getInitialStates(), hyp);
        cex = sc.check();
        if (cex != null) {
            LOGGER.debug("An initial configuration is not contained in hypothesis: " + cex);
            posCEX.add(cex);
            return false;
        }

        // second test: are bad configurations excluded?
        Automata lang = VerificationUltility.getIntersection(hyp, getBadStates());
        cex = AutomataConverter.getSomeShortestWord(lang);
        if (cex != null) {
            LOGGER.debug("A bad configuration is contained in hypothesis: " + cex);
            negCEX.add(cex);
            return false;
        }

        /*
        // third test: are concrete unreachable configurations excluded?
        for (int l = 0; l <= explicitExplorationDepth; ++l) {
            sc = new SubsetChecking(
                    AutomataConverter.getWordAutomaton(hyp, l),
                    finiteStates.getReachableStateAutomaton(l));
            cex = sc.check();
            if (cex != null) {
                LOGGER.debug("An unreachable configuration is contained in hypothesis: " + cex);
                negCEX.add(cex);
                return false;
            }
        }
        */

        // fourth test: is the invariant inductive?
        InductivenessChecking ic = new InductivenessChecking(hyp, relevantStates, getTransition(), getNumLetters());
        List<List<Integer>> xy = ic.check();
        if (xy != null) {
            LOGGER.debug("Hypothesis is not inductive: " + xy);
            if (finiteStates.isReachable(xy.get(0))) {
                LOGGER.debug(" => Configuration " + xy.get(1) + " should be included due to inductiveness.");
                posCEX.add(xy.get(1));
            } else {
                LOGGER.debug(" => Configuration " + xy.get(0) + " is unreachable and should be excluded.");
                negCEX.add(xy.get(0));
            }
            return false;
        }
        return true;
    }
}
