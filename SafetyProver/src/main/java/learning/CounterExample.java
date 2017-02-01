package learning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CounterExample {
    private List<Integer> cex;
    private boolean isPositive;
    private Set<List<Integer>> history = null;

    public CounterExample() {
        this(0);
    }

    public CounterExample(int historySize) {
        if (historySize > 0)
            history = new HashSet<List<Integer>>(historySize);
    }

    public void addNegative(List<Integer> ex) {
        if (cex != null) throw new IllegalStateException("counterexample is set twice!");
        cex = ex;
        if (history != null && !history.add(cex))
            throw new IllegalStateException("teacher produced duplicate counterexamples!");
        isPositive = false;
    }

    public void addPositive(List<Integer> ex) {
        if (cex != null) throw new IllegalStateException("counterexample is set twice!");
        cex = ex;
        if (history != null && !history.add(cex))
            throw new IllegalStateException("teacher produced duplicate counterexamples!");
        isPositive = true;
    }

    public boolean isPositive() {
        return cex != null && this.isPositive;
    }

    public boolean isNegative() {
        return cex != null && !this.isPositive;
    }

    public void reset() {
        cex = null;
    }

    public boolean exists() {
        return cex != null;
    }

    public List<Integer> get() {
        if (!exists())
            return new ArrayList<Integer>();
        else
            return cex;
    }
}
