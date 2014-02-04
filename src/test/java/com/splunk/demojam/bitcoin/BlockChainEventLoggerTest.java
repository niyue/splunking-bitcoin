package com.splunk.demojam.bitcoin;

import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.store.MemoryBlockStore;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.splunk.demojam.bitcoin.events.BlockChainEvent;
import com.splunk.demojam.bitcoin.events.ReceivingTransactionEvent;
import com.splunk.demojam.bitcoin.events.StoredBlockEvent;

public class BlockChainEventLoggerTest {
	private final static Logger logger = LoggerFactory.getLogger(BlockChainEventLoggerTest.class); 
	private final NetworkParameters params = MainNetParams.get();
	
	
	@Test
	public void testLogBlock() throws Exception {
		BriefLogFormatter.init();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        
        peerGroup = new PeerGroup(params, null);
        peerGroup.setUserAgent("SplunkBitcoin", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        
        peerGroup.startAsync(); 
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        String blockHashString = "00000000152340ca42227603908689183edc47355204e7aca59383b0aaac1fd8";
        Sha256Hash blockHash = new Sha256Hash(blockHashString);
        Future<Block> future = peer.getBlock(blockHash);
        logger.info("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        BlockChainEventLogger eventLogger = new BlockChainEventLogger(logger);
        eventLogger.log(new StoredBlockEvent(new StoredBlock(block, block.getWork(), 21000), block.getTransactions().size()));
	}
	
	@Test
	public void testLogTransaction() throws Exception {
		BriefLogFormatter.init();

		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(params, "data/bitcoin-blocks.db", 500000);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(params,  blockStore);
		// FullPrunedBlockStore blockStore = new MemoryFullPrunedBlockStore(params, 500000);
		// FullPrunedBlockChain blockChain = new FullPrunedBlockChain(params,  blockStore);
        PeerGroup peerGroup = new PeerGroup(params, blockChain);
        
        peerGroup = new PeerGroup(params, null);
        peerGroup.setUserAgent("SplunkBitcoin", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        
        peerGroup.startAsync(); 
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        String blockHashString = "00000000152340ca42227603908689183edc47355204e7aca59383b0aaac1fd8";
        Sha256Hash blockHash = new Sha256Hash(blockHashString);
        Future<Block> future = peer.getBlock(blockHash);
        logger.info("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        BlockChainEventLogger eventLogger = new BlockChainEventLogger(logger);
        
        Transaction tx = block.getTransactions().get(1);
        BlockChainEvent event = new ReceivingTransactionEvent(tx, new StoredBlock(block, block.getWork(), 21000), NewBlockType.BEST_CHAIN, 1, blockStore);
        eventLogger.log(event);
	}

}
