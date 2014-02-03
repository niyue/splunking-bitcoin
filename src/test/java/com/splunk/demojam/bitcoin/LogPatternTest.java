package com.splunk.demojam.bitcoin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class LogPatternTest {

	@Test
	public void testComposingLogPattern() {
		Set<String> keys = ImmutableSet.of("ka", "kb", "kc");
		String pattern = LogPattern.of(keys);
		assertThat(pattern, is("ka={}, kb={}, kc={}"));
	}

}
