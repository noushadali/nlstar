package com.denisk.appengine.nl.server;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class ImageCacheService {
	public static final String KEY_DELIM = "#";
	private static MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
	private static ImagesService imageService = ImagesServiceFactory.getImagesService();
	
	public Image getImage(BlobKey key, int w, int h){
		if(key == null){
			throw new IllegalArgumentException("BlobKey was null");
		}

		Image image = null;
		Transform lucky = ImagesServiceFactory.makeImFeelingLucky();
		
		if(w <= 0 || h <= 0){
			w = -1;
			h = -1;
			image = (Image) memcacheService.get(key.getKeyString());
			if(image == null){
				checkBlobExists(key);
				image = ImagesServiceFactory.makeImageFromBlob(key);
				//we need to apply any transform to the image, otherwise the image data will remain null
				image = imageService.applyTransform(lucky, image);
				memcacheService.put(key.getKeyString(), image);
			}
		} else {
			String combinedKey = buildCombinedKey(key, w, h);
			
			image = (Image) memcacheService.get(combinedKey);  
			if(image != null){
				return image;
			}
			
			checkBlobExists(key);
			
			image = ImagesServiceFactory.makeImageFromBlob(key);
			if(image == null) {
				System.out.println("Key doesn't point to any image: " + key.getKeyString());
				return null;
			}
			
			image = imageService.applyTransform(lucky, image);
	
			Transform resize = ImagesServiceFactory.makeResize(w, h);
			image = imageService.applyTransform(resize, image);
			
			memcacheService.put(combinedKey, image);
		}
		
		return image;
	}

	private void checkBlobExists(BlobKey key){
		if(new BlobInfoFactory().loadBlobInfo(key) == null){
			throw new IllegalArgumentException("Blob does not exist for key: " + key.getKeyString());
		}
	}
	
	public String buildCombinedKey(BlobKey key, int w, int h) {
		return key.getKeyString() + KEY_DELIM + w + KEY_DELIM + h;
	}

}
