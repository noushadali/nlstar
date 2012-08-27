package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
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
	
	void deleteItem(ShopItem item);
}
