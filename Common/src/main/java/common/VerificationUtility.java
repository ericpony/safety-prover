package common;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataUtility;
import common.finiteautomata.State;

import java.util.*;

public class VerificationUtility {
    public static EdgeWeightedDigraph simplifyNFA(EdgeWeightedDigraph graph) {
        final int V = graph.getNumVertices();
        final boolean[] reachingAccept = new boolean[V];
        Arrays.fill(reachingAccept, false);

        for (int s : graph.getDestVertices())
            reachingAccept[s] = true;

        boolean changed = true;
        while (changed) {
            changed = false;

            for (int i = 0; i < V; ++i)
                if (!reachingAccept[i])
                    for (DirectedEdge edge : graph.getIncidentEdges(i))
                        if (reachingAccept[edge.to()]) {
                            reachingAccept[i] = true;
                            changed = true;
                        }
        }

        final Map<Integer, Integer> relevantStates = new HashMap<Integer, Integer>();

        for (int i : graph.computeReachableVertices(graph.getSourceVertex())) {
            if (reachingAccept[i])
                relevantStates.put(i, relevantStates.size());
        }

        // we need at least an initial state
        if (relevantStates.isEmpty())
            relevantStates.put(graph.getSourceVertex(), 0);

        EdgeWeightedDigraph dfa = new EdgeWeightedDigraph(relevantStates.size());
        dfa.setSourceVertex(relevantStates.get(graph.getSourceVertex()));

        for (int i = 0; i < V; ++i)
            if (relevantStates.containsKey(i)) {
                final int newFrom = relevantStates.get(i);
                for (DirectedEdge edge : graph.getIncidentEdges(i))
                    if (relevantStates.containsKey(edge.to())) {
                        final int newTo = relevantStates.get(edge.to());

                        DirectedEdgeWithInputOutput ioEdge = (DirectedEdgeWithInputOutput) edge;
                        dfa.addEdge(new DirectedEdgeWithInputOutput(newFrom, newTo,
                                edge.weight(),
                                ioEdge.getInput(),
                                ioEdge.getOutput()));
                    }
            }

        //compute accepting states
        Set<Integer> acceptingDFA = new HashSet<Integer>();
        for (int s : graph.getDestVertices())
            if (relevantStates.containsKey(s))
                acceptingDFA.add(relevantStates.get(s));
        dfa.setDestVertices(acceptingDFA);

        return dfa;
    }

    public static EdgeWeightedDigraph toDFA2(EdgeWeightedDigraph graph, int numLabels) {
        final Set<Integer> allStatesDFA = new HashSet<Integer>();
        final Map<BitSet, Integer> mapStates = new HashMap<BitSet, Integer>();
        final Stack<BitSet> workingStates = new Stack<BitSet>();
        final BitSet initSet = new BitSet();

        initSet.set(graph.getSourceVertex());
        getEpsilonClosure(graph, initSet);
        workingStates.push(initSet);

        //state 0 will be the init state in new DFA
        final int initInDFA = 0;
        mapStates.put(initSet, initInDFA);
        allStatesDFA.add(initInDFA);

        final List<DirectedEdgeWithInputOutput> edges = new ArrayList<DirectedEdgeWithInputOutput>();
        final BitSet[] targetStates = new BitSet[numLabels * numLabels];
        for (int i = 0; i < targetStates.length; ++i)
            targetStates[i] = new BitSet();

        while (!workingStates.isEmpty()) {
            BitSet statesInNFA = workingStates.pop();
            int stateInDFA = mapStates.get(statesInNFA);

            // compute the target states for the various labels
            for (int s = statesInNFA.nextSetBit(0); s >= 0; s = statesInNFA.nextSetBit(s + 1)) {
                for (DirectedEdge edge : graph.getIncidentEdges(s)) {
                    DirectedEdgeWithInputOutput ioEdge = (DirectedEdgeWithInputOutput) edge;
                    targetStates[ioEdge.getInput() * numLabels +
                            ioEdge.getOutput()].set(edge.to());
                }
            }

            // check which target states are actually reachable
            for (int input = 0; input < numLabels; input++)
                for (int output = 0; output < numLabels; output++) {
                    final int index = input * numLabels + output;
                    if (!targetStates[index].isEmpty()) {
                        BitSet destsInNFA = targetStates[index];
                        getEpsilonClosure(graph, destsInNFA);
                        targetStates[index] = new BitSet();

                        Integer destInDFA = mapStates.get(destsInNFA);

                        if (destInDFA == null) {
                            destInDFA = mapStates.size();
                            mapStates.put(destsInNFA, destInDFA);
                            allStatesDFA.add(destInDFA);

                            //new
                            workingStates.push(destsInNFA);
                        }

                        edges.add(new DirectedEdgeWithInputOutput(stateInDFA, destInDFA,
                                input, output));
                    }
                }
        }

        EdgeWeightedDigraph dfa = new EdgeWeightedDigraph(allStatesDFA.size());
        dfa.setSourceVertex(initInDFA);

        //compute accepting states
        Set<Integer> acceptingDFA = new HashSet<Integer>();
        for (BitSet statesNFA : mapStates.keySet()) {
            for (int stateNFA = statesNFA.nextSetBit(0);
                 stateNFA >= 0;
                 stateNFA = statesNFA.nextSetBit(stateNFA + 1)) {
                if (graph.getDestVertices().contains(stateNFA)) {
                    acceptingDFA.add(mapStates.get(statesNFA));
                    break;
                }
            }
        }
        dfa.setDestVertices(acceptingDFA);

        for (DirectedEdgeWithInputOutput edge : edges) {
            dfa.addEdge(edge);
        }

        return dfa;
    }

