package com.denisk.appengine.nl.client.util;

public class Util {
	public static String cutPre(String imageId) {
		if (imageId.startsWith("<pre")) {
			// cut <pre>...</pre>
			imageId = imageId.substring(imageId.indexOf(">") + 1, imageId.length() - 6);
		}
		return imageId;
	}
}
