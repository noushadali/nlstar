package com.denisk.appengine.nl.data;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.blobstore.BlobKey;

public class Category {
	private String name;
	private String description;
	private BlobKey image;
	private BlobKey background;
	private Set<Good> goods = new HashSet<Good>();
}
