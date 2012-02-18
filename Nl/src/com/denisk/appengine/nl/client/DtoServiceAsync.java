package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DtoServiceAsync {
	void generateTestData(AsyncCallback<Void> callback);

	void countEntities(AsyncCallback<String> callback);

	void getCategories(AsyncCallback<JsArrayString> callback);
}
