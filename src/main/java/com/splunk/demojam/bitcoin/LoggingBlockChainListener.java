package com.splunk.demojam.bitcoin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.VerificationException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.splunk.demojam.bitcoin.events.ReceivingTransactionEvent;
import com.splunk.demojam.bitcoin.events.ReorganizingBlockChainEvent;
import com.splunk.demojam.bitcoin.events.StoredBlockEvent;

public class LoggingBlockChainListener implements BlockChainListener {
	private static final Logger logger = LoggerFactory.getLogger(LoggingBlockChainListener.class);
	private final BlockChainEventLogger eventLogger = new BlockChainEventLogger(logger);
	private final FullPrunedBlockStore blockStore;
	private Map<Integer, Transaction> transactions = new LinkedHashMap<Integer, Transaction>();
	
	public LoggingBlockChainListener() {
		this(null);
	}
	
	public LoggingBlockChainListener(FullPrunedBlockStore blockStore) {
		this.blockStore = blockStore;
	}
	
	@Override
	public void notifyNewBestBlock(StoredBlock block)
			throws VerificationException {
		for(Entry<Integer, Transaction> entry : transactions.entrySet()) {
			eventLogger.log(new ReceivingTransactionEvent(entry.getValue(), block, NewBlockType.BEST_CHAIN, entry.getKey(), blockStore));
		}
		eventLogger.log(new StoredBlockEvent(block, transactions.size()));
		transactions = new LinkedHashMap<Integer, Transaction>();
	}

	@Override
	public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks,
			List<StoredBlock> newBlocks) throws VerificationException {
		eventLogger.log(new ReorganizingBlockChainEvent(splitPoint, oldBlocks, newBlocks));
	}

	@Override
	public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
		return true;
	}

	@Override
	public void receiveFromBlock(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset)
			throws VerificationException {
		transactions.put(relativityOffset, tx);
		if(blockType.equals(NewBlockType.SIDE_CHAIN)) {
			eventLogger.log(new ReceivingTransactionEvent(tx, block, blockType, relativityOffset, blockStore));
		}
	}

	@Override
	public void notifyTransactionIsInBlock(Sha256Hash txHash,
			StoredBlock block, NewBlockType blockType, int relativityOffset)
			throws VerificationException {
		// NOTE: not used
	}
}
