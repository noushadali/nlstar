package com.denisk.appengine.nl.server;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;

import com.denisk.appengine.nl.client.DtoService;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DtoServiceServlet extends RemoteServiceServlet implements
		DtoService {
	private static final long serialVersionUID = 3021789825605473063L;
	private static DataHandler dh = new DataHandler();

	@Override
	public String countEntities() {
		return "Catogories: " + dh.countCategories() + ", goods: " + dh.countGoods();
	}


	@Override
	public String getCategoriesJson() {
		try {
			return dh.getCategoriesJson();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void clearData() {
		dh.clearAll();
		MemcacheServiceFactory.getMemcacheService().clearAll();
	}


	@Override
	public String getUploadUrl() {
		return BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/upload");
	}


	
}
