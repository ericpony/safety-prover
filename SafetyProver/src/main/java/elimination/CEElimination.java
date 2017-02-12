package elimination;

import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import encoding.AutomataEncoding;
import encoding.ISatSolver;
import encoding.RankingFunction;
import encoding.TransducerEncoding;
import common.Tuple;
import org.sat4j.specs.ContradictionException;

import java.util.List;

public class CEElimination {
    private ISatSolver solver;

    public CEElimination(ISatSolver solver) {
        this.solver = solver;
    }

    public void ce0Elimination(AutomataEncoding automataEncoding,
                               List<Integer> w) throws ContradictionException {
        solver.addClause(new int[]{automataEncoding.acceptWord(w)});
        /*
        WordAcceptance wordAcceptance = new WordAcceptance(automataEncoding);
		int acceptW = wordAcceptance.encode(w);
		int[] clause = new int[] { acceptW };
		solver.addClause(clause);
	    */
    }

    public void ce1Elimination(AutomataEncoding automataEncoding,
                               Tuple<List<Integer>> cex) throws ContradictionException {
        //		WordAcceptance wordXAcceptance = new WordAcceptance(automataEncoding);
        //		int acceptX = wordXAcceptance.encodeNeg(x);
        //		WordAcceptance wordYAcceptance = new WordAcceptance(automataEncoding);
        //		int acceptY = wordYAcceptance.encode(y);
        int acceptX = automataEncoding.acceptWord(cex.x);
        int acceptY = automataEncoding.acceptWord(cex.y);

        int[] clause = new int[]{-acceptX, acceptY};
        solver.addClause(clause);
    }

    public void ce2Elimination(AutomataEncoding automataAEncoding,
                               AutomataEncoding automataBEncoding, List<Integer> w)
            throws ContradictionException {
        WordAcceptance wordAcceptanceA = new WordAcceptance(automataAEncoding);
        int acceptA = wordAcceptanceA.encodeNeg(w);
        WordAcceptance wordAcceptanceB = new WordAcceptance(automataBEncoding);
        int acceptB = wordAcceptanceB.encode(w);
        int[] clause = new int[]{-acceptA, acceptB};
        solver.addClause(clause);
    }

    public void ce3Elimination(TransducerEncoding transducerEncoding,
                               TransitivityPairSet transitivitySet,
                               List<List<Integer>> counterExamples)
            throws ContradictionException {
        for (int i = 0; i < counterExamples.size() - 1; ++i)
            transitivitySet.addPair(counterExamples.get(i),
                    counterExamples.get(i + 1));

        if (counterExamples.size() == 3)
            transitivitySet.addPair(counterExamples.get(0),
                    counterExamples.get(counterExamples.size() - 1));
        else
            transitivitySet.fixTransitivityCEX(counterExamples);

		/*
        PairAcceptance pairAcceptance = new PairAcceptance(transducerEncoding);
		int w1w2 = pairAcceptance.encodeNeg(w1, w2);
		int w2w3 = pairAcceptance.encodeNeg(w2, w3);
		int w1w3 = pairAcceptance.encodePos(w1, w3);

		int[] clause = new int[] { -w1w2, -w2w3, w1w3 };
		solver.addClause(clause);
		*/
    }

    public void ce4Elimination(AutomataEncoding automataBEncoding,
                               TransducerEncoding transducerEncoding,
                               TransitivityPairSet transitivitySet,
                               RankingFunction rankingFunctionEncoding,
                               List<List<Integer>> counterExamples,
                               Automata F, EdgeWeightedDigraph player2)
            throws ContradictionException {
        CE4Elimination elim =
                new CE4Elimination(automataBEncoding,
                        transducerEncoding, transitivitySet,
                        rankingFunctionEncoding,
                        F, player2, counterExamples.get(0),
                        counterExamples.get(1));
        elim.encode();
    }

    public void ce5Elimination(RankingFunction rankingFunctionEncoding,
                               List<List<Integer>> counterExamples)
            throws ContradictionException {
        PairAcceptance pairAcceptance = new PairAcceptance(rankingFunctionEncoding);
        int w0w1 = pairAcceptance.encodeNeg(counterExamples.get(0), counterExamples.get(1));
        int w0w2 = pairAcceptance.encodeNeg(counterExamples.get(0), counterExamples.get(2));

        int[] clause = new int[]{-w0w1, -w0w2};
        solver.addClause(clause);
    }
}
