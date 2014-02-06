package com.splunk.demojam.bitcoin;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.utils.BlockFileLoader;

public class BlockChainLoaderTest {
	private static final Logger logger = LoggerFactory.getLogger(BlockChainLoaderTest.class);
	private final NetworkParameters netParams = MainNetParams.get();

	@Test
	public void testLoadBootstrapDataFile() throws Exception {
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "./src/vagrant/splunk/assets/outputs/database/bitcoinj", 500000);
		BlockChainListener blockChainListener = new LoggingBlockChainListener(blockStore);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, Arrays.asList(blockChainListener), blockStore);
		blockChain.setRunScripts(false);
		
		File bootstrapFile = new File("./src/vagrant/splunk/assets/bootstrap.dat");
		if(bootstrapFile.exists()) {
			logger.info("Found bootstrap.dat file, start to load blocks from that.");
			BlockFileLoader loader = new BlockFileLoader(netParams, Arrays.asList(bootstrapFile));
			
			for (Block block : loader) {
				blockChain.add(block);
			}
		} else {
			fail("Bootstrap.dat file is not found.");
		}
	}
}
