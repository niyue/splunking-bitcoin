package com.splunk.demojam.bitcoin;

import java.util.Map;

import org.slf4j.Logger;

import com.google.bitcoin.core.VerificationException;
import com.splunk.demojam.bitcoin.events.BlockChainEvent;

public class BlockChainEventLogger {
	private final Logger logger;
	
	public BlockChainEventLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void log(BlockChainEvent event) throws VerificationException {
		Map<String, ? extends Object> map = event.create();
		String pattern = LogPattern.of(map.keySet());
		logger.info(pattern, map.values().toArray());
	}
}
