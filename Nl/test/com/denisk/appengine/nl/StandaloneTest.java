package com.denisk.appengine.nl;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.denisk.appengine.nl.server.ImageCacheService;

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
	
	@Test
	public void testMd5(){
		String s = "AMIfv96JIDl1uaqP-WffUn8_oFsepop3PcS8cuowcHgeinLLML0DMDUe3VQ44-C3V4wWakslglgKX3nQsUM0e3I6euRQF9SzqD9dr2PkcP1ab9FTYjic19uO8bHZUz3GB30P-PmuOZ0dOx-15tGQv4CPROHucBoTHg";
		String md5 = ImageCacheService.md5(s);
		
		System.out.println(md5);
	}
}
