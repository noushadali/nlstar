package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events;

import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class PhotoFocusEvent extends GwtEvent<PhotoFocusHandler> {
	private static final Type<PhotoFocusHandler> TYPE = new Type<PhotoFocusHandler>();	
	
	private Photo photo;
	private int photoIndex;
	
	public Photo getPhoto() {
		return photo;
	}
	public void setPhoto(Photo photo) {
		this.photo = photo;
	}
	public int getPhotoIndex() {
		return photoIndex;
	}
	public void setPhotoIndex(int photoIndex) {
		this.photoIndex = photoIndex;
	}	
	@Override
	protected void dispatch(PhotoFocusHandler handler) {
		handler.photoFocused(this);
	}
	@Override
	public Type<PhotoFocusHandler> getAssociatedType() {
		return TYPE;
	}
	public static Type<PhotoFocusHandler> getType(){
		return TYPE;
	}
	
}
