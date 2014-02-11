package com.splunk.demojam.bitcoin;

public class Difficulty {
	// translated from GetDifficulty RPC in https://github.com/bitcoin/bitcoin/blob/master/src/rpcblockchain.cpp
	public static final double convert(long difficultyTarget) {
		long nBits = difficultyTarget;
		int nShift = (int) ((nBits >> 24) & 0xff);

	    double dDiff =
	        (double)0x0000ffff / (double)(nBits & 0x00ffffff);

	    while (nShift < 29) {
	        dDiff *= 256.0;
	        nShift++;
	    }
	    while (nShift > 29) {
	        dDiff /= 256.0;
	        nShift--;
	    }

	    return dDiff;
	}
}
