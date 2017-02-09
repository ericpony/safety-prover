package learning;

public class SatInvariantNotFoundException extends RuntimeException {

    private int minNumOfStatesTransducer = -1;
    private int minNumOfStatesAutomaton = -1;
    private int minNumOfInitStatesAutomaton = -1;
    private int maxNumOfStatesTransducer = -1;
    private int maxNumOfStatesAutomaton = -1;
    private int maxNumOfInitStatesAutomaton = -1;

    public void setMaxNumOfStatesTransducer(int maxNumOfStatesTransducer) {
        this.maxNumOfStatesTransducer = maxNumOfStatesTransducer;
    }

    public void setMaxNumOfStatesAutomaton(int maxNumOfStatesAutomaton) {
        this.maxNumOfStatesAutomaton = maxNumOfStatesAutomaton;
    }

    public void setMaxNumOfInitStatesAutomaton(int maxNumOfInitStatesAutomaton) {
        this.maxNumOfInitStatesAutomaton = maxNumOfInitStatesAutomaton;
    }

    public void setMinNumOfStatesTransducer(int minNumOfStatesTransducer) {
        this.minNumOfStatesTransducer = minNumOfStatesTransducer;
    }

    public void setMinNumOfStatesAutomaton(int minNumOfStatesAutomaton) {
        this.minNumOfStatesAutomaton = minNumOfStatesAutomaton;
    }

    public void setMinNumOfInitStatesAutomaton(int minNumOfInitStatesAutomaton) {
        this.minNumOfInitStatesAutomaton = minNumOfInitStatesAutomaton;
    }

    public String toString() {
        return "SAT solver cannot find an invariant under the following constraints:\n"
                + "\tNumber of transducer states: " + minNumOfStatesTransducer + ".." + maxNumOfStatesTransducer + "\n"
                + "\tNumber of invariant automaton states: " + minNumOfStatesAutomaton + ".." + maxNumOfStatesAutomaton + "\n"
                + "\tNumber of initial automaton states: " + minNumOfInitStatesAutomaton + ".." + maxNumOfInitStatesAutomaton;
    }
}

