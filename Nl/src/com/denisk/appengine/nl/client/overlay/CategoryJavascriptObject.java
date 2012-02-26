package com.denisk.appengine.nl.client.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class CategoryJavascriptObject extends JavaScriptObject {
	protected CategoryJavascriptObject() {
		
	}

	public final native String getName() /*-{return this.name}-*/;
	
	public final native String getDescription() /*-{return this.description}-*/;
	
	public final native String getImageKey() /*-{return this.imageKey}-*/;

	public static native JsArray<CategoryJavascriptObject> getArrayFromJson(String jsonStr)/*-{
		return eval(jsonStr);
	}-*/;

}
