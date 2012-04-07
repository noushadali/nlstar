package com.denisk.appengine.nl.shared;

import java.io.Serializable;

public enum UploadStatus implements Serializable{
	NO_CHANGE, UPDATE, DELETE;
	
	public static final String FLAG_PREFIX = "st_"; 
}
