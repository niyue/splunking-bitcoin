package com.splunk.demojam.bitcoin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AbstractBlockChain.NewBlockType;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChainListener;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.VerificationException;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class LoggingBlockChainListener implements BlockChainListener {
	private final Logger logger = LoggerFactory.getLogger(LoggingBlockChainListener.class);
	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
	private int txCount = 0;
	
	@Override
	public void notifyNewBestBlock(StoredBlock block)
			throws VerificationException {
		Block header = block.getHeader();
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("event", "new-best-block")
				.put("hash", header.getHashAsString())
				.put("height", block.getHeight())
				.put("chain-work", block.getChainWork())
				.put("previous", header.getPrevBlockHash())
				.put("inflation", header.getBlockInflation(block.getHeight()))
				.put("merkle-root", header.getMerkleRoot())
				.put("difficulty", header.getDifficultyTarget())
				.put("size", header.getMessageSize())
				.put("nounce", header.getNonce())
				.put("time", formatDate(header.getTime()))
				.put("version", header.getVersion())
				.put("work", header.getWork())
				.put("tx-count", txCount)
				.build();
		String pattern = LogPattern.of(map.keySet());
		logger.info(pattern, map.values().toArray());
		txCount = 0;
	}

	@Override
	public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks,
			List<StoredBlock> newBlocks) throws VerificationException {
		StoredBlock oldBlockHead = oldBlocks.get(0);
		StoredBlock newBlockHead = oldBlocks.get(0);
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("event", "reorganize-block-chain")
				.put("time", formatDate(new Date()))
				.put("split-point-height", splitPoint.getHeight())
				.put("split-point-hash", splitPoint.getHeader().getHashAsString())
				.put("split-point-time", formatDate(splitPoint.getHeader().getTime()))
				.put("old-block-height", oldBlockHead.getHeight())
				.put("old-block-hash", oldBlockHead.getHeader().getHashAsString())
				.put("old-block-time", formatDate(oldBlockHead.getHeader().getTime()))
				.put("new-block-height", newBlockHead.getHeight())
				.put("new-block-hash", newBlockHead.getHeader().getHashAsString())
				.put("new-block-time", formatDate(newBlockHead.getHeader().getTime()))
				.build();
		String pattern = LogPattern.of(map.keySet());
		logger.info(pattern, map.values().toArray());
	}

	@Override
	public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
		return true;
	}

	@Override
	public void receiveFromBlock(Transaction tx, StoredBlock block,
			NewBlockType blockType, int relativityOffset)
			throws VerificationException {
		txCount++;
		BigInteger inValue = BigInteger.ZERO;
		List<String> inputs = new ArrayList<String>();
		for(TransactionInput input : tx.getInputs()) {
			boolean isCoinBase = input.isCoinBase();
			String scriptSig = input.getScriptSig().toString();
			TransactionOutput txOut = input.getConnectedOutput();
			// inValue = inValue.add(txOut.getValue());
			String in = String.format("script-sig=%s, is-coinbase=%s", scriptSig, isCoinBase);
			inputs.add(in);
		}
		
		BigInteger outValue = BigInteger.ZERO;
		List<String> outputs = new ArrayList<String>();
		for(TransactionOutput output : tx.getOutputs()) {
			outValue = outValue.add(output.getValue());
			String spentBy = output.getSpentBy() == null ? "null" : output.getSpentBy().getParentTransaction().getHashAsString();
			String out = String.format("value=%s, is-spent=%s, spent-by=%s", 
					Utils.bitcoinValueToFriendlyString(output.getValue()), 
					!output.isAvailableForSpending(), 
					spentBy);
			outputs.add(out);
		}
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("event", "new-transaction")
				.put("hash", tx.getHashAsString())
				.put("size", tx.getMessageSize())
				.put("sig-op-count", tx.getSigOpCount())
				.put("purpose", tx.getPurpose())
				.put("version", tx.getVersion())
				.put("time", formatDate(block.getHeader().getTime()))
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
				.put("inputs", String.format("[%s]", Joiner.on(", ").join(inputs)))
				.put("outputs", String.format("[%s]", Joiner.on(", ").join(outputs)))
				.build();
		String pattern = LogPattern.of(map.keySet());
		logger.info(pattern, map.values().toArray());
	}

	@Override
	public void notifyTransactionIsInBlock(Sha256Hash txHash,
			StoredBlock block, NewBlockType blockType, int relativityOffset)
			throws VerificationException {
		// NOTE: not used
	}
	
	private String formatDate(Date date) {
		return formatter.print(new DateTime(date));
	}
}
