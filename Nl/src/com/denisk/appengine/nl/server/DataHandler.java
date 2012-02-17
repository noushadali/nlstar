package com.denisk.appengine.nl.server;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
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

	private int countKind(String kind) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		int countEntities = ds.prepare(new Query(kind)).countEntities(withLimit(1000));
		return countEntities;
	}
}
