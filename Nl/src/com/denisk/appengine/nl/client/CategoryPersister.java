package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CategoryPersister extends BaseShopItemPersister {

	@Override
	public void persistItem(ShopItem item, AsyncCallback<Void> callback) {
		item.setParentKeyStr("");
		dtoService.persistCategory(item.toJson(), callback);
	}

}
