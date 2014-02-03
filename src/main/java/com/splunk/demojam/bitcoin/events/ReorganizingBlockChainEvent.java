package com.splunk.demojam.bitcoin.events;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.bitcoin.core.StoredBlock;
import com.google.common.collect.ImmutableMap;
import com.splunk.demojam.bitcoin.DateFormatter;

public class ReorganizingBlockChainEvent implements BlockChainEvent {
	private Map<String, ? extends Object> map;
	
	public ReorganizingBlockChainEvent(StoredBlock splitPoint, List<StoredBlock> oldBlocks,
			List<StoredBlock> newBlocks) {
		StoredBlock oldBlockHead = oldBlocks.get(0);
		StoredBlock newBlockHead = oldBlocks.get(0);
		map = ImmutableMap.<String, Object>builder()
				.put("event", "reorganize-block-chain")
				.put("time", DateFormatter.format(new Date()))
				.put("split-point-height", splitPoint.getHeight())
				.put("split-point-hash", splitPoint.getHeader().getHashAsString())
				.put("split-point-time", DateFormatter.format(splitPoint.getHeader().getTime()))
				.put("old-block-height", oldBlockHead.getHeight())
				.put("old-block-hash", oldBlockHead.getHeader().getHashAsString())
				.put("old-block-time", DateFormatter.format(oldBlockHead.getHeader().getTime()))
				.put("new-block-height", newBlockHead.getHeight())
				.put("new-block-hash", newBlockHead.getHeader().getHashAsString())
				.put("new-block-time", DateFormatter.format(newBlockHead.getHeader().getTime()))
				.build();
	}
	
	public Map<String, ? extends Object> create() {
		return map;
	}
}
