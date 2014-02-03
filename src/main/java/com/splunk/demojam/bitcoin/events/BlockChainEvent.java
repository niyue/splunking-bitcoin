package com.splunk.demojam.bitcoin.events;

import java.util.Map;

import com.google.bitcoin.core.VerificationException;

public interface BlockChainEvent {
	/**
	 * Create a new block chain event
	 * @return the newly created block chain event
	 */
	public Map<String, ? extends Object> create() throws VerificationException;
}
