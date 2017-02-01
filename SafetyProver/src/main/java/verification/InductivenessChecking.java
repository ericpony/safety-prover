package verification;


import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import learning.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class InductivenessChecking {
    private static final Logger LOGGER = LogManager.getLogger();

    private Automata aut;
    private Automata knownInv;
    private EdgeWeightedDigraph player;
    private int numLetters;

    /**
     * Make sure that I, label starting from 1
     */
    public InductivenessChecking(Automata aut,
                                 Automata knownInv,
                                 EdgeWeightedDigraph player,
                                 int numLetters) {
        this.aut = aut;
        this.knownInv = knownInv;
        this.player = player;
        this.numLetters = numLetters;
    }

    public Tuple<List<Integer>> check() {
        final Automata lhs = VerificationUltility.getIntersectionLazily(aut, knownInv);
        final Automata img = VerificationUltility.getImage(lhs, player);
        final Automata badImgPoints = VerificationUltility.getDifference(img, aut);
        final List<Integer> point = AutomataConverter.getSomeShortestWord(badImgPoints);

        if (point == null) {
            return null;
        } else {
            final Automata prePoints =
                    AutomataConverter.getPreImage(point, player, numLetters);
            final List<Integer> prePoint =
                    AutomataConverter.getSomeWord(
                            VerificationUltility.getIntersectionLazily(prePoints, lhs));

            return new Tuple<List<Integer>>(prePoint, point);
        }
    }

    /**
     * Return 2 words x, y
     * result[0], result[1]
     */
/*
    public List<List<Integer>> checkX() {
	Automata complementA = AutomataConverter.getComplement(A);

	int numStatesA = A.getStates().length;
	int numStatesCA = complementA.getStates().length;
	int numStatesKI = knownInv.getStates().length;
	int numStatesPlayer = T.V();

	EdgeWeightedDigraph product =
	    new EdgeWeightedDigraph(numStatesA * numStatesPlayer *
				    numStatesCA * numStatesKI);
	product.setSourceVertex(VerificationUltility.hash(A.getSourceVertex(),
						       T.getSourceVertex(),
						       complementA.getSourceVertex(),
						       knownInv.getSourceVertex(),
						       numStatesA,
						       numStatesPlayer,
						       numStatesCA));

	//set accepting
	Set<Integer> acceptings = new HashSet<Integer>();
	for (int acceptAx: A.getDestVertices()) {
	    for (int acceptPlayer: T.getDestVertices()) {
		for (int acceptNAy: complementA.getDestVertices()) {
		    for (int acceptKI: knownInv.getDestVertices()) {
			acceptings.add(VerificationUltility.hash(acceptAx,
								 acceptPlayer,
								 acceptNAy,
								 acceptKI,
								 numStatesA,
								 numStatesPlayer,
								 numStatesCA));
		    }
		}
	    }
	}
	product.setAcceptingStateIds(acceptings);

	List<DirectedEdgeWithInputOutput> edgesA =
	    VerificationUltility.getEdges(A);
	List<DirectedEdgeWithInputOutput> edgesCA =
	    VerificationUltility.getEdges(complementA);
	List<DirectedEdgeWithInputOutput> edgesKI =
	    VerificationUltility.getEdges(knownInv);
	
	for(DirectedEdge edge: T.edges()) {
	    DirectedEdgeWithInputOutput edgePlayer = (DirectedEdgeWithInputOutput) edge;
	    for(DirectedEdgeWithInputOutput edgeAx: edgesA)
		if (edgePlayer.getInput() == edgeAx.getInput())
		    for(DirectedEdgeWithInputOutput edgeKI: edgesKI)
			if (edgeKI.getInput() == edgeAx.getInput())
			    for(DirectedEdgeWithInputOutput edgeAy: edgesCA)
				if (edgePlayer.getOutput() == edgeAy.getInput()) {
				    int source = VerificationUltility.hash(edgeAx.from(),
									   edgePlayer.from(),
									   edgeAy.from(),
									   edgeKI.from(),
									   numStatesA,
									   numStatesPlayer,
									   numStatesCA);
				    int dest = VerificationUltility.hash(edgeAx.to(),
									 edgePlayer.to(),
									 edgeAy.to(),
									 edgeKI.to(),
									 numStatesA,
									 numStatesPlayer,
									 numStatesCA);
			    
				    DirectedEdgeWithInputOutput newEdge =
					new DirectedEdgeWithInputOutput(source, dest,
									edgePlayer.getInput(),
									edgePlayer.getOutput());
				    product.addEdge(newEdge);
				}
	}
	
	List<DirectedEdge> edges =
	    product.DFS(product.getSourceVertex(), product.getDestVertices());

	if (edges == null) {
	    return null;
	} else {
	    List<Integer> x = new ArrayList<Integer>();
	    List<Integer> y = new ArrayList<Integer>();

	    for (DirectedEdge _edge : edges) {
		DirectedEdgeWithInputOutput edge = (DirectedEdgeWithInputOutput)_edge;
		x.add(edge.getInput());
		y.add(edge.getOutput());
	    }

	    List<List<Integer>> result = new ArrayList<List<Integer>>();
	    result.add(x);
	    result.add(y);

	    return result;
	}
    }
    */
}
