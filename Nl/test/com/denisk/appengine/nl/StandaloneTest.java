package com.denisk.appengine.nl;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Playground class
 * @author denisk
 *
 */
public class StandaloneTest {
	@Test
	public void testNegativeDigitRegexp(){
		String str = "@-1";
		assertFalse(str.matches("@\\d"));
	}
}
