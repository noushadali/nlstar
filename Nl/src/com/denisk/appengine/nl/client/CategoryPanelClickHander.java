package com.denisk.appengine.nl.client;

import com.google.gwt.event.dom.client.ClickHandler;

public interface CategoryPanelClickHander extends ClickHandler {
	void setKeyStr(String keyStr);
	String getKeyStr();
}
