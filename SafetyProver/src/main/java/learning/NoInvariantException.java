package learning;

import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NoInvariantException extends RuntimeException {
    private static Map<Integer, String> indexToLabelMapping;

    public NoInvariantException(List<Integer> cex, Automata I, EdgeWeightedDigraph T) {
        super("Invariant does not exist! A bad configuration is reachable: " +
                getLabeledWord(cex) + "\nTrace: " + getTrace(cex, I, T));
    }

    static List<Object> getTrace(List<Integer> target, Automata I, EdgeWeightedDigraph T) {
        List<List<Integer>> trace = VerificationUtility.findSomeTrace(target, I, T);
        return trace == null ? null : Arrays.asList(trace.stream()
                .map(word -> getLabeledWord(word).toString()).toArray());
    }

    static List<Object> getLabeledWord(List<Integer> word) {
        return Arrays.asList(word.stream().map(
                index -> indexToLabelMapping.get(index)).toArray());
    }

    public static void setIndexToLabelMapping(Map<Integer, String> indexToLabelMapping) {
        NoInvariantException.indexToLabelMapping = indexToLabelMapping;
    }
}

