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
	
	public final native String getImageBlobKey() /*-{return this.imageBlobKey}-*/;
	
	public final native String getParentKeyStr() /*-{return this.parentKeyStr}-*/;
	
	public final native void setName(String name) /*-{this.name = name}-*/;
	
	public final native void setDescription(String description) /*-{this.description = description}-*/;
	
	public final native void setImageBlobKey(String imageBlobKey) /*-{this.imageBlobKey = imageBlobKey}-*/;
	
	public final native void setParentKeyStr(String parentKeyStr) /*-{this.parentKeyStr = parentKeyStr}-*/;

	public static native <T extends ShopItem> JsArray<T> getArrayFromJson(String jsonStr)/*-{
		return eval(jsonStr);
	}-*/;

	public final native String toJson() /*-{return JSON.stringify(this)}-*/;

}