package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class Photo{
	private String url;
	private String caption;
	private String text;
	
	public Photo() {
	}
	
	public Photo(String url){
		this.url = url;
	}
	public Photo(String url,String caption){
		this(url);
		this.caption = caption;
	}
	public Photo(String url, String caption, String text){
		this(url, caption);
		this.text = text;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}