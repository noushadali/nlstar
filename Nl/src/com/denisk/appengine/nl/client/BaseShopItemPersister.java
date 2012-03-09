package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.GWT;

public abstract class BaseShopItemPersister implements ShopItemPersister {
	protected static DtoServiceAsync dtoService = GWT.create(DtoService.class);
}
