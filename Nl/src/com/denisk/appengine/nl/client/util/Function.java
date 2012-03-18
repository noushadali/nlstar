package com.denisk.appengine.nl.client.util;

public interface Function<I, O> {
	public O apply(I input);
}
