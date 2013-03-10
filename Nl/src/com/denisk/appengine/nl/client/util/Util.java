package com.denisk.appengine.nl.client.util;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class Util {
	public static String cutPre(String imageId) {
		if (imageId.startsWith("<pre")) {
			// cut <pre>...</pre>
			imageId = imageId.substring(imageId.indexOf(">") + 1, imageId.length() - 6);
		}
		return imageId;
	}

	public static <T extends JavaScriptObject> ArrayList<T> toList(JsArray<T> arrayFromJson) {
		ArrayList<T> items = new ArrayList<T>();
		for(int i = 0; i < arrayFromJson.length(); i++){
			items.add(arrayFromJson.get(i));
		}
		return items;
	}
}
