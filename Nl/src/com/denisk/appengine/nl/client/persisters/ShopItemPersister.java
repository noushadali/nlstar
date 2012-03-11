package com.denisk.appengine.nl.client.persisters;

import java.util.Map;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public interface ShopItemPersister {
	ShopItem createEntity();
	void setAdditionalProperties(ShopItem item, Map<String, Object> additionalProperties);
	void persistItem(ShopItem item, AsyncCallback<String> callback);
	void afterEntitySaved(String keyStr);
	public abstract void setRedrawAfterItemCreatedCallback(ClickHandler callback);
}
