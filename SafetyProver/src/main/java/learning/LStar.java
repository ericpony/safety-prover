package learning;

import common.finiteautomata.Automata;
import common.finiteautomata.State;

import java.util.*;

public class LStar extends Learner {

    private Automata solution;
    private Node classTree;
    private List<List<Integer>> distWords = new ArrayList<List<Integer>>();
    private static final List<Integer> emptyWord = new ArrayList<Integer>();

    protected void setup() {
        final Teacher teacher = getTeacher();
        final int numLetters = getNumLetters();
        final boolean initAccepting = teacher.isAccepted(emptyWord);
        final Automata hypAut = new Automata(0, 1, numLetters);
        final Set<Integer> accept = new HashSet<Integer>();
        solution = null;

        if (initAccepting) accept.add(0);

        hypAut.setAcceptingStates(accept);

        for (int l = 0; l < numLetters; ++l)
            hypAut.addTrans(0, l, 0);

        CounterExample cex = new CounterExample();

        if (teacher.isCorrectLanguage(hypAut, cex)) {
            // finished already
            solution = hypAut;
        } else {
            classTree = new Node(
                    emptyWord,
                    new Node(initAccepting ? cex.get() : emptyWord, null, null),
                    new Node(initAccepting ? emptyWord : cex.get(), null, null));
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public Automata solve() {
        if (solution != null) return solution;

        Teacher teacher = getTeacher();
        if (teacher == null) throw new IllegalStateException("Must set teacher before calling setup().");
        CounterExample cex = new CounterExample();
        final List<List<Integer>> accessWords = new ArrayList<List<Integer>>();
        classTree.collectLeafWords(accessWords);

        Automata hypAut = extractAutomaton(accessWords);
        boolean cont = !teacher.isCorrectLanguage(hypAut, cex);

        while (cont) {
            final List<Integer> ex = cex.get();
            // analyze the counterexampe
            int currentState = hypAut.getInitState();
            final List<Integer> prefix = new ArrayList<Integer>();
            Node lastSifted = null;

            int j = 0;
            while (j <= ex.size()) {
                final Node sifted = classTree.sift(prefix);

                if (!sifted.word.equals(accessWords.get(currentState))) {
                    // have found the point where the automaton goes wrong;
                    // add a new state

                    prefix.remove(prefix.size() - 1);

                    final Node[] distNode = new Node[1];
                    final boolean[] swapped = new boolean[1];

                    classTree.findDistinguishingPoint
                            (sifted.word, accessWords.get(currentState), distNode, swapped);

                    final Node nodeA = new Node(lastSifted.word, null, null);
                    final Node nodeB = new Node(prefix, null, null);

                    List<Integer> bestDistWord = new ArrayList<Integer>();
                    bestDistWord.add(ex.get(j - 1));
                    bestDistWord.addAll(distNode[0].word);

//		    System.out.println("new distinguishing word: " +
//				       bestDistWord);

                    // check whether we can find a shorter distinguishing word
                    for (List<Integer> oldDist : distWords) {
                        final List<Integer> oldDistPrefix = new ArrayList<Integer>();

                        for (int i = 0;
                             i < oldDist.size() && i < bestDistWord.size() - 1;
                             ++i) {
                            oldDistPrefix.add(oldDist.get(i));
                            if (oldDistPrefix.equals(bestDistWord))
                                continue;

                            final List<Integer> a = new ArrayList<Integer>(nodeA.word);
                            final List<Integer> b = new ArrayList<Integer>(nodeB.word);

                            a.addAll(oldDistPrefix);
                            b.addAll(oldDistPrefix);

                            if (teacher.isAccepted(a) != teacher.isAccepted(b)) {
                                bestDistWord = oldDistPrefix;

//				System.out.println
//				    ("better distinguishing word: " +
//				     bestDistWord);
                            }
                        }
                    }

                    if (!distWords.contains(bestDistWord))
                        distWords.add(bestDistWord);

                    lastSifted.word = bestDistWord;

                    final List<Integer> a = new ArrayList<Integer>(nodeA.word);
                    a.addAll(bestDistWord);
                    if (teacher.isAccepted(a)) {
                        lastSifted.right = nodeA;
                        lastSifted.left = nodeB;
                    } else {
                        lastSifted.right = nodeB;
                        lastSifted.left = nodeA;
                    }

                    break;
                }
                //if (j >= cex.size()) break;

                lastSifted = sifted;

                final int nextChar = ex.get(j++);
                final State s = hypAut.getStates()[currentState];
                final Set<Integer> nextStates = s.getDest(nextChar);
                assert (nextStates.size() == 1);

                currentState = nextStates.iterator().next();

                prefix.add(nextChar);
            }

            accessWords.clear();
            classTree.collectLeafWords(accessWords);

            hypAut = extractAutomaton(accessWords);

            /*
            if (lastHypAut != null && lastHypAut.getNumStates() == hypAut.getNumStates()) {
                System.err.println("error: L-star algorithm learned the same automaton twice!");
                System.err.println("Automaton 1: " + lastHypAut);
                System.err.println("Automaton 2: " + hypAut);
                throw new RuntimeException("L-star algorithm got stuck");
            }
            lastHypAut = hypAut;
            */
            if (!cex.isPositive() == hypAut.accepts(ex)) {
                // the counterexample has not been eliminated yet, try again
            } else {
                cex.reset();
                cont = !teacher.isCorrectLanguage(hypAut, cex);
            }
        }
        solution = hypAut;
        return solution;
    }

    ////////////////////////////////////////////////////////////////////////////

    private Automata extractAutomaton(List<List<Integer>> accessWords) {
        final Map<List<Integer>, Integer> accessIndex = new HashMap<List<Integer>, Integer>();
        final int numLetters = getNumLetters();

        int i = 0;
        for (List<Integer> w : accessWords)
            accessIndex.put(w, i++);

        final Automata result = new Automata(accessIndex.get(emptyWord),
                accessWords.size(), numLetters);
        final Set<Integer> accept = new HashSet<Integer>();

        // add transitions and accepting states
        for (int x = 0; x < 2; ++x) {
            final Iterator<Node> it =
                    ((x == 0) ? classTree.left : classTree.right).enumLeaves();
            while (it.hasNext()) {
                final Node leaf = it.next();
                final int index = accessIndex.get(leaf.word);

                final List<Integer> extWord = new ArrayList<Integer>();
                extWord.addAll(leaf.word);

                for (int l = 0; l < numLetters; ++l) {
                    extWord.add(l);
                    result.addTrans(index, l, accessIndex.get(classTree.sift(extWord).word));
                    extWord.remove(extWord.size() - 1);
                }

                if (x == 1)
                    accept.add(index);
            }
        }

        result.setAcceptingStates(accept);
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////

    private class Node {
        public List<Integer> word;
        public Node left;
        public Node right;

        public Node(List<Integer> word,
                    Node left,
                    Node right) {
            this.word = word;
            this.left = left;
            this.right = right;
        }

        public Node sift(List<Integer> w) {
            if (left == null && right == null) {
                return this;
            } else {
                final int oldSize = w.size();
                w.addAll(this.word);

                final boolean f = getTeacher().isAccepted(w);

                while (w.size() > oldSize)
                    w.remove(w.size() - 1);

                return (f ? right : left).sift(w);
            }
        }

        public int getLeafNum() {
            if (left == null && right == null)
                return 1;
            else
                return left.getLeafNum() + right.getLeafNum();
        }

        public void collectLeafWords(List<List<Integer>> words) {
            if (left == null && right == null) {
                words.add(word);
            } else {
                left.collectLeafWords(words);
                right.collectLeafWords(words);
            }
        }

        public int findDistinguishingPoint(List<Integer> word1,
                                           List<Integer> word2,
                                           Node[] node,
                                           boolean[] swapped) {
            if (left == null && right == null) {
                if (word.equals(word1))
                    return 1;
                else if (word.equals(word2))
                    return 2;
                else
                    return 0;
            } else {
                final int leftRes =
                        left.findDistinguishingPoint(word1, word2, node, swapped);
                if (leftRes == 3) // found
                    return 3;

                final int rightRes =
                        right.findDistinguishingPoint(word1, word2, node, swapped);

                if (leftRes == 1 && rightRes == 2) {
                    node[0] = this;
                    swapped[0] = false;
                    return 3;
                }

                if (leftRes == 2 && rightRes == 1) {
                    node[0] = this;
                    swapped[0] = true;
                    return 3;
                }

                assert (leftRes == 0 || rightRes == 0);

                return leftRes + rightRes;
            }
        }

        public Iterator<Node> enumLeaves() {
            final Stack<Node> rem = new Stack<Node>();
            rem.push(this);

            return new Iterator<Node>() {
                private final Stack<Node> remaining = rem;

                public boolean hasNext() {
                    return !remaining.empty();
                }

                public Node next() {
                    Node res = remaining.pop();
                    while (res.left != null) {
                        remaining.push(res.right);
                        res = res.left;
                    }
                    return res;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
