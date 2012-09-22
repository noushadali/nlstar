package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class Photo{
	private String url;
	private String title;
	private String text;
	
	public Photo() {
	}
	
	public Photo(String url){
		this.url = url;
	}
	public Photo(String url,String title){
		this(url);
		this.title = title;
	}
	public Photo(String url, String title, String text){
		this(url, title);
		this.text = text;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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