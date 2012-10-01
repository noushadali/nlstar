package com.denisk.appengine.nl.server.data;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public abstract class Jsonable<T extends Jsonable<?>> {

	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String KEY_STR = "keyStr";
	public static final String PARENT_KEY_STR = "parentKeyStr";
	public static final String IMAGE_BLOB_KEY = "imageBlobKey";
	
	protected String name;
	protected String description;
	
	private Key key;
	private String imageBlobKey;
	private String parentKeyStr;

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

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getImageBlobKey() {
		return imageBlobKey;
	}

	public void setImageBlobKey(String imageBlobKey) {
		this.imageBlobKey = imageBlobKey;
	}

	public String getParentKeyStr() {
		return parentKeyStr;
	}

	public void setParentKeyStr(String parentKeyStr) {
		this.parentKeyStr = parentKeyStr;
	}

	public T getFromJson(String json) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json);
			
			T jsonable = instance();
			jsonable.setName(jsonObject.getString(NAME));
			jsonable.setDescription(jsonObject.getString(DESCRIPTION));
			jsonable.setImageBlobKey(jsonObject.getString(IMAGE_BLOB_KEY));
			jsonable.setParentKeyStr(jsonObject.getString(PARENT_KEY_STR));
			
			setAddtionalPropertiesOnCategory(jsonable, jsonObject);
			return jsonable;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public String toJson() {
		try {
			JSONWriter writer = new JSONStringer().object();
			if(key != null) {
				writer = writer.key(Jsonable.KEY_STR).value(KeyFactory.keyToString(key));
			}
			JSONWriter value = writer
			.key(Category.NAME).value(getName())
			.key(Category.DESCRIPTION).value(getDescription())
			.key(Category.PARENT_KEY_STR).value(getParentKeyStr())
			.key(Category.IMAGE_BLOB_KEY).value(getImageBlobKey());
			
			value = addAdditionalPropertiesToJson(value);
			String string = value.endObject().toString();
			
			return string;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected JSONWriter addAdditionalPropertiesToJson(JSONWriter value) throws JSONException {
		return value;
	}
	protected void setAddtionalPropertiesOnCategory(T jsonable, JSONObject jsonObject) throws JSONException {
	}


	protected abstract T instance();

}
