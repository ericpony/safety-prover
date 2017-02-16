package verification;

import common.VerificationUtility;
import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;

import java.util.HashSet;
import java.util.Set;

public class TransducerComposition {

    /*
     * 2 pair edges (source1, input1, output1, dest1) (source2, input2, output2, dest2) where output1 = input2
     * create new edge in resule (source1, source2) (input1, ouput2). output1 (dest1, dest2)
     */
    public static EdgeWeightedDigraph compose(EdgeWeightedDigraph graph) {

        int numStates = graph.getNumVertices();
        EdgeWeightedDigraph result = new EdgeWeightedDigraph(numStates * numStates);
        for (DirectedEdge edge1 : graph.getEdges()) {
            DirectedEdgeWithInputOutput tempEdge1 = (DirectedEdgeWithInputOutput) edge1;
            for (DirectedEdge edge2 : graph.getEdges()) {
                DirectedEdgeWithInputOutput tempEdge2 = (DirectedEdgeWithInputOutput) edge2;
                if (tempEdge1.getOutput() == tempEdge2.getInput()) {
                    DirectedEdge newEdge = new DirectedEdgeWithInputOutputCommon(VerificationUtility.hash(tempEdge1.from(), tempEdge2.from(), numStates),
                            VerificationUtility.hash(tempEdge1.to(), tempEdge2.to(), numStates),
                            tempEdge1.getInput(), tempEdge2.getOutput(), tempEdge1.getOutput());
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

    public static class DirectedEdgeWithInputOutputCommon extends DirectedEdgeWithInputOutput {

        private int common;

        public DirectedEdgeWithInputOutputCommon(int v, int w, int input, int output, int common) {
            super(v, w, input, output);
            this.common = common;
        }

        public int getCommon() {
            return common;
        }

        /**
         * Returns a string representation of the directed edge.
         *
         * @return a string representation of the directed edge
         */
        public String toString() {
            return this.from() + " (" + getInput() + "," + getOutput() + ")/" + common + " " + this.to();
        }
    }
}

