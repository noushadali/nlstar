package com.denisk.appengine.nl.client.persisters;

import java.util.Map;

import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GoodPersister extends BaseShopItemPersister {
	private String parentCategoryItemKeyStr;
	
	public void setParentCategoryItemKeyStr(String parentCategoryItemKeyStr) {
		this.parentCategoryItemKeyStr = parentCategoryItemKeyStr;
	}

	@Override
	public void persistItem(ShopItem item, AsyncCallback<String> callback) {
		item.setParentKeyStr(parentCategoryItemKeyStr);
		dtoService.persistGood(item.toJson(), callback);
	}

	@Override
	public GoodJavascriptObject createEntity() {
		return GoodJavascriptObject.createObject().cast();
	}

	@Override
	public void setAdditionalProperties(ShopItem item, Map<String, Object> additionalProperties) {}

	@Override
	public void afterEntitySaved(String keyStr) {
		super.afterEntitySaved(keyStr);
	}

}
