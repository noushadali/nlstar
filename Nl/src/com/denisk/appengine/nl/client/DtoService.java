package com.denisk.appengine.nl.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dtoService")
public interface DtoService extends RemoteService {
	String countEntities();
	
	String getCategoriesJson();

	void clearData(); 
	
	String getUploadUrl();
	
}