    public static EdgeWeightedDigraph toDFA(EdgeWeightedDigraph graph, int numLabels) {
        Set<Integer> allStatesDFA = new HashSet<Integer>();
        Map<Set<Integer>, Integer> mapStates = new HashMap<Set<Integer>, Integer>();

        Stack<Set<Integer>> workingStates = new Stack<Set<Integer>>();
        Set<Integer> initSet = new HashSet<Integer>();
        initSet.add(graph.getSourceVertex());
        initSet = getEpsilonClosure(graph, initSet);

        workingStates.push(initSet);

        //state 0 will be the init state in new DFA
        int initInDFA = 0;
        mapStates.put(initSet, initInDFA);
        allStatesDFA.add(initInDFA);

        List<DirectedEdgeWithInputOutput> edges = new ArrayList<DirectedEdgeWithInputOutput>();

        while (!workingStates.isEmpty()) {
            Set<Integer> statesInNFA = workingStates.pop();
            int stateInDFA = mapStates.get(statesInNFA);

            for (int input = 0; input < numLabels; input++) {
                for (int output = 0; output < numLabels; output++) {
                    Set<Integer> destsInNFA =
                            getEpsilonClosure(graph,
                                    getDests(graph, statesInNFA,
                                            input, output));

                    if (!destsInNFA.isEmpty()) {
                        Integer destInDFA = mapStates.get(destsInNFA);

                        if (destInDFA == null) {
                            destInDFA = mapStates.size();
                            mapStates.put(destsInNFA, destInDFA);
                            allStatesDFA.add(destInDFA);

                            //new
                            workingStates.push(destsInNFA);
                        }
                        edges.add(new DirectedEdgeWithInputOutput(stateInDFA, destInDFA, input, output));
                    }
                }
            }
        }

        EdgeWeightedDigraph dfa = new EdgeWeightedDigraph(allStatesDFA.size());
        dfa.setSourceVertex(initInDFA);

        //compute accepting states
        Set<Integer> acceptingDFA = new HashSet<Integer>();
        for (Set<Integer> statesNFA : mapStates.keySet()) {
            for (Integer stateNFA : statesNFA) {
                if (graph.getDestVertices().contains(stateNFA)) {
                    acceptingDFA.add(mapStates.get(statesNFA));
                    break;
                }
            }
        }
        dfa.setDestVertices(acceptingDFA);

        for (DirectedEdgeWithInputOutput edge : edges) {
            dfa.addEdge(edge);
        }
        return dfa;
    }

