package verification;

import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            List<List<Integer>> initialStates = AutomataConverter.getWords(I, wordLen);
            Queue<List<Integer>> todo = new ArrayDeque<List<Integer>>(initialStates);
            reachable = new HashSet<List<Integer>>(initialStates);

            while (!todo.isEmpty()) {
                List<Integer> next = todo.poll();
                List<List<Integer>> post = AutomataConverter.getWords(
                        AutomataConverter.getImage(next, T, numLetters),
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

            //final Automata complementF = AutomataConverter.getComplement(F);
            reachable = AutomataConverter.getWordAutomaton(I, wordLen);

            // do one initial transition
            reachable = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getUnion(
                            reachable,
                            VerificationUltility.getImage(reachable, T)));
            Automata newConfigurations = reachable;

            while (true) {
                // check whether any new configurations exist
                if (AutomataConverter.getWords(newConfigurations, wordLen, 1).isEmpty()) break;

                LOGGER.debug("reachable " + reachable.getStates().length +
                        ", new " + newConfigurations.getStates().length);

                Automata post = AutomataConverter.minimiseAcyclic(
                        VerificationUltility.getImage(newConfigurations, T));

                newConfigurations = AutomataConverter.minimiseAcyclic(
                        VerificationUltility.getIntersectionLazily(post, reachable, true));

                reachable = AutomataConverter.minimiseAcyclic(
                        VerificationUltility.getUnion(reachable, post));
            }
            if (AutomataConverter.getSomeWord(VerificationUltility.getIntersection(reachable, B)) != null)
                throw new NoInvariantException();

            reachableStateAutomata.put(wordLen, reachable);
        }
        return reachable;
    }

    public boolean isReachable(List<Integer> word) {
        return getReachableStateAutomaton(word.size()).accepts(word);
    }
}