package com.splunk.demojam.bitcoin.events;

import java.util.Map;

import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.StoredBlock;
import com.google.bitcoin.core.VerificationException;
import com.google.common.collect.ImmutableMap;
import com.splunk.demojam.bitcoin.DateFormatter;
import com.splunk.demojam.bitcoin.Difficulty;

public class StoredBlockEvent implements BlockChainEvent {
	private final StoredBlock block;
	private final int txCount;
	
	public StoredBlockEvent(StoredBlock block, int txCount) {
		this.block = block;
		this.txCount = txCount;
	}
	
	public Map<String, ? extends Object> create() throws VerificationException {
		Block header = block.getHeader();
		Map<String, ? extends Object> map = ImmutableMap.<String, Object>builder()
				.put("time", DateFormatter.format(header.getTime()))
				.put("event", "new-best-block")
				.put("hash", header.getHashAsString())
				.put("height", block.getHeight())
				.put("chain-work", block.getChainWork())
				.put("previous", header.getPrevBlockHash())
				.put("inflation", header.getBlockInflation(block.getHeight()))
				.put("merkle-root", header.getMerkleRoot())
				.put("difficulty-target", header.getDifficultyTarget())
				.put("difficulty", Difficulty.convert(header.getDifficultyTarget()))
				.put("size", header.getMessageSize())
				.put("nounce", header.getNonce())
				.put("version", header.getVersion())
				.put("work", header.getWork())
				.put("tx-count", txCount)
				.build();
		return map;
	}
}
