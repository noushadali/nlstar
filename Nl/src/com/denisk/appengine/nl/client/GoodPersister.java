package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GoodPersister extends BaseShopItemPersister {
	private String parentCategoryItemKeyStr;
	
	public void setParentCategoryItemKeyStr(String parentCategoryItemKeyStr) {
		this.parentCategoryItemKeyStr = parentCategoryItemKeyStr;
	}

	@Override
	public void persistItem(ShopItem item, AsyncCallback<Void> callback) {
		item.setParentKeyStr(parentCategoryItemKeyStr);
		dtoService.persistGood(item.toJson(), callback);
	}

}
