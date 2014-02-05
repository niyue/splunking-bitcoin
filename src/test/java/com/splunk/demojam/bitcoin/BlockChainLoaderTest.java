package com.splunk.demojam.bitcoin;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.utils.BlockFileLoader;

public class BlockChainLoaderTest {
	private final NetworkParameters netParams = MainNetParams.get();

	@Test
	public void testLoadBootstrapDataFile() throws Exception {
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "outputs/database/bitcoinj", 500000);
		BlockChainListener blockChainListener = new LoggingBlockChainListener(blockStore);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, Arrays.asList(blockChainListener), blockStore);
		blockChain.setRunScripts(false);

		BlockFileLoader loader = new BlockFileLoader(netParams, Arrays.asList(new File("/Users/sni/Library/Application Support/Bitcoin/bootstrap.dat")));

		for (Block block : loader) {
			blockChain.add(block);
		}
	}
}
