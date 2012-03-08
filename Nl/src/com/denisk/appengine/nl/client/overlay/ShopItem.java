package com.denisk.appengine.nl.client.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class ShopItem extends JavaScriptObject {

	protected ShopItem() {
		super();
	}
	
	public final native String getKeyStr() /*-{return this.keyStr}-*/;

	public final native String getName() /*-{return this.name}-*/;
	
	public final native String getDescription() /*-{return this.description}-*/;
	
	public final native String getImageKey() /*-{return this.imageKey}-*/;
	
	public final native void setName(String name) /*-{this.name = name}-*/;
	
	public final native void setDescription(String description) /*-{this.description = description}-*/;
	
	public final native void setImageKey(String imageKey) /*-{this.imageKey = imageKey}-*/;

	public static native <T extends ShopItem> JsArray<T> getArrayFromJson(String jsonStr)/*-{
		return eval(jsonStr);
	}-*/;

	public final native String toJson() /*-{return JSON.stringify(this)}-*/;

}