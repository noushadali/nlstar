package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events;

import com.google.gwt.event.shared.EventHandler;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public interface PhotoToFrontHandler extends EventHandler{
	public void photoToFront(PhotoToFrontEvent event);
}
