package verification;

import common.VerificationUtility;
import common.bellmanford.EdgeWeightedDigraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TransitivitiyChecking {
    private static final Logger LOGGER = LogManager.getLogger();

    private EdgeWeightedDigraph transducer;
    private int numLetters;

    public TransitivitiyChecking(EdgeWeightedDigraph transducer, int numLetters) {
        this.transducer = transducer;
        this.numLetters = numLetters;
    }

    /*
     * return 3 words w1, w2, w3
     */
    public List<List<Integer>> check() {
        EdgeWeightedDigraph composition = VerificationUtility.computeSquare(transducer);

        //no need to call because it does not contain empty transition
//		if(!VerificationUltility.isDFA(composition, numLetters)){
//			composition = VerificationUltility.toDFA(composition, numLetters);
//		}

        if (!VerificationUtility.isDFA(transducer, numLetters)) {
            transducer = VerificationUtility.toDFA(transducer, numLetters);
        }

        if (!VerificationUtility.isComplete(transducer, numLetters)) {
            transducer = VerificationUtility.makeComplete(transducer, numLetters);
        }

        List<int[]> counterExample = L2TransducerInclusionChecking.findShortestCounterExample(composition, transducer);
        return VerificationUtility.convertToWords(counterExample, 3);
    }


}
