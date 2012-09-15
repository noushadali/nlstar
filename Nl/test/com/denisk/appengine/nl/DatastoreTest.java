package com.denisk.appengine.nl;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denisk.appengine.nl.server.DataHandler;
import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.server.data.Good;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatastoreTest {
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());
	private final LocalServiceTestHelper helper1 = new LocalServiceTestHelper(
			new LocalBlobstoreServiceTestConfig());
	private DatastoreService ds;
	private DataHandler dh;

	@Before
	public void before() {
		helper.setUp();
		helper1.setUp();
		ds = DatastoreServiceFactory.getDatastoreService();
		dh = new DataHandler();
	}

	@After
	public void after() {
		// helper1.tearDown();
		helper.tearDown();
	}

	@Test
	public void datastoreTest() {
		final String kind = "yum";
		assertEquals(0, ds.prepare(new Query(kind))
				.countEntities(withLimit(10)));
		ds.put(new Entity(kind));
		ds.put(new Entity(kind));
		assertEquals(2, ds.prepare(new Query(kind))
				.countEntities(withLimit(10)));
	}

	@Test
	public void dataHandlerBasicTest() throws EntityNotFoundException {

		Category c = new Category();
		c.setName("hello");
		c.setDescription("desc");

		Good g1 = new Good();
		Good g2 = new Good();

		g1.setName("g1_name");
		g2.setName("g2_name");

		g1.setName("g1_desc");
		g2.setDescription("g2_desc");

		c.getGoods().add(g1);
		c.getGoods().add(g2);

		assertEquals(
				0,
				ds.prepare(new Query(Category.KIND)).countEntities(
						withLimit(10)));
		assertEquals(0,
				ds.prepare(new Query(Good.KIND)).countEntities(withLimit(10)));

		Key key = dh.saveCategoryWithGoods(c);

		assertEquals(
				1,
				ds.prepare(new Query(Category.KIND)).countEntities(
						withLimit(10)));
		assertEquals(2,
				ds.prepare(new Query(Good.KIND)).countEntities(withLimit(10)));

		ds.get(key);
	}

	@Test
	public void getCategories() {

		Category c = new Category();
		c.setName("hello");
		c.setDescription("desc");

		Category c1 = new Category();
		c1.setName("another");
		c1.setDescription("desc");

		Good g1 = new Good();
		Good g2 = new Good();

		g1.setName("g1_name");
		g2.setName("g2_name");

		g1.setName("g1_desc");
		g2.setDescription("g2_desc");

		c.getGoods().add(g1);
		c.getGoods().add(g2);

		Key key1 = dh.saveCategoryWithGoods(c);
		Key key2 = dh.saveCategoryWithGoods(c1);

		ArrayList<Category> categories = dh.getCategories();

		assertEquals(2, categories.size());
		assertTrue(categories.contains(c));
		assertTrue(categories.contains(c1));
	}

	@Test
	public void getCategoriesJson() throws JSONException {
		Category c = new Category();
		c.setName("hello");
		c.setDescription("desc");

		Category c1 = new Category();
		c1.setName("another");
		c1.setDescription("desc");

		Good g1 = new Good();
		Good g2 = new Good();

		g1.setName("g1_name");
		g2.setName("g2_name");

		g1.setDescription("g1_desc");
		g2.setDescription("g2_desc");

		c.getGoods().add(g1);
		c.getGoods().add(g2);

		Key key1 = dh.saveCategoryWithGoods(c);
		Key key2 = dh.saveCategoryWithGoods(c1);

		final String categoriesJson = dh.getCategoriesJson();
		System.out.println(categoriesJson);

		assertTrue(categoriesJson.contains("another"));
		assertTrue(categoriesJson.contains("hello"));
	}

	@Test
	public void getGoodsJson() {
		Category c = new Category();
		c.setName("hello");
		c.setDescription("desc");

		Category c1 = new Category();
		c1.setName("another");
		c1.setDescription("desc");

		Good g1 = new Good();
		Good g2 = new Good();
		Good g3 = new Good();

		g1.setName("g1_name");
		g2.setName("g2_name");
		g3.setName("g3_name");

		g1.setDescription("g1_desc");
		g2.setDescription("g2_desc");
		g3.setDescription("g3_desc");

		c.getGoods().add(g1);
		c.getGoods().add(g2);

		c1.getGoods().add(g3);

		Key key1 = dh.saveCategoryWithGoods(c);
		Key key2 = dh.saveCategoryWithGoods(c1);

		String json = dh.getGoodsJson(KeyFactory.keyToString(key1));

		assertTrue(json.contains("g1_name"));
		assertTrue(json.contains("g2_name"));
		assertTrue(json.contains("g1_desc"));
		assertTrue(json.contains("g2_desc"));

		assertFalse(json.contains("g3_name"));
		assertFalse(json.contains("g3_desc"));
	}

	@Test
	public void clearGoodsForCategory() {
		Category c = new Category();
		c.setName("hello");
		c.setDescription("desc");

		Category c1 = new Category();
		c1.setName("another");
		c1.setDescription("desc");

		Good g1 = new Good();
		Good g2 = new Good();
		Good g3 = new Good();

		g1.setName("g1_name");
		g2.setName("g2_name");
		g3.setName("g3_name");

		g1.setDescription("g1_desc");
		g2.setDescription("g2_desc");
		g3.setDescription("g3_desc");

		c.getGoods().add(g1);
		c.getGoods().add(g2);

		c1.getGoods().add(g3);

		Key key1 = dh.saveCategoryWithGoods(c);
		Key key2 = dh.saveCategoryWithGoods(c1);

		String key1Str = KeyFactory.keyToString(key1);
		String key2Str = KeyFactory.keyToString(key2);

		dh.clearGoodsForCategory(key1Str);

		String goodsJson = dh.getGoodsJson(key1Str);
		assertFalse(goodsJson.contains("g1_name"));
		assertFalse(goodsJson.contains("g2_name"));

		goodsJson = dh.getGoodsJson(key2Str);
		assertTrue(goodsJson.contains("g3_name"));
	}

	@Test
	public void persistGood() {
		Category c = new Category();
		Key categoryKey = dh.saveCategoryWithGoods(c);

		Good g = new Good();
		String name = "hello";
		String descr = "descr";
		String imageKey = "otnuotenuh";

		g.setName(name);
		g.setDescription(descr);
		g.setImageBlobKey(imageKey);
		g.setParentKeyStr(KeyFactory.keyToString(categoryKey));

		Key key = dh.persistGood(g.toJson());

		assertEquals(1,
				ds.prepare(new Query(Good.KIND)).countEntities(withLimit(10)));

		List<Entity> list = ds.prepare(new Query(Good.KIND)).asList(
				withLimit(10));

		assertEquals(1, list.size());
		Entity saved = list.get(0);

		assertEquals(name, saved.getProperty(Good.NAME));
		assertEquals(descr, saved.getProperty(Good.DESCRIPTION));
		assertEquals(imageKey, saved.getProperty(Good.IMAGE_BLOB_KEY));
		assertEquals(name, saved.getProperty(Good.NAME));
	}

	@Test
	public void updateCategoryBackgroundImage() throws IOException {
		String imageKeyStr = createImage();

		Category c = new Category();
		String categoryKeyStr = KeyFactory.keyToString(dh
				.saveCategoryWithGoods(c));

		dh.updateCategoryBackground(categoryKeyStr, imageKeyStr);

		ArrayList<Category> categories = dh.getCategories();

		assertEquals(1, categories.size());
		assertEquals(imageKeyStr, categories.get(0).getBackgroundBlobKey());
	}

	@Test
	public void updatingExistingEntity() {
		Entity e = new Entity("hello");
		assertFalse(e.getKey().isComplete());
		ds.put(e);
		assertTrue(e.getKey().isComplete());
	}

	@Test
	public void testDeleteGood() throws Exception {
		Entity good = new Entity(Good.KIND);
		String imageKey = createImage();
		good.setProperty(Good.IMAGE_BLOB_KEY, imageKey);
		Key k = ds.put(good);

		assertNotNull(ds.get(k));

		dh.deleteGood(KeyFactory.keyToString(k), imageKey);

		assertEntityDoesNotExist(k);

		assertImageDoesNotExist(imageKey);
	}

	@Test
	public void deleteCategory() throws Throwable {
		Entity category = new Entity(Category.KIND);

		String categoryImageKey = createImage();
		String categoryBackgroundKey = createImage();

		category.setProperty(Category.IMAGE_BLOB_KEY, categoryImageKey);
		category.setProperty(Category.BACKGROUND_BLOB_KEY,
				categoryBackgroundKey);

		Key categoryKey = ds.put(category);

		String g1Image = createImage();
		String g2Image = createImage();
		Key good1Key = createGood(categoryKey, g1Image);
		Key good2Key = createGood(categoryKey, g2Image);
		
		assertNotNull(ds.get(good1Key).getProperty(Good.IMAGE_BLOB_KEY));
		assertNotNull(ds.get(good2Key).getProperty(Good.IMAGE_BLOB_KEY));
		
		dh.deleteCategory(KeyFactory.keyToString(categoryKey),
				categoryImageKey, categoryBackgroundKey);

		assertEntityDoesNotExist(categoryKey);
		assertEntityDoesNotExist(good1Key);
		assertEntityDoesNotExist(good2Key);

		assertImageDoesNotExist(categoryImageKey);
		assertImageDoesNotExist(categoryBackgroundKey);

		assertImageDoesNotExist(g1Image);
		assertImageDoesNotExist(g2Image);
	}

	@Test
	public void testAncestors() {
		Entity p = new Entity("p");
		Key pk = ds.put(p);
		
		Entity c1 = new Entity("c", pk);
		Entity c2 = new Entity("c", pk);
		
		ds.put(c1);
		ds.put(c2);
		
		Query q = new Query("c", pk);
		q.setKeysOnly();
		
		System.out.println(ds.prepare(q).asList(withLimit(100)).size());;
	}
	//======================HELPER METHODS==================================
	private String createImage() throws IOException, FileNotFoundException,
			FinalizationException, LockException {
		FileService fileService = FileServiceFactory.getFileService();
		AppEngineFile file = fileService.createNewBlobFile("text/plain");
		FileWriteChannel writeChannel = fileService
				.openWriteChannel(file, true);
		PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel,
				"UTF8"));
		out.println("Hello");
		out.close();
		writeChannel.closeFinally();

		BlobKey bk = fileService.getBlobKey(file);
		String imageKeyStr = bk.getKeyString();
		return imageKeyStr;
	}

	private void assertEntityDoesNotExist(Key k) {
		try {
			ds.get(k);
			fail();
		} catch (EntityNotFoundException e) {
			// OK
		}
	}

	private void assertImageDoesNotExist(String imageKey) {
		BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
		try {
			bs.fetchData(new BlobKey(imageKey), 0, 1);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	private Key createGood(Key categoryKey, String imageKey) {
		Entity good = new Entity(Good.KIND, categoryKey);
		good.setProperty(Good.IMAGE_BLOB_KEY, imageKey);
		
		return ds.put(good);
	}
}
