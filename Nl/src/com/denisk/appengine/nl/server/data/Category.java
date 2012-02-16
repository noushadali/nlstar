package com.denisk.appengine.nl.server.data;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.blobstore.BlobKey;

public class Category {
	public static final String KIND = "c";
	public static final String NAME = "name";
	public static final String DESCIPTION = "description";
	
	private String name;
	private String description;
	private BlobKey image;
	private BlobKey background;
	private Set<Good> goods = new HashSet<Good>();
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
	public BlobKey getBackground() {
		return background;
	}
	public void setBackground(BlobKey background) {
		this.background = background;
	}
	public Set<Good> getGoods() {
		return goods;
	}
	public void setGoods(Set<Good> goods) {
		this.goods = goods;
	}
	
	
}
