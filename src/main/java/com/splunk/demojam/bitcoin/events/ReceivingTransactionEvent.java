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
import com.google.bitcoin.script.ScriptChunk;
import com.google.bitcoin.script.ScriptOpCodes;
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
							logger.warn("Transaction input was not handle because txOutChanges were not found. block_hash={}", blockHash);
						}
					} else {
						logger.error("Fail to retrieve the block for block hash={}", blockHash);
					}
				} catch (BlockStoreException e) {
					logger.error("Fail to get transaction output, message={}, cause={}", e.getMessage(), e.getCause());
				}
				inValue = inValue.add(outValue);
			}
			String fromAddress = "coinbase";
			if(!input.isCoinBase()) {
				try {
					// NOTE: the getFromAddress API may be removed from bitcoinj later
					@SuppressWarnings("deprecation")
					String from = input.getFromAddress().toString();
					fromAddress = from;
				} catch (ScriptException e) {
					fromAddress = "unknown";
				}
			}
			Script script = input.getScriptSig();
			for(ScriptChunk chunk : script.getChunks()) {
				if (chunk.isOpCode()) {
					String opCodeName = ScriptOpCodes.getOpCodeName(chunk.data[0]);
					logger.info("time={}, event=tx_op, op_code={}, tx={}, block_height={}, script_sig={}", 
							DateFormatter.format((block.getHeader().getTime())), opCodeName, tx.getHashAsString(), block.getHeight(), input.getScriptSig());
				}
			}
			String in = String.format("is_coinbase=%s, from_address=%s, out_hash=%s, out_index=%s, out_value=%s", isCoinBase, fromAddress, out.getHash(), out.getIndex(), outValue);
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
			String out = String.format("to_address=%s, value=%s",
					toAddress,
					Utils.bitcoinValueToFriendlyString(output.getValue()));
			outputs.add(out);
		}
		BigInteger transactionFee = inValue.equals(BigInteger.ZERO) ? BigInteger.ZERO : inValue.subtract(outValue);
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("time", DateFormatter.format((block.getHeader().getTime())))
				.put("event", "new_transaction")
				.put("hash", tx.getHashAsString())
				.put("size", tx.getMessageSize())
				.put("sig_op_count", tx.getSigOpCount())
				.put("purpose", tx.getPurpose())
				.put("version", tx.getVersion())
				.put("is_coinbase", tx.isCoinBase())
				.put("is_every_output_spent", tx.isEveryOutputSpent())
				.put("is_mature", tx.isMature())
				.put("is_pending", tx.isPending())
				.put("is_time_locked", tx.isTimeLocked())
				.put("block_height", block.getHeight())
				.put("block_hash", block.getHeader().getHashAsString())
				.put("block_type", blockType)
				.put("relativity_offset", relativityOffset)
				.put("input_value", Utils.bitcoinValueToFriendlyString(inValue))
				.put("input_size", tx.getInputs().size())
				.put("output_value", Utils.bitcoinValueToFriendlyString(outValue))
				.put("output_size", tx.getOutputs().size())
				.put("fee", Utils.bitcoinValueToFriendlyString(transactionFee))
				.put("inputs", String.format("[%s]", Joiner.on(", ").join(inputs)))
				.put("outputs", String.format("[%s]", Joiner.on(", ").join(outputs)))
				.build();
		return map;
	}
}
