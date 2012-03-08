package com.denisk.appengine.nl.server.data;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

public class Category {
	public static final String KIND = "c";
	public static final String KEY_STR = "keyStr";
	public static final String NAME = "name";
	public static final String DESCIPTION = "description";
	public static final String IMAGE_BLOB_KEY = "imageKey";
	
	private Key key;
	private String name;
	private String description;
	private String imageBlobKey;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public String getImageBlobKey() {
		return imageBlobKey;
	}
	public void setImageBlobKey(String imageBlobKey) {
		this.imageBlobKey = imageBlobKey;
	}
	
	public static Category getFromJson(String json) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json);
			
			Category category = new Category();
			category.setName(jsonObject.getString(NAME));
			category.setDescription(jsonObject.getString(DESCIPTION));
			category.setImageBlobKey(jsonObject.getString(IMAGE_BLOB_KEY));
			
			return category;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	public Key getKey() {
		return key;
	}
	public void setKey(Key key){
		this.key = key;
	}
	
}
