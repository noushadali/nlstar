package com.denisk.appengine.nl.server;

import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;

import com.denisk.appengine.nl.client.DtoService;
import com.denisk.appengine.nl.server.data.Category;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DtoServiceServlet extends RemoteServiceServlet implements DtoService {
	private static final long serialVersionUID = 3021789825605473063L;
	private static DataHandler dh = new DataHandler();
	private UserService us = UserServiceFactory.getUserService(); 

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
		checkCredentials();

		dh.clearAll();
		
		MemcacheServiceFactory.getMemcacheService().clearAll();
		
		Iterator<BlobInfo> infos = new BlobInfoFactory().queryBlobInfos();
		HashSet<BlobKey> toDelete = new HashSet<BlobKey>();
		while(infos.hasNext()){
			toDelete.add(infos.next().getBlobKey());
		}
		BlobstoreServiceFactory.getBlobstoreService().delete(toDelete.toArray(new BlobKey[]{}));
	}


	@Override
	public String getImageUploadUrl() {
		checkCredentials();
		return BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/upload");
	}

	@Override
	public String persistCategory(String categoryJson) {
		checkCredentials();
		Category category = new Category().getFromJson(categoryJson);
		return KeyFactory.keyToString(dh.saveCategoryWithGoods(category));
	}


	private void checkCredentials() throws IllegalAccessError {
		if(us.getCurrentUser() == null || ! us.isUserAdmin()){
			throw new IllegalAccessError("User is not allowed to perform this operation: " + us.getCurrentUser());
		}
	}


	@Override
	public UserStatus isAdmin() {
		User currentUser = us.getCurrentUser();
		if(currentUser==null){
			return UserStatus.NOT_LOGGED_IN;
		}
		return us.isUserAdmin() ? UserStatus.ADMIN : UserStatus.NOT_ADMIN;
	}


	@Override
	public String getLoginUrl() {
		return us.createLoginURL("/");
	}


	@Override
	public String getLogoutUrl() {
		return us.createLogoutURL("/");
	}


	@Override
	public String getGoodsJson(String categoryKeyStr) {
		return dh.getGoodsJson(categoryKeyStr);
	}


	@Override
	public void clearGoodsForCategory(String categoryKeyStr) {
		checkCredentials();
		dh.clearGoodsForCategory(categoryKeyStr);
	}


	@Override
	public String persistGood(String goodJson) {
		checkCredentials();
		return KeyFactory.keyToString(dh.persistGood(goodJson));
	}


	@Override
	public void updateCategoryBackground(String categoryKeyStr,	String backgoundImageKeyStr) {
		checkCredentials();
		dh.updateCategoryBackground(categoryKeyStr, backgoundImageKeyStr);
	}


	@Override
	public void deleteCategory(String key, String imageKey, String backgroundImageKey) {
		checkCredentials();
		dh.deleteCategory(key, imageKey, backgroundImageKey);
	}
	
	@Override
	public void deleteGood(String key, String imageKey){
		checkCredentials();
		dh.deleteGood(key, imageKey);
	}
}
