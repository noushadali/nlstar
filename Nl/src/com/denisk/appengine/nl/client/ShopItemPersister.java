package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ShopItemPersister {
	void persistItem(ShopItem item, AsyncCallback<Void> callback);
}
