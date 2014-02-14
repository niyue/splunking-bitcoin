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
			try {
				eventLogger.log(new ReceivingTransactionEvent(entry.getValue(), block, NewBlockType.BEST_CHAIN, entry.getKey(), blockStore));
			} catch (Exception e) {
				logger.error("Fail to handle transaction in new block, error=%s, block_height=%s, transaction=%s, error_message=%s", 
						"transaction_error", block.getHeight(), entry.getValue().getHashAsString(), e.getMessage());
				e.printStackTrace();
			}
		}
		try {
			eventLogger.log(new StoredBlockEvent(block, transactions.size()));
		} catch (Exception e) {
			logger.error("Fail to handle new best block, error=%s, block_hash=%s, block_height=%s", 
					"best_block_error", block.getHeader().getHashAsString(), block.getHeight());
		}
		transactions = new LinkedHashMap<Integer, Transaction>();
	}

	@Override
	public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks,
			List<StoredBlock> newBlocks) throws VerificationException {
		try {
			eventLogger.log(new ReorganizingBlockChainEvent(splitPoint, oldBlocks, newBlocks));
		} catch (Exception e) {
			logger.error("Fail to handle block chain reorganize, error=%s, split_point=%s, old_block_height=%s, new_block_height=%s, error_message=%s", 
					"reorganize_error", splitPoint, oldBlocks.size(), newBlocks.size(), e.getMessage());
		}
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
			try {
				eventLogger.log(new ReceivingTransactionEvent(tx, block, blockType, relativityOffset, blockStore));
			}  catch (Exception e) {
				logger.error("Fail to handle transaction in a side chain block, error=%s, block_height=%s, transaction=%s, error_message=%s", 
						"side_chain_transaction_error", block.getHeight(), tx.getHashAsString(), e.getMessage());
			}
		}
	}

	@Override
	public void notifyTransactionIsInBlock(Sha256Hash txHash,
			StoredBlock block, NewBlockType blockType, int relativityOffset)
			throws VerificationException {
		// NOTE: not used
	}
}
