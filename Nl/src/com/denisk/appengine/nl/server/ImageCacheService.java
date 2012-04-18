package com.denisk.appengine.nl.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class ImageCacheService {
	public static final String KEY_DELIM = "#";
	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 115;
	private static final int MAX_WIDTH = 500;
	private static final int MAX_HEIGHT = 300;

	private static MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
	private static ImagesService imageService = ImagesServiceFactory.getImagesService();
	
	public Image getImage(BlobKey key, int w, int h){
		if(key == null){
			throw new IllegalArgumentException("BlobKey was null");
		}
		if(w <= 0){
			throw new IllegalArgumentException("width should be positive, was " + w);
		}
		if(h <= 0){
			throw new IllegalArgumentException("height should be positive, was " + h);
		}

//		if (w > MAX_WIDTH) {
//			System.out.println("Width " + w + " exceeded limit " + MAX_WIDTH + ", setting to " + DEFAULT_WIDTH);
//			w = DEFAULT_WIDTH;
//		}
//
//		if (h > MAX_HEIGHT) {
//			System.out.println("Height " + h + " exceeded limit " + MAX_HEIGHT + ", setting to " + DEFAULT_HEIGHT);
//			h = DEFAULT_HEIGHT;
//		}

		String combinedKey = buildCombinedKey(key, w, h);
		
		Image resizedImage = (Image) memcacheService.get(combinedKey);
		if(resizedImage != null){
			return resizedImage;
		}
		
		Image image = ImagesServiceFactory.makeImageFromBlob(key);
		if(image == null) {
			System.out.println("Key doesn't point to any image: " + key.getKeyString());
			return null;
		}
		
		Transform resize = ImagesServiceFactory.makeResize(w, h);
		Transform lucky = ImagesServiceFactory.makeImFeelingLucky();
		resizedImage = imageService.applyTransform(resize, image);
		resizedImage = imageService.applyTransform(lucky, resizedImage);
		memcacheService.put(combinedKey, resizedImage);
		
		return resizedImage;
	}

	public String buildCombinedKey(BlobKey key, int w, int h) {
		return key.getKeyString() + KEY_DELIM + w + KEY_DELIM + h;
	}

}
