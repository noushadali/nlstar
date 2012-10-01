package com.denisk.appengine.nl.server.data;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONWriter;

public class Category extends Jsonable<Category> {
	public static final String KIND = "c";
	public static final String BACKGROUND_BLOB_KEY = "backgroundBlobKey";

	private String backgroundBlobKey;
	private Set<Good> goods = new HashSet<Good>();
	
	public String getBackgroundBlobKey() {
		return backgroundBlobKey;
	}
	public void setBackgroundBlobKey(String backgroundBlobKey) {
		this.backgroundBlobKey = backgroundBlobKey;
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
	@Override
	protected Category instance() {
		return new Category();
	}
	@Override
	protected JSONWriter addAdditionalPropertiesToJson(JSONWriter value) throws JSONException {
		return value.key(BACKGROUND_BLOB_KEY).value(getBackgroundBlobKey());
	}
	@Override
	protected void setAddtionalPropertiesOnCategory(Category jsonable, JSONObject jsonObject) throws JSONException {
		jsonable.setBackgroundBlobKey(jsonObject.getString(BACKGROUND_BLOB_KEY));
	}
	
}
