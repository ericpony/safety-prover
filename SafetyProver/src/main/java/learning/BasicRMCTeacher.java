package learning;

import common.Tuple;
import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import main.LOGGER;
import verification.FiniteStateSets;
import verification.InductivenessChecking;
import verification.SubsetChecking;

import java.util.List;

public class BasicRMCTeacher extends RMCTeacher {

    //public static final Logger LOGGER = LogManager.getLogger();
    protected Automata relevantStates;
    protected FiniteStateSets finiteStates;

    public BasicRMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T) {
        super(numLetters, I, B, T);
        relevantStates = AutomataConverter.getComplement(B);
        finiteStates = new FiniteStateSets(I, T, B);
    }

    public boolean isAccepted(List<Integer> word) {
        boolean isReachable = finiteStates.isReachable(word);
        boolean isBad = getBadStates().accepts(word);
        if (isReachable && isBad) {
            LOGGER.debug("membership query: " + word);
            throw new NoInvariantException(word);
        }
        boolean accepted = isReachable && !isBad;
        LOGGER.debug("membership query: " + word + " -> " + (accepted ? "accepted" : "rejected"));
        return accepted;
    }

    public boolean isCorrectLanguage(Automata hyp, CounterExample cex) {
        LOGGER.debug("found hypothesis, size " + hyp.getStates().length);
        List<Integer> ex;
        SubsetChecking sc;

        // first test: are initial states contained?
        sc = new SubsetChecking(getInitialStates(), hyp);
        ex = sc.check();
        if (ex != null) {
            LOGGER.debug("An initial configuration is not contained in hypothesis: " + ex);
            cex.addPositive(ex);
            return false;
        }

        // second test: are bad configurations excluded?
        Automata lang = VerificationUltility.getIntersection(hyp, getBadStates());
        ex = AutomataConverter.getSomeShortestWord(lang);
        if (ex != null) {
//            if (finiteStates.isReachable(ex))
//                throw new NoInvariantException(ex);
            LOGGER.debug("A bad configuration is contained in hypothesis: " + ex);
            cex.addNegative(ex);
            return false;
        }
        // reachable?

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
        Tuple<List<Integer>> xy = ic.check();
        if (xy != null) {
            LOGGER.debug("Hypothesis is not inductive: " + xy);
            if (finiteStates.isReachable(xy.x)) {
                LOGGER.debug(" => Configuration " + xy.y + " should be included due to inductiveness.");
                cex.addPositive(xy.y);
            } else {
                LOGGER.debug(" => Configuration " + xy.x + " is unreachable and should be excluded.");
                cex.addNegative(xy.x);
            }
            return false;
        }
        return true;
    }
}
