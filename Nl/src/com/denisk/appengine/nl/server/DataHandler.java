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

public class DataHandler {
	public Key saveCategoryWithGoods(Category category){
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		Transaction tx = ds.beginTransaction();
		
		Entity c = new Entity(Category.KIND);

		c.setProperty(Category.NAME, category.getName());
		c.setProperty(Category.DESCIPTION, category.getDescription());
		
		Key key = ds.put(c);
		
		Key cKey = c.getKey();
		
		for(Good good: category.getGoods()) {
			Entity g = new Entity(Good.KIND, cKey);
		
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
		Iterator<Entity> iterator = ds.prepare(new Query(Category.KIND)).asIterator();
		
		while(iterator.hasNext()){
			Entity e = iterator.next();
			Category c = new Category();
			c.setName((String) e.getProperty(Category.NAME));
			c.setDescription((String) e.getProperty(Category.DESCIPTION));
			
			result.add(c);
		}
		
		return result;
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
			.endObject();
		}
		writer = writer.endArray();
		
		return writer.toString();
	}
}
