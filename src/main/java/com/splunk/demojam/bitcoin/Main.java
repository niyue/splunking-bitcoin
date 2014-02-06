package com.splunk.demojam.bitcoin;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.utils.BlockFileLoader;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final NetworkParameters netParams = MainNetParams.get();
	
	public static void main(String[] args) throws Exception {
		logger.info("Start to load blocks from bootstrap data file");
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "target/outputs/database/bitcoinj", 500000);
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
			logger.info("Blocks from bootstrap.dat were loaded.");
		} else {
			logger.warn("bootstrap.dat file is not found, the initial syncing will be much slower.");
		}
		
		blockChain.setRunScripts(true);
		PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
		peerGroup.setUserAgent("SplunkBitcoin", "0.0.1");
		peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
		peerGroup.startAsync();
		// TODO: keep it running without exiting
		while(true) {
			logger.info("Start to listen to Bitcoin network and download block chains.");
			peerGroup.downloadBlockChain();
			peerGroup.awaitTerminated();
			logger.info("All block chains were downloaded, wait for next block chain to appear.");
			Thread.sleep(30000);
		}
	}
}
