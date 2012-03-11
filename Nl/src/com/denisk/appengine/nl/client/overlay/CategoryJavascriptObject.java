package com.denisk.appengine.nl.client.overlay;

public class CategoryJavascriptObject extends ShopItem {
	protected CategoryJavascriptObject() {}
	
	public final native String getBackgroundBlobKey() /*-{return this.backgroundBlobKey}-*/;
	public final native void setBackgroundBlobKey(String backgroundBlobKey) /*-{this.backgroundBlobKey = backgroundBlobKey}-*/;
}
