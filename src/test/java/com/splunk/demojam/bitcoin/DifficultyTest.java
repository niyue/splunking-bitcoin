package com.splunk.demojam.bitcoin;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DifficultyTest {

	@Test
	public void testInitialDifficulty() {
		assertThat(Difficulty.convert(486604799), closeTo(1.0, 0.01));
	}
	
	@Test
	public void testRecentDifficulty() {
		assertThat(Difficulty.convert(419587686), closeTo(1789546951.05, 0.01));
	}
}
