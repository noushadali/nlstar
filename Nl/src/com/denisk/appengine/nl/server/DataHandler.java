package com.denisk.appengine.nl.server;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.server.data.Good;
import com.denisk.appengine.nl.server.data.Jsonable;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.gwt.dev.GetJreEmulation;

public class DataHandler {
	private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	
	public Key saveCategoryWithGoods(Category category){
		Transaction tx = ds.beginTransaction();
		
		String kind = Category.KIND;
		Entity c = new Entity(kind);

		c.setProperty(Category.NAME, category.getName());
		c.setProperty(Category.DESCRIPTION, category.getDescription());
		c.setProperty(Category.IMAGE_BLOB_KEY, category.getImageBlobKey());
		
		Key key = ds.put(c);
		
		
		for(Jsonable good: category.getGoods()) {
			doPersistGood(ds, key, good);
		}
		
		
		tx.commit();
		
		return key;
	}

	private Key doPersistGood(DatastoreService ds, Key parentKey, Jsonable good) {
		Entity g = new Entity(Good.KIND, parentKey);

		g.setProperty(Jsonable.NAME, good.getName());
		g.setProperty(Jsonable.DESCRIPTION, good.getDescription());
		g.setProperty(Jsonable.IMAGE_BLOB_KEY, good.getImageBlobKey());
		
		return ds.put(g);
	}
	
	public int countCategories() {
		return countKind(Category.KIND);
	}
	
	public int countGoods() {
		return countKind(Good.KIND);
	}

	public ArrayList<Category> getCategories(){
		ArrayList<Category> result = new ArrayList<Category>();
		Iterator<Entity> iterator = getAllEntities(ds, Category.KIND);
		
		while(iterator.hasNext()){
			Entity e = iterator.next();
			Category c = new Category();
			setCommonJsonableProperties(e, c);
			
			result.add(c);
		}
		
		return result;
	}

	private void setCommonJsonableProperties(Entity e, Jsonable c) {
		c.setKey(e.getKey());
		c.setName((String) e.getProperty(Jsonable.NAME));
		c.setDescription((String) e.getProperty(Jsonable.DESCRIPTION));
		c.setImageBlobKey((String) e.getProperty(Jsonable.IMAGE_BLOB_KEY));
	}

	public ArrayList<Good> getGoods(Key categoryKey){
		ArrayList<Good> goods = new ArrayList<Good>();
		Iterable<Entity> goodEntities = getGoodEntities(categoryKey);
		
		for(Entity goodEntity: goodEntities){
			Good good = new Good();
			setCommonJsonableProperties(goodEntity, good);
			goods.add(good);
		}
		
		return goods;
	}
	
	private Iterator<Entity> getAllEntities(DatastoreService ds, String kind) {
		return ds.prepare(new Query(kind)).asIterator();
	}
	
	private int countKind(String kind) {
		int countEntities = ds.prepare(new Query(kind)).countEntities(withLimit(1000));
		return countEntities;
	}

	public String getCategoriesJson() throws JSONException{
		ArrayList<Category> categories = getCategories();
		return getItemsJson(categories);
	}

	private String getItemsJson(Iterable<? extends Jsonable> categories)
			throws JSONException {
		JSONStringer st = new JSONStringer();
		JSONWriter writer = st.array();
		for(Jsonable c: categories) {
			writer = writer.value(new JSONObject(c.toJson()));
		}
		writer = writer.endArray();
		
		return writer.toString();
	}

	public String getGoodsJson(String categoryKeyStr) {
		Key key = KeyFactory.stringToKey(categoryKeyStr);
		ArrayList<Good> goods = getGoods(key);
		try {
			return getItemsJson(goods);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void clearAll() {
		deleteAllEntities(ds, Category.KIND);
		deleteAllEntities(ds, Good.KIND);
		
	}

	private void deleteAllEntities(DatastoreService ds, String kind) {
		final Iterator<Entity> allEntities = getAllEntities(ds, kind);
		while(allEntities.hasNext()) {
			ds.delete(allEntities.next().getKey());
		}
	}

	public void clearGoodsForCategory(String categoryKeyStr) {
		Key key = KeyFactory.stringToKey(categoryKeyStr);
		Iterable<Entity> iterable = getGoodEntities(key);
		
		for(Entity e: iterable){
			ds.delete(e.getKey());
		}
	}

	private Iterable<Entity> getGoodEntities(Key category) {
		return ds.prepare(new Query(Good.KIND, category)).asIterable();
	}

	public Key persistGood(String goodJson) {
		Good fromJson = new Good().getFromJson(goodJson);
		Key categoryKey = KeyFactory.stringToKey(fromJson.getParentKeyStr());
		return doPersistGood(ds, categoryKey, fromJson);
	}

}
