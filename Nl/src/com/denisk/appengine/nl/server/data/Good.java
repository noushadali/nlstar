package com.denisk.appengine.nl.server.data;

import com.google.appengine.api.blobstore.BlobKey;

public class Good {
	public static final String KIND = "g";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	
	private String name;
	private String description;
	private BlobKey image;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BlobKey getImage() {
		return image;
	}
	public void setImage(BlobKey image) {
		this.image = image;
	}
}
