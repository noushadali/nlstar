package com.denisk.appengine.nl.client.ui.views;

import com.google.gwt.event.dom.client.ClickHandler;

public abstract class AbstractItemsView {
	
	public abstract ClickHandler getNewItemHandler();
	public abstract ClickHandler getClearAllHandler();
}
