package com.denisk.appengine.nl.client.service;

import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dtoService")
public interface DtoService extends RemoteService {
	String countEntities();
	
	String getCategoriesJson();
	
	String getGoodsJson(String categoryKeyStr);

	void clearData(); 
	
	void clearGoodsForCategory(String categoryKeyStr);
	
	String getImageUploadUrl();
	
	String persistCategory(String categoryJson);
	
	UserStatus isAdmin();
	
	String getLoginUrl();

	String getLogoutUrl();

	String persistGood(String goodJson);
	
	void updateCategoryBackground(String categoryKeyStr, String backgoundImageKeyStr);
	
	void deleteCategory(String key, String imageKey, String backgroundImageKey);
	
	void deleteGood(String key, String imageKey);

	String getSingleCategoryJson(String categoryKey);
}
