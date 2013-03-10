package com.denisk.appengine.nl.server;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.apphosting.datastore.DatastoreV4.FilterOrBuilder;

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
		
		
		for(Jsonable<?> good: category.getGoods()) {
			doPersistGood(ds, key, good);
		}
		
		
		tx.commit();
		
		return key;
	}

	private Key doPersistGood(DatastoreService ds, Key parentKey, Jsonable<?> good) {
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
		Iterator<Entity> iterator = getAllEntities(ds, Category.KIND);

		return getCategories(iterator);
	}

	public ArrayList<Category> getCategories(Iterator<Entity> iterator) {
		ArrayList<Category> result = new ArrayList<Category>();
		while(iterator.hasNext()){
			Entity e = iterator.next();
			Category c = categoryFromEntity(e);
			
			result.add(c);
		}
		
		return result;
	}

	private Category categoryFromEntity(Entity e) {
		Category c = new Category();
		setCommonJsonableProperties(e, c);
		c.setBackgroundBlobKey((String) e.getProperty(Category.BACKGROUND_BLOB_KEY));
		
		return c;
	}

	private void setCommonJsonableProperties(Entity e, Jsonable<?> c) {
		c.setKey(e.getKey());
		c.setName((String) e.getProperty(Jsonable.NAME));
		c.setDescription(((Text) e.getProperty(Jsonable.DESCRIPTION)));
		c.setImageBlobKey((String) e.getProperty(Jsonable.IMAGE_BLOB_KEY));
	}

	public ArrayList<Good> getGoods(Key categoryKey){
		Iterable<Entity> goodEntities = getGoodEntities(categoryKey);

		return goodsFromEntities(goodEntities);
	}

	public ArrayList<Good> goodsFromEntities(Iterable<Entity> goodEntities) {
		ArrayList<Good> goods = new ArrayList<Good>();
		for(Entity goodEntity: goodEntities){
			Good good = new Good();
			setCommonJsonableProperties(goodEntity, good);
			//set parent
			good.setParentKeyStr(KeyFactory.keyToString(goodEntity.getParent()));
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

	/**
	 * Gets JSON string from a collection of goods or categories
	 */
	private String getItemsJson(Iterable<? extends Jsonable<?>> items)
			throws JSONException {
		JSONStringer st = new JSONStringer();
		JSONWriter writer = st.array();
		for(Jsonable<?> i: items) {
			writer = writer.value(new JSONObject(i.toJson()));
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
	
	/**
	 * Gets JSON string for all goods in the system
	 */
	public String getAllGoodsJson() throws JSONException{
		Iterable<Entity> allGoods = getAllGoodEntities();
		ArrayList<Good> goodsFromEntities = goodsFromEntities(allGoods);
		return getItemsJson(goodsFromEntities);
	}
	
	private Iterable<Entity> getGoodEntities(Key category) {
		return ds.prepare(new Query(Good.KIND, category)).asIterable();
	}

	private Iterable<Entity> getAllGoodEntities(){
		return ds.prepare(new Query(Good.KIND)).asIterable();
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
			if (! name.equals("description")) {
				e.setProperty(name, properties.get(name));
			} else {
				e.setProperty(name, new Text(properties.get(name)));
			}
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

	public void deleteGood(String key, String imageKey){
		Transaction tx = ds.beginTransaction();
		delete(key, tx);
		tx.commit();
		deleteImage(imageKey);
	}
	
	public void deleteCategory(String key, String imageKey, String backgroundKey){
		if(key == null || key.isEmpty()){
			return;
		}
		Transaction tx = ds.beginTransaction();
		for(Entity good: getGoodEntities(KeyFactory.stringToKey(key))){
			String goodImageKey = (String) good.getProperty(Good.IMAGE_BLOB_KEY);
			if(goodImageKey != null && ! goodImageKey.isEmpty()){
				deleteImage(goodImageKey);
			}
			ds.delete(tx, good.getKey());
		}
		delete(key, tx);
		
		tx.commit();
		if (imageKey != null && ! imageKey.isEmpty()) {
			deleteImage(imageKey);
		}
		if (backgroundKey != null && ! backgroundKey.isEmpty()) {
			deleteImage(backgroundKey);
		}
	}
	
	private void delete(String key, Transaction tx) {
		if(key != null && ! key.isEmpty()){
			ds.delete(tx, KeyFactory.stringToKey(key));
		}
	}

	private void deleteImage(String imageKey) {
		if(imageKey != null && !imageKey.isEmpty()){
			bs.delete(new BlobKey(imageKey));
		}
	}

	public String getCategoryBackgroundKey(String categoryKey) {
		Entity category;
		try {
			category = ds.get(KeyFactory.stringToKey(categoryKey));
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}

		return (String) category.getProperty(Category.BACKGROUND_BLOB_KEY);
	}

	public String getAllCategoriesExcept(String categoryKeyStr) throws JSONException {
		Iterable<Entity> categories = getCategoryEntitiesExcept(categoryKeyStr);

		return getItemsJson(getCategories(categories.iterator()));
	}

	public Iterable<Entity> getCategoryEntitiesExcept(String categoryKeyStr) {
		Query.Filter filter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.NOT_EQUAL, KeyFactory.stringToKey(categoryKeyStr));
		Iterable<Entity> categories = ds.prepare(new Query(Category.KIND).setFilter(filter)).asIterable();
		return categories;
	}
}
