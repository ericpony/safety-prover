package common;

public class Tuple<T> {
    public final T x;
    public final T y;

    public Tuple(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
