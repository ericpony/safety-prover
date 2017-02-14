package verification;

import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import learning.NoInvariantException;
import main.LOGGER;

import java.util.*;

public class FiniteStateSets {

    //private static final Logger LOGGER = LogManager.getLogger();
    private final Automata I, B;
    private final EdgeWeightedDigraph T;
    private final Map<Integer, Set<List<Integer>>> reachableStates =
            new HashMap<Integer, Set<List<Integer>>>();
    private final Map<Integer, Automata> reachableStateAutomata =
            new HashMap<Integer, Automata>();

    public FiniteStateSets(Automata I, EdgeWeightedDigraph T, Automata B) {
        this.I = I;
        this.T = T;
        this.B = B;
    }

    public Set<List<Integer>> getReachableStates(int wordLen, int numLetters) {
        Set<List<Integer>> reachable = reachableStates.get(wordLen);
        if (reachable == null) {
            // Compute initial states for the given word length
            List<List<Integer>> initialStates = AutomataUtility.getWords(I, wordLen);
            Queue<List<Integer>> todo = new ArrayDeque<List<Integer>>(initialStates);
            reachable = new HashSet<List<Integer>>(initialStates);

            while (!todo.isEmpty()) {
                List<Integer> next = todo.poll();
                List<List<Integer>> post = AutomataUtility.getWords(
                        VerificationUtility.getImage(next, T, numLetters),
                        wordLen);
                for (List<Integer> w : post) {
                    if (!reachable.contains(w)) {
                        reachable.add(w);
                        todo.add(w);
                    }
                }
            }
            LOGGER.debug("" + reachable.size() + " reachable words");
            reachableStates.put(wordLen, reachable);
        }
        return reachable;
    }

    public Automata getReachableStateAutomaton(int wordLen) {
        Automata reachable = reachableStateAutomata.get(wordLen);
        if (reachable == null) {
            LOGGER.debug("computing automaton describing reachable configurations of length " + wordLen);

            // initial configurations are those in I with length wordLen
            reachable = AutomataUtility.getWordAutomaton(I, wordLen);

            // do one transition from the initial configurations
            reachable = AutomataUtility.minimiseAcyclic(
                    AutomataUtility.getUnion(
                            reachable,
                            VerificationUtility.getImage(reachable, T)));
            Automata newConfig = reachable;

            while (true) {
                // check whether any new configurations exist
                if (AutomataUtility.getWords(newConfig, wordLen, 1).isEmpty()) break;

                LOGGER.debug("reachable " + reachable.getStates().length +
                        ", new " + newConfig.getStates().length);

                Automata post = AutomataUtility.minimiseAcyclic(
                        VerificationUtility.getImage(newConfig, T));

                newConfig = AutomataUtility.minimiseAcyclic(
                        AutomataUtility.getDifference(post, reachable));

                reachable = AutomataUtility.minimiseAcyclic(
                        AutomataUtility.getUnion(reachable, post));
            }

            List<Integer> cex = AutomataUtility.findSomeWord(AutomataUtility.getIntersection(reachable, B));
            if (cex != null) throw new NoInvariantException(cex, I, T);

            reachableStateAutomata.put(wordLen, reachable);
        }
        return reachable;
    }

    public boolean isReachable(List<Integer> word) {
        return getReachableStateAutomaton(word.size()).accepts(word);
    }
}