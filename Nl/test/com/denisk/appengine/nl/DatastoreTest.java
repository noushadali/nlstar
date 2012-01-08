package com.denisk.appengine.nl;

import static org.junit.Assert.assertEquals;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatastoreTest {
	 private final LocalServiceTestHelper helper =
		        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void before() {
		helper.setUp();
	}
	
	@After
	public void after() {
		helper.tearDown();
	}
	
	@Test
	public void memcacheTest() {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		final String kind = "yum";
		assertEquals(0, ds.prepare(new Query(kind)).countEntities(withLimit(10)));
		ds.put(new Entity(kind));
		ds.put(new Entity(kind));
		assertEquals(2, ds.prepare(new Query(kind)).countEntities(withLimit(10)));
	}

}
