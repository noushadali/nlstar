package com.denisk.appengine.nl.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DtoServiceAsync {
	void countEntities(AsyncCallback<String> callback);

	void getCategoriesJson(AsyncCallback<String> callback);

	void clearData(AsyncCallback<Void> asyncCallback);

	void getUploadUrl(AsyncCallback<String> callback);
}
