package com.splunk.demojam.bitcoin.events;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.StoredTransactionOutput;
import com.google.bitcoin.core.StoredUndoableBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutPoint;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.splunk.demojam.bitcoin.DateFormatter;

public class ReceivingTransactionEvent implements BlockChainEvent {
	private final Transaction tx;
	private final StoredBlock block;
	private final NewBlockType blockType;
	private final int relativityOffset;
	private FullPrunedBlockStore blockStore;
	private final static Logger logger = LoggerFactory.getLogger(ReceivingTransactionEvent.class);
	private static int outputIndex = 0;
	private static Sha256Hash lastBlockHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
	private static final NetworkParameters netParams = MainNetParams.get();
	
	public ReceivingTransactionEvent(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset) {
		this.tx = tx;
		this.block = block;
		this.blockType = blockType;
		this.relativityOffset = relativityOffset;
		if(isTransactionInNewBlock()) {
			lastBlockHash = block.getHeader().getHash();
			outputIndex = 0;
		}
	}
	
	public ReceivingTransactionEvent(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset, FullPrunedBlockStore blockStore) {
		this(tx, block, blockType, relativityOffset);
		this.blockStore = blockStore;
	}
	
	private boolean isTransactionInNewBlock() {
		return !lastBlockHash.equals(block.getHeader().getHash());
	}
	
	
	public Map<String, ? extends Object> create() throws ScriptException {
		BigInteger inValue = BigInteger.ZERO;
		List<String> inputs = new ArrayList<String>();
		for(TransactionInput input : tx.getInputs()) {
			boolean isCoinBase = input.isCoinBase();
			TransactionOutPoint out = input.getOutpoint();
			BigInteger outValue = BigInteger.ZERO;
			Sha256Hash blockHash = block.getHeader().getHash();
			if(blockStore != null && !tx.isCoinBase()) {
				try {
					StoredUndoableBlock undoableBlock = blockStore.getUndoBlock(blockHash);
					if(undoableBlock != null && undoableBlock.getTxOutChanges() != null) {
						if(undoableBlock.getTxOutChanges() != null) {
							List<StoredTransactionOutput> outputs = undoableBlock.getTxOutChanges().txOutsSpent;
							StoredTransactionOutput txOut = outputs.get(outputIndex);
							outputIndex++;
							if(txOut != null) {
								outValue = txOut.getValue();
							}
						} else {
							logger.warn("Transaction input was not handle because txOutChanges were not found. block-hash={}", blockHash);
						}
					} else {
						logger.error("Fail to retrieve the block for block hash={}", blockHash);
					}
				} catch (BlockStoreException e) {
					logger.error("Fail to get transaction output, message={}, cause={}", e.getMessage(), e.getCause());
				}
				inValue = inValue.add(outValue);
			}
			@SuppressWarnings("deprecation")
			// NOTE: the getFromAddress API may be removed from bitcoinj later
			String fromAddress = input.isCoinBase() ? "coinbase" : input.getFromAddress().toString();
			String in = String.format("is-coinbase=%s, from-address=%s, out-hash=%s, out-index=%s, out-value=%s", isCoinBase, fromAddress, out.getHash(), out.getIndex(), outValue);
			inputs.add(in);
		}
		
		BigInteger outValue = BigInteger.ZERO;
		List<String> outputs = new ArrayList<String>();
		for(TransactionOutput output : tx.getOutputs()) {
			outValue = outValue.add(output.getValue());
			Script scriptPubKey = output.getScriptPubKey();
			String toAddress = "NA";
			if(scriptPubKey.isSentToAddress() || scriptPubKey.isSentToP2SH()) {
				toAddress = scriptPubKey.getToAddress(netParams).toString();
			}
			String out = String.format("to-address=%s, value=%s",
					toAddress,
					Utils.bitcoinValueToFriendlyString(output.getValue()));
			outputs.add(out);
		}
		BigInteger transactionFee = inValue.equals(BigInteger.ZERO) ? BigInteger.ZERO : inValue.subtract(outValue);
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("time", DateFormatter.format((block.getHeader().getTime())))
				.put("event", "new-transaction")
				.put("hash", tx.getHashAsString())
				.put("size", tx.getMessageSize())
				.put("sig-op-count", tx.getSigOpCount())
				.put("purpose", tx.getPurpose())
				.put("version", tx.getVersion())
				.put("is-coinbase", tx.isCoinBase())
				.put("is-every-output-spent", tx.isEveryOutputSpent())
				.put("is-mature", tx.isMature())
				.put("is-pending", tx.isPending())
				.put("is-time-locked", tx.isTimeLocked())
				.put("block-height", block.getHeight())
				.put("block-hash", block.getHeader().getHashAsString())
				.put("block-type", blockType)
				.put("relativity-offset", relativityOffset)
				.put("input-value", Utils.bitcoinValueToFriendlyString(inValue))
				.put("input-size", tx.getInputs().size())
				.put("output-value", Utils.bitcoinValueToFriendlyString(outValue))
				.put("output-size", tx.getOutputs().size())
				.put("fee", Utils.bitcoinValueToFriendlyString(transactionFee))
				.put("inputs", String.format("[%s]", Joiner.on(", ").join(inputs)))
				.put("outputs", String.format("[%s]", Joiner.on(", ").join(outputs)))
				.build();
		return map;
	}
}
