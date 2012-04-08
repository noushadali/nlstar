package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class Photo{
	private String url;
	private String caption;
	
	public Photo() {
	}
	
	public Photo(String url){
		this.url = url;
	}
	public Photo(String url,String caption){
		this.url = url;
		this.caption = caption;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}