package com.splunk.demojam.bitcoin;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.StoredUndoableBlock;
import com.google.bitcoin.core.TransactionOutputChanges;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;

public class BlockChainExplorerTest {
	final NetworkParameters netParams = MainNetParams.get();
	private final Logger logger = LoggerFactory.getLogger(BlockChainExplorerTest.class);
	
	@Test
	public void testGetChainHead() throws BlockStoreException {
		FullPrunedBlockStore blockStore = new H2FullPrunedBlockStore(netParams, "data/bitcoin-blocks.db", 500000);
		FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams,  blockStore);
		logger.info("best-chain-height={}", blockChain.getBestChainHeight());
		assertThat(blockChain.getBestChainHeight(), greaterThan(0));
		StoredBlock headBlock = blockChain.getChainHead();
		assertThat(headBlock, notNullValue());
		// Work is a measure of how many tries are needed to solve a block
		logger.info("chain-work={}", headBlock.getChainWork());
		StoredBlock secondBlock = headBlock.getPrev(blockStore);
		assertThat(secondBlock, notNullValue());
		
		Sha256Hash headHash = headBlock.getHeader().getHash();
		StoredUndoableBlock block = blockStore.getUndoBlock(headHash);
		TransactionOutputChanges outputChanges = block.getTxOutChanges(); 
		assertThat(outputChanges, notNullValue());
	}

}
