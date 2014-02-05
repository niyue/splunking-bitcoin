package com.splunk.demojam.bitcoin;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;

public class BlockChainListenerTest {
	private final NetworkParameters netParams = MainNetParams.get();
	
	@Test
	public void testListenBlockChain() throws BlockStoreException, InterruptedException {
		BlockChainListener blockChainListener = new LoggingBlockChainListener();
		BlockStore blockStore = new SPVBlockStore(netParams, new File("spv-blocks"));
		BlockChain blockChain = new BlockChain(netParams, Arrays.asList(blockChainListener), blockStore);
		// FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "full-blocks.db", 500000);
		// FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, Arrays.asList(blockChainListener), blockStore);
		PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
		peerGroup.setUserAgent("SplunkBitcoin", "0.0.1");
		peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
		// the time in official bitcoin bootstrap.dat (uploaded in 2014-01-11)
		// https://blockchain.info/block-height/279000
		// DateTime block279000Time = new DateTime(2014, 1, 6, 22, 31, 11);
		// peerGroup.setFastCatchupTimeSecs(block279000Time.getMillis());
		peerGroup.startAsync();
		peerGroup.downloadBlockChain();
		while(true) {
			Thread.sleep(1000);
		}
	}
}
