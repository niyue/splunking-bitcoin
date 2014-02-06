package com.splunk.demojam.bitcoin;

import java.util.Arrays;

import org.junit.Test;

import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;

public class BlockChainListenerTest {
	private final NetworkParameters netParams = MainNetParams.get();
	
	@Test
	public void testListenBlockChain() throws BlockStoreException, InterruptedException {
		BlockChainListener blockChainListener = new LoggingBlockChainListener();
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "target/data/database/bitcoinj", 500000);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, Arrays.asList(blockChainListener), blockStore);
		blockChain.setRunScripts(false);
		PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
		peerGroup.setUserAgent("SplunkBitcoin", "0.0.1");
		peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
		peerGroup.startAsync();
		peerGroup.downloadBlockChain();
		while(true) {
			Thread.sleep(1000);
		}
	}
}
