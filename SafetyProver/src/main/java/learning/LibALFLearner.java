package learning;

import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import de.libalf.BasicAutomaton;
import de.libalf.Knowledgebase;
import de.libalf.LearningAlgorithm;
import de.libalf.LibALFFactory;
import de.libalf.jni.JNIFactory;

import java.util.Arrays;
import java.util.List;


public class LibALFLearner extends Learner {

    final LibALFFactory.Algorithm algorithmType;
    LearningAlgorithm algorithm;
    Knowledgebase knowledgebase;

    public LibALFLearner(LibALFFactory.Algorithm algorithmType) {
        this.algorithmType = algorithmType;
    }

    protected void setup() {
        /**
         * A LibALFFactory called "factory" is obtained.
         */
        final LibALFFactory factory = JNIFactory.STATIC;

        /**
         * A Knowledgebase called "knowledgebase" is created in the factory.
         */
        knowledgebase = factory.createKnowledgebase();

        /**
         * A LearningAlgorithm is created from the Factory by passing three
         * information. 1. The Algorithm to be used - Here we use the RPNI
         * Algorithm. 2. The Knowledgebase - "base". 3. The Size of the Alphabet
         * - "AlphabetSize".
         */
        algorithm = factory.createLearningAlgorithm(algorithmType, knowledgebase, getNumLetters());
    }

    public Automata solve() {
        final Teacher teacher = getTeacher();

        /**
         * An BasicAutomaton automaton is created and initialized to null. The
         * automaton is used to store the a conjecture which is marked correct
         * by the user.
         */
        BasicAutomaton automaton = null;

        /**
         * The method "advance" is iterated in a loop which checks if there is
         * enough information to formulate a conjecture. If there was no enough
         * information for the same, the method creates a list of words that are
         * to be classified by the user. The classification is then added to the
         * knowledgebase. This information may either give enough knowledge to
         * the algorithm to produce a conjecture or may produce more queries to
         * be resolved. This is identified in the next iteration of "advance".
         *
         * On the otherhand, When the algorithm has enough information to
         * compute a conjecture, it is presented to the user to classify it as
         * correct or incorrect. If marked correct, the conjecture is stored in
         * a variable named "automaton" which is later used to construct the
         * ".dot" output. If the conjecture is rejected, the algorithm requires a
         * counter example from the user which will be used to construct further
         * queries to formulate the conjecture.
         */
        do {
            BasicAutomaton conjecture = (BasicAutomaton) algorithm.advance();
            List<int[]> queries = knowledgebase.get_queries();
            if (!queries.isEmpty()) {
                for (int[] query : queries) {
                    boolean answer = teacher.isAccepted(
                            Arrays.asList(ints2Integers(query)));
                    knowledgebase.add_knowledge(query, answer);
                }
            } else {
                CounterExample cex = new CounterExample();
                if (teacher.isCorrectLanguage(AutomataConverter.Alf2SLRP(conjecture), cex)) {
                    automaton = conjecture;
                } else {
                    List<Integer> ex = cex.get();
                    algorithm.add_counterexample(
                            integers2ints(ex.toArray(new Integer[ex.size()])));
                }
            }
        } while (automaton == null);
        // Present result
        //System.out.println("\nResult:\n\n" + automaton.toDot());
        return AutomataConverter.Alf2SLRP(automaton);
    }

    private Integer[] ints2Integers(int[] ints) {
        Integer[] integers = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            integers[i] = ints[i];
        }
        return integers;
    }

    private int[] integers2ints(Integer[] integers) {
        int[] ints = new int[integers.length];
        for (int i = 0; i < integers.length; i++) {
            ints[i] = integers[i];
        }
        return ints;
    }
}
