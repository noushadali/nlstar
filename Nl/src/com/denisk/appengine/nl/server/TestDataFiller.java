package com.denisk.appengine.nl.server;

import java.util.Random;

import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.server.data.Good;

public class TestDataFiller {
	private final static String DICTIONARY = "abcde1234!";
	private DataHandler dh = new DataHandler();
	
	public void createTestDataSet(int categoryCount, int goodCount){
		for(int i=0; i< categoryCount; i++){
			Category c = new Category();
			c.setName("Category_" + getRandomString(5));
			c.setDescription("some description");
			
			for (int k = 0; k < goodCount; k++){
				Good g = new Good();
				g.setName("Good_" + getRandomString(3));
				c.getGoods().add(g);
			}
			dh.saveCategoryWithGoods(c);
		}
	}
	
	private String getRandomString(int length){
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		for(int i = 0; i < length; i++){
			sb.append(DICTIONARY.charAt(r.nextInt(DICTIONARY.length())));
		}
		
		return sb.toString();
	}

	public DataHandler getDataHandler() {
		return dh;
	}
}
