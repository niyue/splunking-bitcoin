package com.splunk.demojam.bitcoin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.MemoryBlockStore;
import com.google.bitcoin.utils.BriefLogFormatter;

public class FetchGenesisBlockTest {
	private final static Logger logger = LoggerFactory.getLogger(FetchGenesisBlockTest.class); 
	final NetworkParameters params = MainNetParams.get();
	
	@Test
	public void testFetchingGenesisBlock() throws Exception {
		BriefLogFormatter.init();
		logger.info("Connecting to node");

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        logger.info("Starting peer group");
        
        peerGroup = new PeerGroup(params, null /* no chain */);
        peerGroup.setUserAgent("SplunkBitcoin", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        
        peerGroup.startAsync(); 
        logger.info("Waiting for peers");
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        logger.info("Getting block");
        String genesisBlockHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        Sha256Hash blockHash = new Sha256Hash(genesisBlockHash);
        Future<Block> future = peer.getBlock(blockHash);
        logger.info("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        logger.info("block={}", block);
        List<Transaction> transactions = block.getTransactions();
        // List<TransactionInput> inputs = transactions.get(0).getInputs();
        List<TransactionOutput> outputs = transactions.get(0).getOutputs();
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0).getValue(), is(BigInteger.valueOf(5000000000L)));
        peerGroup.stopAsync();
	}
	
	@Test
	public void testFetchingTenThousandsBtcPizzaBlock() throws Exception {
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
        logger.info("block={}", block);
        logger.info("block_difficulty={}", Difficulty.convert(block.getDifficultyTarget()));
        List<Transaction> transactions = block.getTransactions();
        assertThat(transactions.size(), is(2));
        Transaction coinbaseTx = transactions.get(0);
        assertThat(coinbaseTx.isCoinBase(), is(true));
        Transaction pizzaTx = transactions.get(1);
        assertThat(pizzaTx.isCoinBase(), is(false));
        List<TransactionOutput> outputs = pizzaTx.getOutputs();
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0).getValue(), is(BigInteger.valueOf(1000000000000L)));
        peerGroup.stopAsync();
	}
}
