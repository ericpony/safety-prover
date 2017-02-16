package learning;

import common.Tuple;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import verification.FiniteStateSets;
import verification.InductivenessChecking;
import verification.SubsetChecking;

import java.util.List;

public class BasicRMCTeacher extends RMCTeacher {

    public static final Logger LOGGER = LogManager.getLogger();
    protected Automata relevantStates;
    protected FiniteStateSets finiteStates;

    public BasicRMCTeacher(int numLetters, Automata I, Automata B, EdgeWeightedDigraph T) {
        super(numLetters, I, B, T);
        relevantStates = AutomataUtility.getComplement(B);
        finiteStates = new FiniteStateSets(I, T, B);
    }

    public boolean isAccepted(List<Integer> word) {
        boolean isReachable = finiteStates.isReachable(word);
        boolean isBad = getBadStates().accepts(word);
        String labeledWord = LOGGER.isDebugEnabled() ? NoInvariantException.getLabeledWord(word) : null;
        if (isReachable && isBad) {
            LOGGER.debug("membership query: " + labeledWord);
            throw new NoInvariantException(word, getInitialStates(), getTransition());
        }
        boolean accepted = isReachable && !isBad;
        LOGGER.debug("membership query: " + labeledWord + " -> " + (accepted ? "accepted" : "rejected"));
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
            if (LOGGER.isDebugEnabled()) {
                String word = NoInvariantException.getLabeledWord(ex);
                LOGGER.debug("An initial configuration is not contained in hypothesis: " + word);
            }
            cex.addPositive(ex);
            return false;
        }

        // second test: are bad configurations excluded?
        Automata lang = AutomataUtility.getIntersection(hyp, getBadStates());
        ex = AutomataUtility.findSomeShortestWord(lang);
        if (ex != null) {
            if (LOGGER.isDebugEnabled()) {
                String word = NoInvariantException.getLabeledWord(ex);
                LOGGER.debug("A bad configuration is contained in hypothesis: " + word);
            }
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Hypothesis is not inductive: ");
                String x = NoInvariantException.getLabeledWord(xy.x);
                String y = NoInvariantException.getLabeledWord(xy.y);
                LOGGER.debug(x + " => " + y);
            }
            if (finiteStates.isReachable(xy.x)) {
                LOGGER.debug("* The second configuration should be included in the hypothesis.");
                cex.addPositive(xy.y);
            } else {
                LOGGER.debug("* The first configuration should be excluded from the hypothesis.");
                cex.addNegative(xy.x);
            }
            return false;
        }
        return true;
    }
}
