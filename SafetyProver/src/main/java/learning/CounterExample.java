package learning;

import java.util.ArrayList;
import java.util.List;

public class CounterExample {
    private List<Integer> cex;
    private boolean isPositive;

    public void addNegative(List<Integer> ex) {
        if (cex != null) throw new IllegalStateException("cex is already set");
        cex = ex;
        isPositive = false;
    }

    public void addPositive(List<Integer> ex) {
        if (cex != null) throw new IllegalStateException("cex is already set");
        cex = ex;
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
