package com.denisk.appengine.nl.server;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.util.HashSet;
import java.util.Iterator;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.server.data.Good;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class DataHandler {
	public Key saveCategoryWithGoods(Category category){
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		Transaction tx = ds.beginTransaction();
		
		Entity c = new Entity(Category.KIND);

		c.setProperty(Category.NAME, category.getName());
		c.setProperty(Category.DESCIPTION, category.getDescription());
		c.setProperty(Category.IMAGE_BLOB_KEY, category.getImageBlobKey());
		
		Key key = ds.put(c);
		
		Key categoryKey = c.getKey();
		
		for(Good good: category.getGoods()) {
			Entity g = new Entity(Good.KIND, categoryKey);
		
			g.setProperty(Good.NAME, good.getName());
			g.setProperty(Good.DESCRIPTION, good.getDescription());
			
			ds.put(g);
		}
		
		
		tx.commit();
		
		return key;
	}
	
	public int countCategories() {
		return countKind(Category.KIND);
	}
	
	public int countGoods() {
		return countKind(Good.KIND);
	}

	public HashSet<Category> getCategories(){
		HashSet<Category> result = new HashSet<Category>();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Iterator<Entity> iterator = getAllEntities(ds, Category.KIND);
		
		while(iterator.hasNext()){
			Entity e = iterator.next();
			Category c = new Category();
			c.setName((String) e.getProperty(Category.NAME));
			c.setDescription((String) e.getProperty(Category.DESCIPTION));
			c.setImageBlobKey((String) e.getProperty(Category.IMAGE_BLOB_KEY));
			
			result.add(c);
		}
		
		return result;
	}

	private Iterator<Entity> getAllEntities(DatastoreService ds, String kind) {
		return ds.prepare(new Query(kind)).asIterator();
	}
	
	private int countKind(String kind) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		int countEntities = ds.prepare(new Query(kind)).countEntities(withLimit(1000));
		return countEntities;
	}

	public String getCategoriesJson() throws JSONException{
		HashSet<Category> categories = getCategories();
		JSONStringer st = new JSONStringer();
		JSONWriter writer = st.array();
		for(Category c: categories) {
			writer = writer.object()
				.key(Category.NAME).value(c.getName())
				.key(Category.DESCIPTION).value(c.getDescription())
				.key(Category.IMAGE_BLOB_KEY).value(c.getImageBlobKey())
			.endObject();
		}
		writer = writer.endArray();
		
		return writer.toString();
	}

	public void clearAll() {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		deleteAllEntities(ds, Category.KIND);
		deleteAllEntities(ds, Good.KIND);
		
	}

	private void deleteAllEntities(DatastoreService ds, String kind) {
		final Iterator<Entity> allEntities = getAllEntities(ds, kind);
		while(allEntities.hasNext()) {
			ds.delete(allEntities.next().getKey());
		}
	}
}
