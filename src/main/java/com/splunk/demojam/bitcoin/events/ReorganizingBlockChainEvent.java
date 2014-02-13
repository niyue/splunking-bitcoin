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
				.put("time", DateFormatter.format(new Date()))
				.put("event", "reorganize_block_chain")
				.put("split_point_height", splitPoint.getHeight())
				.put("split_point_hash", splitPoint.getHeader().getHashAsString())
				.put("split_point_time", DateFormatter.format(splitPoint.getHeader().getTime()))
				.put("old_block_height", oldBlockHead.getHeight())
				.put("old_block_hash", oldBlockHead.getHeader().getHashAsString())
				.put("old_block_time", DateFormatter.format(oldBlockHead.getHeader().getTime()))
				.put("new_block_height", newBlockHead.getHeight())
				.put("new_block_hash", newBlockHead.getHeader().getHashAsString())
				.put("new_block_time", DateFormatter.format(newBlockHead.getHeader().getTime()))
				.build();
	}
	
	public Map<String, ? extends Object> create() {
		return map;
	}
}
