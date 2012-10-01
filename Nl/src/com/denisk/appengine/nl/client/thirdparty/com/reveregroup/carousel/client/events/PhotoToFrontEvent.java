package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events;

import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class PhotoToFrontEvent extends GwtEvent<PhotoToFrontHandler> {
	private static final Type<PhotoToFrontHandler> TYPE = new Type<PhotoToFrontHandler>();	
	
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
	protected void dispatch(PhotoToFrontHandler handler) {
		handler.photoToFront(this);
	}
	@Override
	public Type<PhotoToFrontHandler> getAssociatedType() {
		return TYPE;
	}
	public static Type<PhotoToFrontHandler> getType(){
		return TYPE;
	}
	
}
