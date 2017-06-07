package visitor;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import grammar.Absyn.*;
import grammar.AllVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class AllVisitorImpl implements AllVisitor<Object, RegularModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<String, Integer> transducerStateToIndex = new HashMap<>();
    private Map<String, Integer> labelToIndex = new HashMap<>();

    private Map<String, Integer> automataStateToIndex = new HashMap<>();

    //transducer looping transition info temporary
    private List<Integer> iStarStates = new ArrayList<>();

    public Object visit(Model p, RegularModel arg) {

        Automata I = (Automata) p.automatonrule_1.accept(this, arg);
        p.maybeclosed_.accept(this, arg);
        Automata F = (Automata) p.automatonrule_2.accept(this, arg);
        EdgeWeightedDigraph T = (EdgeWeightedDigraph) p.transducerrule_.accept(this, arg);

        for (VerifierOption o : p.listverifieroption_)
            o.accept(this, arg);

        final int numLabels = labelToIndex.size();

        I.setNumLabels(numLabels);
        F.setNumLabels(numLabels);

        //set mapping of Label
        arg.setI(I);
        arg.setB(F);
        arg.setT(T);
        arg.setLabelToIndex(labelToIndex);
        arg.setNumberOfLetters(numLabels);

        LOGGER.info("Label mapping: " + labelToIndex);

        return null;
    }

    public Object visit(Transducer p, RegularModel arg) {
        //reset temporary data
        transducerStateToIndex = new HashMap<String, Integer>();
        iStarStates = new ArrayList<Integer>();

        int initState = (Integer) p.initrule_.accept(this, arg);

        List<DirectedEdgeWithInputOutput> edges = new ArrayList<DirectedEdgeWithInputOutput>();
        for (TransitionRule transitionRule : p.listtransitionrule_) {
            DirectedEdgeWithInputOutput newEdge = (DirectedEdgeWithInputOutput) transitionRule.accept(this, arg);
            if (newEdge != null) {
                edges.add(newEdge);
            }
        }

        Set<Integer> acceptingStates = (Set<Integer>) p.acceptingrule_.accept(this, arg);

        //process after collecting information
        int numStates = transducerStateToIndex.size();
        int numLetters = labelToIndex.size();


        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(numStates, initState, acceptingStates);
        for (DirectedEdgeWithInputOutput edge : edges) {
            graph.addEdge(edge);
        }

        //add i* transition
        for (int loopingState : this.iStarStates) {
            List<DirectedEdge> loopingTransitions = addIStartEdge(loopingState, numLetters);
            for (DirectedEdge loopingTransition : loopingTransitions) {
                graph.addEdge(loopingTransition);
            }
        }

        return graph;
    }

    public Object visit(TransducerInitialState p, RegularModel arg) {
        int init = getIndex(p.name_, arg, transducerStateToIndex);
        return init;
    }

    public Object visit(FulTransition p, RegularModel arg) {
        int from = getIndex(p.name_1, arg, transducerStateToIndex);
        int to = getIndex(p.name_2, arg, transducerStateToIndex);
        int input = getIndex(p.name_3, arg, labelToIndex);
        int output = getIndex(p.name_4, arg, labelToIndex);

        DirectedEdge newEdge = new DirectedEdgeWithInputOutput(from, to, input, output);

        return newEdge;
    }

    public Object visit(EmptyTransition p, RegularModel arg) {
        int from = getIndex(p.name_1, arg, transducerStateToIndex);
        int to = getIndex(p.name_2, arg, transducerStateToIndex);
        int input = common.finiteautomata.Automata.EPSILON_LABEL;
        int output = common.finiteautomata.Automata.EPSILON_LABEL;

        DirectedEdge newEdge = new DirectedEdgeWithInputOutput(from, to, input, output);

        return newEdge;
    }

    public Object visit(LoopingTransition p, RegularModel arg) {
        for (Name name : p.listname_) {
            int stateIndex = getIndex(name, arg, transducerStateToIndex);
            this.iStarStates.add(stateIndex);
        }

        return null;
    }

    public Object visit(TransducerAccepting p, RegularModel arg) {
        List<String> names = getNames(p.listname_, arg);
        List<Integer> acceptings = getIndexes(names, transducerStateToIndex);

        return new HashSet<Integer>(acceptings);
    }

    public Object visit(Automaton p, RegularModel arg) {
        //reset temporary data
        automataStateToIndex = new HashMap<String, Integer>();

        int initState = (Integer) p.automatainitrule_.accept(this, arg);

        List<DirectedEdgeWithInputOutput> edges = new ArrayList<DirectedEdgeWithInputOutput>();
        for (AutomataTransitionRule transition : p.listautomatatransitionrule_) {
            edges.add((DirectedEdgeWithInputOutput) transition.accept(this, arg));
        }

        Set<Integer> acceptingStates = (Set<Integer>) p.automataacceptingsrule_.accept(this, arg);

        common.finiteautomata.Automata newAutomata = new common.finiteautomata.Automata(initState, automataStateToIndex.size(), labelToIndex.size());
        newAutomata.setAcceptingStateIds(acceptingStates);
        for (DirectedEdgeWithInputOutput edge : edges) {
            newAutomata.addTrans(edge.from(), edge.getInput(), edge.to());
        }

        return newAutomata;
    }

    public Object visit(AutomataInitialState p, RegularModel arg) {
        return getIndex(p.name_, arg, automataStateToIndex);
    }

    public Object visit(AutomataTransition p, RegularModel arg) {
        int source = getIndex(p.name_1, arg, automataStateToIndex);
        int destination = getIndex(p.name_2, arg, automataStateToIndex);
        int label = getIndex(p.name_3, arg, labelToIndex);

        int dummyOutput = 0;
        DirectedEdge edge = new DirectedEdgeWithInputOutput(source, destination, label, dummyOutput);

        return edge;
    }


    public Object visit(AutomataEmptyTransition p, RegularModel arg) {
        int source = getIndex(p.name_1, arg, automataStateToIndex);
        int destination = getIndex(p.name_2, arg, automataStateToIndex);
        int label = common.finiteautomata.Automata.EPSILON_LABEL;

        int dummyOutput = 0;
        DirectedEdge edge = new DirectedEdgeWithInputOutput(source, destination, label, dummyOutput);

        return edge;
    }

    public Object visit(AutomataAcceptings p, RegularModel arg) {
        Set<Integer> acceptings = new HashSet<Integer>();
        List<String> names = getNames(p.listname_, arg);
        List<Integer> indexes = getIndexes(names, automataStateToIndex);

        acceptings.addAll(indexes);
        return acceptings;
    }

    public Object visit(NumberName p, RegularModel arg) {
        return p.myinteger_;
    }

    public Object visit(LiteralName p, RegularModel arg) {
        return p.labelident_;
    }

    public Object visit(NumOfStatesTransducerGuess p, RegularModel arg) {
        arg.setMinNumOfStatesTransducer(Integer.parseInt(p.myinteger_1));
        arg.setMaxNumOfStatesTransducer(Integer.parseInt(p.myinteger_2));
        return null;
    }

    public Object visit(NumOfStatesAutomatonGuess p, RegularModel arg) {
        arg.setMinNumOfStatesAutomaton(Integer.parseInt(p.myinteger_1));
        arg.setMaxNumOfStatesAutomaton(Integer.parseInt(p.myinteger_2));
        return null;
    }

    public Object visit(NumOfInitStatesAutomatonGuess p, RegularModel arg) {
        arg.setMinNumOfInitStatesAutomaton(Integer.parseInt(p.myinteger_1));
        arg.setMaxNumOfInitStatesAutomaton(Integer.parseInt(p.myinteger_2));
        return null;
    }

    public Object visit(NoInitStatesAutomatonGuess p, RegularModel arg) {
        arg.setMinNumOfInitStatesAutomaton(1);
        arg.setMaxNumOfInitStatesAutomaton(1);
        return null;
    }

    public Object visit(ClosedInit p, RegularModel arg) {
        arg.setCloseInitStates(true);
        return null;
    }

    public Object visit(NotClosedInit p, RegularModel arg) {
        arg.setCloseInitStates(false);
        return null;
    }

    private String getName(Name name, RegularModel arg) {
        return (String) name.accept(this, arg);
    }

    private List<String> getNames(ListName names, RegularModel arg) {
        List<String> result = new ArrayList<String>();
        for (Name name : names) {
            result.add(getName(name, arg));
        }

        return result;
    }

    private List<Integer> getIndexes(List<String> names, Map<String, Integer> mapping) {
        List<Integer> result = new ArrayList<Integer>();
        for (String name : names) {
            result.add(getIndex(name, mapping));
        }

        return result;
    }

    private Integer getIndex(String name, Map<String, Integer> mapping) {
        if (mapping.containsKey(name)) {
            return mapping.get(name);
        } else {
            int value = mapping.size();
            mapping.put(name, value);
            return value;
        }
    }

    private Integer getIndex(Name name, RegularModel arg, Map<String, Integer> mapping) {
        String nameLiteral = getName(name, arg);

        return getIndex(nameLiteral, mapping);
    }

    private List<DirectedEdge> addIStartEdge(int state, int numLetters) {
        List<DirectedEdge> result = new ArrayList<DirectedEdge>();
        for (int i = 0; i < numLetters; i++) {
            DirectedEdgeWithInputOutput newEdge = new DirectedEdgeWithInputOutput(state, state, i, i);
            result.add(newEdge);
        }

        return result;
    }

    public Object visit(grammar.Absyn.SymmetryOptions p,
                        RegularModel arg) {
        for (SymmetryOption so : p.listsymmetryoption_)
            so.accept(this, arg);
        return null;
    }

    public Object visit(grammar.Absyn.RotationSymmetry p,
                        RegularModel arg) {
        arg.addSymmetry("rotation");
        return null;
    }

    public Object visit(grammar.Absyn.RotationWithSymmetry p,
                        RegularModel arg) {
        String rot = "rotation";
        for (Name n : p.listname_)
            rot = rot + "_" + getIndex(n, arg, labelToIndex);
        arg.addSymmetry(rot);
        return null;
    }

    public Object visit(grammar.Absyn.ExplicitChecks p, RegularModel arg) {
        arg.setExplicitChecksUntilLength(Integer.parseInt(p.myinteger_));
        return null;
    }

    public Object visit(grammar.Absyn.UseRankingFunctions p, RegularModel arg) {
        arg.setUseRankingFunctions(true);
        return null;
    }

    public Object visit(grammar.Absyn.MonolithicWitness p, RegularModel arg) {
        arg.setAlwaysMonolithic(true);
        return null;
    }

    public Object visit(grammar.Absyn.NoPrecomputedInv p, RegularModel arg) {
        arg.setPrecomputedInv(false);
        return null;
    }

    public Object visit(grammar.Absyn.LogLevel p, RegularModel arg) {
        arg.setLogLevel(Integer.parseInt(p.myinteger_));
        return null;
    }

    public Object visit(grammar.Absyn.ParLevel p, RegularModel arg) {
        arg.setParLevel(Integer.parseInt(p.myinteger_));
        return null;
    }

}
