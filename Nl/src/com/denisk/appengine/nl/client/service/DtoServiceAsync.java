package com.denisk.appengine.nl.client.service;

import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DtoServiceAsync {
	void countEntities(AsyncCallback<String> callback);

	void getCategoriesJson(AsyncCallback<String> callback);

	void getAllCategoriesExcept(String categoryKeyStr,
			AsyncCallback<String> callback);

	void clearData(AsyncCallback<Void> asyncCallback);

	void getImageUploadUrl(AsyncCallback<String> callback);

	void persistCategory(String categoryJson, AsyncCallback<String> callback);

	void persistGood(String goodJson, AsyncCallback<String> callback);

	void isAdmin(AsyncCallback<UserStatus> callback);

	void getLoginUrl(AsyncCallback<String> callback);
	
	void getLogoutUrl(AsyncCallback<String> callback);

	void getGoodsJson(String categoryKeyStr, AsyncCallback<String> callback);

	void getAllGoodsJson(AsyncCallback<String> callback);

	void clearGoodsForCategory(String categoryKeyStr,
			AsyncCallback<Void> callback);

	void updateCategoryBackground(String categoryKeyStr,
			String backgoundImageKeyStr, AsyncCallback<Void> callback);

	void deleteCategory(String key, String imageKey, String backgroundImageKey,
			AsyncCallback<Void> callback);

	void deleteGood(String key, String imageKey, AsyncCallback<Void> callback);

	void getCategoryBackgroundKey(String categoryKey, AsyncCallback<String> callback);
}
