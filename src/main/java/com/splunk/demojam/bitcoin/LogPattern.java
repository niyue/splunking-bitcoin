package com.splunk.demojam.bitcoin;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public final class LogPattern {
	private static Function<String, String> kvPairing = new Function<String, String>() {
		public String apply(String s) {
			return String.format("%s={}", s);
		}
	};

	public static String of(Set<String> keys) {
		return Joiner.on(", ").join(Iterables.transform(keys, kvPairing));
	}
}
