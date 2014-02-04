package com.splunk.demojam.bitcoin.events;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.StoredTransactionOutput;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutPoint;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
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
	
	public ReceivingTransactionEvent(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset) {
		this.tx = tx;
		this.block = block;
		this.blockType = blockType;
		this.relativityOffset = relativityOffset;
	}
	
	public ReceivingTransactionEvent(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset, FullPrunedBlockStore blockStore) {
		this(tx, block, blockType, relativityOffset);
		this.blockStore = blockStore;
	}
	
	public Map<String, ? extends Object> create() throws ScriptException {
		BigInteger inValue = BigInteger.ZERO;
		List<String> inputs = new ArrayList<String>();
		for(TransactionInput input : tx.getInputs()) {
			boolean isCoinBase = input.isCoinBase();
			String scriptSig = input.getScriptSig().toString();
			BigInteger outValue = BigInteger.ZERO;
			TransactionOutPoint out = input.getOutpoint();
			if(input.getConnectedOutput() != null) {
				TransactionOutput txOut = input.getConnectedOutput();
				outValue = txOut.getValue();
			} else if(blockStore != null) {
				try {
					StoredTransactionOutput txOut = blockStore.getTransactionOutput(out.getHash(), out.getIndex());
					if(txOut != null) {
						outValue = txOut.getValue();
					}
				} catch (BlockStoreException e) {
					logger.error("Fail to get transaction output, message={}, cause={}", e.getMessage(), e.getCause());
				}
			}
			inValue = inValue.add(outValue);
			String in = String.format("script-sig=%s, is-coinbase=%s, out-hash=%s, out-index=%s, out-value=%s", scriptSig, isCoinBase, out.getHash(), out.getIndex(), outValue);
			inputs.add(in);
		}
		
		BigInteger outValue = BigInteger.ZERO;
		List<String> outputs = new ArrayList<String>();
		for(TransactionOutput output : tx.getOutputs()) {
			outValue = outValue.add(output.getValue());
			String spentBy = output.getSpentBy() == null ? "" : output.getSpentBy().getParentTransaction().getHashAsString();
			String out = String.format("value=%s, is-spent=%s, spent-by=%s", 
					Utils.bitcoinValueToFriendlyString(output.getValue()), 
					!output.isAvailableForSpending(), 
					spentBy);
			outputs.add(out);
		}
		BigInteger transactionFee = inValue.equals(BigInteger.ZERO) ? BigInteger.ZERO : inValue.subtract(outValue);
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("event", "new-transaction")
				.put("hash", tx.getHashAsString())
				.put("size", tx.getMessageSize())
				.put("sig-op-count", tx.getSigOpCount())
				.put("purpose", tx.getPurpose())
				.put("version", tx.getVersion())
				.put("time", DateFormatter.format((block.getHeader().getTime())))
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
