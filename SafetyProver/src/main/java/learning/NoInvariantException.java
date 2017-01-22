package learning;

public class NoInvariantException extends RuntimeException {
    public NoInvariantException() {
        super("Invariant does not exist!");
    }
}

