package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Image;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class CarouselImage extends Image{
	private int originalHeight = 0;
	private int originalWidth = 0;
	
	private static Map<String, int[]> sizeCache = new HashMap<String, int[]>();
	
	@Override
	public void setUrl(String url) {
		super.setUrl(url);
		int[] dimension = sizeCache.get(url);
		if (dimension == null) {
			originalWidth = 0;
			originalHeight = 0;
		} else {
			originalWidth = dimension[0];
			originalHeight = dimension[1];
		}
	}
	
	public void sizeToBounds(int maxWidth, int maxHeight) {
		if (originalWidth == 0) {
			setSize("", "");
			originalWidth = getWidth();
			originalHeight = getHeight();
			if (originalWidth != 0) {
				int[] dimension = new int[2];
				dimension[0] = originalWidth;
				dimension[1] = originalHeight;
				sizeCache.put(getUrl(), dimension);
			} else {
				setSize(Integer.toString(maxWidth), Integer.toString(maxHeight));
				return;
			}
		}
		
		double aspectRatio = originalHeight / originalWidth;
		double containerAR = ((double)maxHeight) / ((double)maxWidth);
		
		if (aspectRatio >= containerAR){
			//limit height
			setSize("", Integer.toString(maxHeight));
		} else {
			//limit width
			setSize(Integer.toString(maxWidth), "");
		}
	}

	public int getOriginalHeight() {
		return originalHeight;
	}

	public int getOriginalWidth() {
		return originalWidth;
	}
	
	public void setOpacity(double opacity) {
		if (opacity > .995) {
			getElement().getStyle().setProperty("opacity", "");
			getElement().getStyle().setProperty("filter", "");
			getElement().getStyle().setProperty("-moz-opacity", "");
			getElement().getStyle().setProperty("-khtml-opacity", "");
		} else {
			String s = Integer.toString((int) Math.round(opacity * 100));
			String sDecimal = (s.length() == 1 ? ".0" : ".") + s;
			getElement().getStyle().setProperty("opacity", sDecimal);
			getElement().getStyle().setProperty("filter", "alpha(opacity=" + s + ")");
			getElement().getStyle().setProperty("-moz-opacity", sDecimal);
			getElement().getStyle().setProperty("-khtml-opacity", sDecimal);
		}
	}
	
}
