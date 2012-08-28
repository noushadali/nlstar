package com.denisk.appengine.nl.client.overlay;

import com.denisk.appengine.nl.server.data.Category;

public class CategoryJavascriptObject extends ShopItem {
	protected CategoryJavascriptObject() {}
	
	public final native String getBackgroundBlobKey() /*-{return this.backgroundBlobKey}-*/;
	public final native void setBackgroundBlobKey(String backgroundBlobKey) /*-{this.backgroundBlobKey = backgroundBlobKey}-*/;
	
	@Override
	public String getKind(){
		return Category.KIND;
	}
}
