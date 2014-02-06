package com.splunk.demojam.bitcoin;

import org.junit.Test;

import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;

public class BlockChainDownloadTest {
	final NetworkParameters netParams = MainNetParams.get();
	
	@Test
	public void testDownload() throws BlockStoreException {
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "target/data/bitcoin-blocks", 500000);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams,  blockStore);
		PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
		peerGroup.setUserAgent("SplunkBitcoin", "0.0.1");
		peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
		peerGroup.startAsync();
		peerGroup.downloadBlockChain();
		peerGroup.stopAsync();
	}
}
