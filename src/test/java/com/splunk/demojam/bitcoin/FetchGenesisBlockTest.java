package com.splunk.demojam.bitcoin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Test;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.MemoryBlockStore;
import com.google.bitcoin.utils.BriefLogFormatter;

public class FetchGenesisBlockTest {
	final NetworkParameters netParams = MainNetParams.get();
	
	BlockStore blockStore = new MemoryBlockStore(netParams);
	
	BlockChain chain;
	
	@Test
	public void testFetchingGenesisBlock() throws Exception {
		BriefLogFormatter.init();
        System.out.println("Connecting to node");
        final NetworkParameters params = MainNetParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        System.out.println("Starting peer group");
        
        peerGroup = new PeerGroup(params, null /* no chain */);
        peerGroup.setUserAgent("PeerMonitor", "1.0");
        peerGroup.setMaxConnections(4);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        
        peerGroup.startAsync(); 
        System.out.println("Waiting for peers");
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        System.out.println("Getting block");
        String genesisBlockHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        Sha256Hash blockHash = new Sha256Hash(genesisBlockHash);
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        List<Transaction> transactions = block.getTransactions();
        // List<TransactionInput> inputs = transactions.get(0).getInputs();
        List<TransactionOutput> outputs = transactions.get(0).getOutputs();
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0).getValue(), is(BigInteger.valueOf(5000000000L)));
        peerGroup.stopAsync();
	}
	
	@Test
	public void testFetchingBlock() throws Exception {
		BriefLogFormatter.init();
        System.out.println("Connecting to node");
        final NetworkParameters params = MainNetParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        System.out.println("Starting peer group");
        
        peerGroup = new PeerGroup(params, null /* no chain */);
        peerGroup.setUserAgent("PeerMonitor", "1.0");
        peerGroup.setMaxConnections(4);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        
        peerGroup.startAsync(); 
        System.out.println("Waiting for peers");
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        System.out.println("Getting block");
        String genesisBlockHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        Sha256Hash blockHash = new Sha256Hash(genesisBlockHash);
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        List<Transaction> transactions = block.getTransactions();
        // List<TransactionInput> inputs = transactions.get(0).getInputs();
        List<TransactionOutput> outputs = transactions.get(0).getOutputs();
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0).getValue(), is(BigInteger.valueOf(5000000000L)));
        peerGroup.stopAsync();
	}
	
}