    /**
     * Compute epsilon closure from a set of states
     */
    public static Set<Integer> getEpsilonClosure(EdgeWeightedDigraph graph, Set<Integer> fromStates) {
        final Set<Integer> result = new HashSet<Integer>();
        final Stack<Integer> workingStates = new Stack<Integer>();
        final boolean[] isVisited = new boolean[graph.getNumVertices()];

        workingStates.addAll(fromStates);

        for (int fromState : fromStates) {
            isVisited[fromState] = true;
        }

        while (!workingStates.isEmpty()) {
            int currentState = workingStates.pop();
            result.add(currentState);

            //add new states to workingState
            for (DirectedEdge edge : graph.getIncidentEdges(currentState)) {
                DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
                if (tempEdge.getInput() == Automata.EPSILON_LABEL && tempEdge.getOutput() == Automata.EPSILON_LABEL) {
                    if (!isVisited[tempEdge.to()]) {
                        isVisited[tempEdge.to()] = true;
                        workingStates.push(tempEdge.to());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Compute epsilon closure from a set of states
     */
    public static void getEpsilonClosure(EdgeWeightedDigraph graph, BitSet fromStates) {
        final Stack<Integer> workingStates = new Stack<Integer>();

        for (int i = fromStates.nextSetBit(0); i >= 0; i = fromStates.nextSetBit(i + 1))
            workingStates.add(i);

        while (!workingStates.isEmpty()) {
            int currentState = workingStates.pop();
            //add new states to workingState
            for (DirectedEdge edge : graph.getIncidentEdges(currentState)) {
                DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
                if (tempEdge.getInput() == Automata.EPSILON_LABEL &&
                        tempEdge.getOutput() == Automata.EPSILON_LABEL) {
                    if (!fromStates.get(tempEdge.to())) {
                        fromStates.set(tempEdge.to());
                        workingStates.push(tempEdge.to());
                    }
                }
            }
        }
    }

    private static Set<Integer> getDests(EdgeWeightedDigraph graph, Set<Integer> states, int input, int output) {
        final Set<Integer> result = new HashSet<Integer>();

        for (int stateIndex : states) {
            Iterable<DirectedEdge> edges = graph.getIncidentEdges(stateIndex);
            for (DirectedEdge edge : edges) {
                DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
                if (tempEdge.getInput() == input && tempEdge.getOutput() == output) {
                    result.add(tempEdge.to());
                }
            }
        }
        return result;
    }

    public static boolean isDFA(EdgeWeightedDigraph graph, int numLetters) {
        final int numStates = graph.getNumVertices();

        for (int i = 0; i < numStates; i++) {
            Iterable<DirectedEdge> edges = graph.getIncidentEdges(i);
            boolean[][] hasTrans = new boolean[numLetters][numLetters];
            for (DirectedEdge edge : edges) {
                DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
                int input = tempEdge.getInput();
                int output = tempEdge.getOutput();

                if (input == Automata.EPSILON_LABEL || output == Automata.EPSILON_LABEL) {
                    return false;
                } else if (hasTrans[input][output]) {
                    return false;
                } else {
                    hasTrans[input][output] = true;
                }
            }
        }
        return true;
    }

    public static EdgeWeightedDigraph makeComplete(EdgeWeightedDigraph transducer, int numLetters) {
        EdgeWeightedDigraph completeTransducer = new EdgeWeightedDigraph(transducer.getNumVertices() + 1, transducer.getSourceVertex(), new HashSet<Integer>(transducer.getDestVertices()));
        final int dummyState = transducer.getNumVertices();

        for (int i = 0; i < transducer.getNumVertices(); i++) {
            boolean[][] hasTrans = new boolean[numLetters][numLetters];
            Iterable<DirectedEdge> edges = transducer.getIncidentEdges(i);

            //copy transition
            for (DirectedEdge edge : edges) {
                DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
                hasTrans[tempEdge.getInput()][tempEdge.getOutput()] = true;

                completeTransducer.addEdge(new DirectedEdgeWithInputOutput(tempEdge));
            }

            //add dummy transition
            for (int input = 0; input < numLetters; input++) {
                for (int output = 0; output < numLetters; output++) {
                    if (!hasTrans[input][output]) {
                        completeTransducer.addEdge(new DirectedEdgeWithInputOutput(i, dummyState, input, output));
                    }
                }
            }
        }

        //loop at dummy
        for (int input = 0; input < numLetters; input++) {
            for (int output = 0; output < numLetters; output++) {
                completeTransducer.addEdge(new DirectedEdgeWithInputOutput(dummyState, dummyState, input, output));
            }
        }
        return completeTransducer;
    }

    public static EdgeWeightedDigraph computeSquare(EdgeWeightedDigraph graph) {
        int numStates = graph.getNumVertices();
        EdgeWeightedDigraph result = new EdgeWeightedDigraph(numStates * numStates);

        for (DirectedEdge edge1 : graph.getEdges()) {
            DirectedEdgeWithInputOutput tempEdge1 = (DirectedEdgeWithInputOutput) edge1;
            for (DirectedEdge edge2 : graph.getEdges()) {
                DirectedEdgeWithInputOutput tempEdge2 = (DirectedEdgeWithInputOutput) edge2;
                if (tempEdge1.getOutput() == tempEdge2.getInput()) {
                    DirectedEdge newEdge = new DirectedEdgeWithInputOutput(
                            VerificationUtility.hash(tempEdge1.from(), tempEdge2.from(), numStates),
                            VerificationUtility.hash(tempEdge1.to(), tempEdge2.to(), numStates),
                            tempEdge1.getInput(), tempEdge2.getOutput());
                    result.addEdge(newEdge);
                }
            }
        }
        //set init
        result.setSourceVertex(VerificationUtility.hash(graph.getSourceVertex(), graph.getSourceVertex(), numStates));

        //set accepting states
        Set<Integer> acceptings = new HashSet<Integer>();
        for (int accept1 : graph.getDestVertices()) {
            for (int accept2 : graph.getDestVertices()) {
                acceptings.add(VerificationUtility.hash(accept1, accept2, numStates));
            }
        }

        result.setDestVertices(acceptings);
        return result;
    }

    public static boolean isComplete(EdgeWeightedDigraph transducer, int numLetters) {
        int numStates = transducer.getNumVertices();

        boolean[][][] hasTrans = new boolean[numStates][numLetters][numLetters];
        for (DirectedEdge edge : transducer.getEdges()) {
            DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
            int source = tempEdge.from();
            int input = tempEdge.getInput();
            int output = tempEdge.getOutput();
            if (hasTrans[source][input][output]) {
                return false;
            } else {
                hasTrans[source][input][output] = true;
            }
        }

        return false;
    }

    /*
     * States are counted from 0
     */
    public static int hash(int state1, int state2, int numStates1) {
        return state1 + numStates1 * state2;
    }

    /*
     * States are counted from 0
     */
    public static int hash(int state1, int state2, int state3, int numStates1, int numStates2) {
        return state1 + numStates1 * (state2 + numStates2 * state3);
    }

    /*
     * States are counted from 0
     */
    public static int hash(int state1, int state2, int state3, int state4,
                           int numStates1, int numStates2, int numStates3) {
        return state1 + numStates1 * (state2 + numStates2 * (state3 + numStates3 * state4));
    }


    /**
     * Convert accepting states to based 0.
     *
     * @param acceptingStates
     * @return
     */
    public static Set<Integer> convertAccepting(Set<Integer> acceptingStates) {
        // compute accepting state
        final Set<Integer> newAccept = new HashSet<Integer>();
        for (Integer acc : acceptingStates) {
            newAccept.add(acc - 1);
        }
        return newAccept;
    }

    public static Automata getImage(Automata from,
                                    EdgeWeightedDigraph function) {
        final int numFrom = from.getStates().length;
        final int numFunction = function.getNumVertices();
        final int numLetters = from.getNumLabels();
        final Automata result =
                new Automata(VerificationUtility.hash(from.getInitStateId(),
                        function.getSourceVertex(),
                        numFrom),
                        numFrom * numFunction,
                        numLetters);

        Set<Integer> acceptings = new HashSet<Integer>();
        for (int acc1 : from.getAcceptingStateIds())
            for (int acc3 : function.getDestVertices())
                acceptings.add(VerificationUtility.hash(acc1, acc3, numFrom));
        result.setAcceptingStateIds(acceptings);

        for (DirectedEdge edge : function.getEdges()) {
            DirectedEdgeWithInputOutput ioEdge = (DirectedEdgeWithInputOutput) edge;
            for (int from1 = 0; from1 < numFrom; ++from1)
                for (int to1 : from.getStates()[from1].getDestIds(ioEdge.getInput()))
                    result.addTrans(VerificationUtility.hash(from1, ioEdge.from(),
                            numFrom),
                            ioEdge.getOutput(),
                            VerificationUtility.hash(to1, ioEdge.to(), numFrom));
        }

        return result;
    }

    public static Automata getPreImage(EdgeWeightedDigraph function,
                                       Automata to) {
        final int numTo = to.getStates().length;
        final int numFunction = function.getNumVertices();
        final int numLetters = to.getNumLabels();
        final Automata result =
                new Automata(VerificationUtility.hash(to.getInitStateId(),
                        function.getSourceVertex(),
                        numTo),
                        numTo * numFunction,
                        numLetters);

        Set<Integer> acceptings = new HashSet<Integer>();
        for (int acc1 : to.getAcceptingStateIds())
            for (int acc3 : function.getDestVertices())
                acceptings.add(VerificationUtility.hash(acc1, acc3, numTo));
        result.setAcceptingStateIds(acceptings);

        for (DirectedEdge edge : function.getEdges()) {
            DirectedEdgeWithInputOutput ioEdge = (DirectedEdgeWithInputOutput) edge;
            for (int from1 = 0; from1 < numTo; ++from1)
                for (int to1 : to.getStates()[from1].getDestIds(ioEdge.getOutput()))
                    result.addTrans(VerificationUtility.hash(from1, ioEdge.from(),
                            numTo),
                            ioEdge.getInput(),
                            VerificationUtility.hash(to1, ioEdge.to(), numTo));
        }

        return result;
    }


    public static List<List<Integer>> findSomeTrace(
            List<Integer> target, Automata from,
            EdgeWeightedDigraph function
    ) {
        final LinkedList<List<Integer>> trace = new LinkedList<>();
        final Automata init = AutomataUtility.getWordAutomaton(from, target.size());
        final boolean isFound = findSomeTraceHelper(init, function, target, trace);
        return isFound ? trace : null;
    }

    private static boolean findSomeTraceHelper(
            Automata from,
            EdgeWeightedDigraph function,
            List<Integer> target,
            LinkedList<List<Integer>> trace
    ) {
        trace.addFirst(target);
        if (from.accepts(target)) {
            return true;
        }
        final int numLetters = from.getNumLabels();
        final List<List<Integer>> range = AutomataUtility.getWords(
                getPreImage(target, function, numLetters), target.size());
        for (List<Integer> word : range) {
            if (trace.contains(word))
                continue;
            if (findSomeTraceHelper(from, function, word, trace))
                return true;
        }
        trace.removeFirst();
        return false;
    }

    public static EdgeWeightedDigraph minimise(EdgeWeightedDigraph graph, int numLetters) {
        numLetters += 1;
        Automata aut = new Automata(graph.getSourceVertex(),
                graph.getNumVertices(), numLetters * numLetters);
        for (DirectedEdge e : graph.getEdges()) {
            DirectedEdgeWithInputOutput edge = (DirectedEdgeWithInputOutput) e;
            int label = (edge.getInput() + 1) * numLetters + (edge.getOutput() + 1);
            aut.addTrans(edge.from(), label, edge.to());
        }
        aut.setAcceptingStateIds(graph.getDestVertices());
        aut = AutomataUtility.minimise(aut);
        EdgeWeightedDigraph res = new EdgeWeightedDigraph(
                aut.getNumStates(), aut.getInitStateId(), aut.getAcceptingStateIds());
        for (State s : aut.getStates()) {
            for (int label : s.getOutgoingLabels()) {
                int from = s.getId();
                int input = (label / numLetters) - 1;
                int output = (label % numLetters) - 1;
                for (int to : s.getDestIds(label)) {
                    res.addEdge(new DirectedEdgeWithInputOutput(from, to, input, output));
                }
            }
        }
        return res;
    }

    /*
     * counterExample[i] contains labels i.th of words
     * return list of words
     */
    public static List<List<Integer>> convertToWords(List<int[]> counterExample, int NUM_WORDS) {
        if (counterExample == null) {
            return null;
        }

        List<List<Integer>> result = new ArrayList<List<Integer>>();
        for (int i = 0; i < NUM_WORDS; i++) {
            result.add(new ArrayList<Integer>());
        }

        for (int[] tripple : counterExample) {
            for (int i = 0; i < NUM_WORDS; i++) {
                result.get(i).add(tripple[i]);
            }
        }

        return result;
    }

    /**
     * Compute the set of all words x such that (x, y) \in fun for some y
     */

    public static Automata computeDomain(EdgeWeightedDigraph fun,
                                         int numLabels) {
        Automata result = new Automata(fun.getSourceVertex(),
                fun.getNumVertices(),
                numLabels);

        for (int s = 0; s < fun.getNumVertices(); ++s)
            for (DirectedEdge edge : fun.getIncidentEdges(s)) {
                DirectedEdgeWithInputOutput ioEdge =
                        (DirectedEdgeWithInputOutput) edge;
                result.addTrans(ioEdge.from(), ioEdge.getInput(), ioEdge.to());
            }

        result.setAcceptingStateIds(fun.getDestVertices());

        return AutomataUtility.minimise(result);
    }

    /**
     * Compute the set of all words y such that (x, y) \in fun for some x
     */
    public static Automata computeRange(EdgeWeightedDigraph fun,
                                        int numLabels) {
        Automata result = new Automata(fun.getSourceVertex(),
                fun.getNumVertices(),
                numLabels);

        for (int s = 0; s < fun.getNumVertices(); ++s)
            for (DirectedEdge edge : fun.getIncidentEdges(s)) {
                DirectedEdgeWithInputOutput ioEdge =
                        (DirectedEdgeWithInputOutput) edge;
                result.addTrans(ioEdge.from(), ioEdge.getOutput(), ioEdge.to());
            }

        result.setAcceptingStateIds(fun.getDestVertices());

        return AutomataUtility.minimise(result);
    }

    public static Automata getPreImage(List<Integer> word,
                                       EdgeWeightedDigraph function,
                                       int numLetters) {
        int wordLen = word.size();

        final int hashStride = wordLen + 1;
        Automata aut =
                new Automata(hash(0, function.getSourceVertex(), hashStride),
                        function.getNumVertices() * (wordLen + 1),
                        numLetters);

        for (int pos = 0; pos < wordLen; ++pos) {
            int nextChar = word.get(pos);
            for (DirectedEdge edge : function.getEdges()) {
                DirectedEdgeWithInputOutput edgeFunction = (DirectedEdgeWithInputOutput) edge;
                if (edgeFunction.getOutput() == nextChar)
                    aut.addTrans(hash(pos, edgeFunction.from(), hashStride),
                            edgeFunction.getInput(),
                            hash(pos + 1, edgeFunction.to(), hashStride));
            }
        }

        Set<Integer> acceptings = new HashSet<Integer>();
        for (int a : function.getDestVertices())
            acceptings.add(hash(wordLen, a, hashStride));
        aut.setAcceptingStateIds(acceptings);

        Automata prunedAut = AutomataUtility.pruneUnreachableStates(aut);
        Automata completeAut = AutomataUtility.toCompleteDFA(prunedAut);
        Automata minimalAut = AutomataUtility.toMinimalDFA(completeAut);

        return minimalAut;
    }

    public static Automata getImage(List<Integer> word,
                                    EdgeWeightedDigraph function,
                                    int numLetters) {
        int wordLen = word.size();

        final int hashStride = wordLen + 1;
        Automata aut =
                new Automata(hash(0, function.getSourceVertex(), hashStride),
                        function.getNumVertices() * (wordLen + 1),
                        numLetters);

        for (int pos = 0; pos < wordLen; ++pos) {
            int nextChar = word.get(pos);
            for (DirectedEdge edge : function.getEdges()) {
                DirectedEdgeWithInputOutput edgeFunction = (DirectedEdgeWithInputOutput) edge;
                if (edgeFunction.getInput() == nextChar)
                    aut.addTrans(hash(pos, edgeFunction.from(), hashStride),
                            edgeFunction.getOutput(),
                            hash(pos + 1, edgeFunction.to(), hashStride));
            }
        }

        Set<Integer> acceptings = new HashSet<Integer>();
        for (int a : function.getDestVertices())
            acceptings.add(hash(wordLen, a, hashStride));
        aut.setAcceptingStateIds(acceptings);

        Automata prunedAut = AutomataUtility.pruneUnreachableStates(aut);
        Automata completeAut = AutomataUtility.toCompleteDFA(prunedAut);
        Automata minimalAut = AutomataUtility.toMinimalDFA(completeAut);

        return minimalAut;
    }
}

// vim: ts=4
