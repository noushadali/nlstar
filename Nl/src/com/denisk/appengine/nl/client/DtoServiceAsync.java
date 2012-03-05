package com.denisk.appengine.nl.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DtoServiceAsync {
	void countEntities(AsyncCallback<String> callback);

	void getCategoriesJson(AsyncCallback<String> callback);

	void clearData(AsyncCallback<Void> asyncCallback);

	void getUploadUrl(AsyncCallback<String> callback);

	void persistCategory(String categoryJson, AsyncCallback<Void> callback);

	void isAdmin(AsyncCallback<Boolean> callback);

	void getLoginUrl(AsyncCallback<String> callback);
	
	void getLogoutUrl(AsyncCallback<String> callback);
}
