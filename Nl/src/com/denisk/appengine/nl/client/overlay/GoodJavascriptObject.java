package com.denisk.appengine.nl.client.overlay;

import com.denisk.appengine.nl.server.data.Good;

public class GoodJavascriptObject extends ShopItem {
	protected GoodJavascriptObject() {}
	
	public String getKind(){
		return Good.KIND;
	}
}
