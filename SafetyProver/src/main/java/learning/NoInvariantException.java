package learning;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NoInvariantException extends RuntimeException {
    private static Map<Integer, String> indexToLabelMapping;

    public NoInvariantException(List<Integer> cex) {
        super("Invariant does not exist! A bad configuration is reachable: " +
                Arrays.asList(cex.stream()
                        .map(index -> indexToLabelMapping.get(index)).toArray()));
    }

    public static void setIndexToLabelMapping(Map<Integer, String> indexToLabelMapping) {
        NoInvariantException.indexToLabelMapping = indexToLabelMapping;
    }
}

