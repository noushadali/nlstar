package com.denisk.appengine.nl.server;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.server.data.Good;
import com.denisk.appengine.nl.server.data.Jsonable;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.gwt.dev.GetJreEmulation;

public class DataHandler {
	private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	private BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
	
	public Key saveCategoryWithGoods(Category category){
		Transaction tx = ds.beginTransaction();
		
		String kind = Category.KIND;
		Entity categoryEntity = new Entity(kind);

		categoryEntity.setProperty(Category.NAME, category.getName());
		categoryEntity.setProperty(Category.DESCRIPTION, category.getDescription());
		categoryEntity.setProperty(Category.IMAGE_BLOB_KEY, category.getImageBlobKey());
		categoryEntity.setProperty(Category.BACKGROUND_BLOB_KEY, category.getBackgroundBlobKey());
		
		Key key = ds.put(categoryEntity);
		
		
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
			c.setBackgroundBlobKey((String) e.getProperty(Category.BACKGROUND_BLOB_KEY));
			
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
			String imageBlobKey = (String) e.getProperty(Good.IMAGE_BLOB_KEY);
			if(imageBlobKey != null && ! imageBlobKey.isEmpty()){
				BlobKey bk = new BlobKey(imageBlobKey);
				bs.delete(bk);
			}
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

	public void updateCategoryBackground(String categoryKeyStr, String backgoundImageKeyStr) {
		Key categoryKey = KeyFactory.stringToKey(categoryKeyStr);
		Entity category;
		try {
			category = ds.get(categoryKey);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
		category.setProperty(Category.BACKGROUND_BLOB_KEY, backgoundImageKeyStr);
		ds.put(category);
		
	}

	public Entity createEntity(String kind, String parentKeyStr, Map<String, String> properties){
		Entity e;
		if(parentKeyStr == null || parentKeyStr.isEmpty()) {
			e = new Entity(kind);
		} else {
			e = new Entity(kind, KeyFactory.stringToKey(parentKeyStr));
		}
		setProperties(e, properties);
		ds.put(e);
		return e;
	}

	public void setProperties(Entity e, Map<String, String> properties) {
		for(String name: properties.keySet()){
			e.setProperty(name, properties.get(name));
		}
	}

	public Entity find(String key) throws ServletException {
		try {
			return ds.get(KeyFactory.stringToKey(key));
		} catch (EntityNotFoundException e) {
			throw new ServletException(e);
		}
	}

	public void save(Entity entity) {
		ds.put(entity);
	}

	public void deleteBlob(Entity entity, String blobField) {
		String key = (String) entity.getProperty(blobField);
		bs.delete(new BlobKey(key));
		entity.setProperty(blobField, null);
		save(entity);
	}

	public BlobKey writeJpegImage(byte[] bytes) throws IOException{
		FileService fileService = FileServiceFactory.getFileService();
		AppEngineFile file = fileService.createNewBlobFile("image/jpeg");
		FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
		OutputStream out = (Channels.newOutputStream(writeChannel));
		out.write(bytes);
		out.close();
		writeChannel.closeFinally();
		
		return fileService.getBlobKey(file);
	}
	
	public void updateBlob(Entity entity, String blobField, byte[] bytes) throws ServletException {
		String existingKey = (String) entity.getProperty(blobField);
		if(existingKey != null && !existingKey.isEmpty()){
			bs.delete(new BlobKey(existingKey));
			System.out.println("Deleting existing key " + existingKey);
		}
		
		BlobKey key;
		try {
			key = writeJpegImage(bytes);
		} catch (IOException e) {
			throw new ServletException(e);
		}
		
		entity.setProperty(blobField, key.getKeyString());
		save(entity);
	}
}
