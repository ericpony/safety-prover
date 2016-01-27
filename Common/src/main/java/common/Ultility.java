package common;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import common.bellmanford.DirectedEdge;
import common.bellmanford.DirectedEdgeWithInputOutput;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.State;


public class Ultility {
	
	public static String toDot(Automata automata, Map<String, Integer> transducerLabelToIndex){
		final String NEW_LINE = "\n";
		final String SPACE = " ";
		StringBuilder result = new StringBuilder();
		
		result.append("digraph finite_state_machine {");
		result.append(NEW_LINE);
		result.append("rankdir=LR;");
		result.append(NEW_LINE);
		result.append("size=\"8,5\"");
		result.append(NEW_LINE);
		result.append("node [shape = doublecircle]; ");
		
		for(int accepting: automata.getAcceptingStates()){
			result.append(accepting);
			result.append(SPACE);
		}
		result.append(";");
		result.append(NEW_LINE);
		result.append("node [shape = circle];");
		result.append(NEW_LINE);
		
		for(State state: automata.getStates()){
			for(int i = Automata.EPSILON_LABEL; i < automata.getNumLabels(); i++){
				String label = (i == Automata.EPSILON_LABEL)? "": String.valueOf(getLabel(transducerLabelToIndex, i));
				Set<Integer> nexts = state.getDest(i);
				for(Integer next: nexts){
					result.append(state.getId() + " -> " + next + " [ label = \"" + label + "\" ];");
					result.append(NEW_LINE);
				}
			}
		}
		
		result.append("}");
		
		return result.toString();
	}
	
	public static String toDot(EdgeWeightedDigraph transducer, Map<String, Integer> transducerLabelToIndex){
		final String NEW_LINE = "\n";
		final String SPACE = " ";
		StringBuilder result = new StringBuilder();
		
		result.append("digraph finite_state_machine {");
		result.append(NEW_LINE);
		result.append("rankdir=LR;");
		result.append(NEW_LINE);
		result.append("size=\"8,5\"");
		result.append(NEW_LINE);
		result.append("node [shape = doublecircle]; ");
		for(int accepting: transducer.getAcceptingStates()){
			result.append(accepting);
			result.append(SPACE);
		}
		result.append(";");
		result.append(NEW_LINE);
		result.append("node [shape = circle];");
		result.append(NEW_LINE);
		
		for(DirectedEdge edge: transducer.edges()){
			DirectedEdgeWithInputOutput tempEdge = (DirectedEdgeWithInputOutput) edge;
			String inputLabel = getLabel(transducerLabelToIndex, tempEdge.getInput());
			String outputLabel = getLabel(transducerLabelToIndex, tempEdge.getOutput());
			result.append(tempEdge.from() + " -> " + tempEdge.to() + " [ label = \"" + inputLabel + "/" + outputLabel + "\" ];");
			result.append(NEW_LINE);
		}
		
		result.append("}");
		
		return result.toString();
	}
	
	public static String getLabel(Map<String, Integer> transducerLabelToIndex, int value){
		for(String key: transducerLabelToIndex.keySet()){
			if(transducerLabelToIndex.get(key) == value){
				return key;
			}
		}
		
		throw new RuntimeException("Invalid index " + value + " for input/output!");
	}
	
	public static void writeOut(String content, String fileName)
			throws FileNotFoundException {
		PrintWriter writer;
		writer = new PrintWriter(fileName);
		writer.write(content);
		writer.close();
	}
}
