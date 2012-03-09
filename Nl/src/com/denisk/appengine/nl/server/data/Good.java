package com.denisk.appengine.nl.server.data;

public class Good extends Jsonable<Good> {
	public static final String KIND = "g";

	@Override
	protected Good instance() {
		return new Good();
	}
	
	
}
