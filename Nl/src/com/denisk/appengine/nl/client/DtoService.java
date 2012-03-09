package com.denisk.appengine.nl.client;

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
	
	String getUploadUrl();
	
	void persistCategory(String categoryJson);
	
	UserStatus isAdmin();
	
	String getLoginUrl();

	String getLogoutUrl();

	void persistGood(String goodJson);
}
