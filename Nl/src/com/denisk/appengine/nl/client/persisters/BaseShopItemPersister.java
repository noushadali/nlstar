package com.denisk.appengine.nl.client.persisters;

import com.denisk.appengine.nl.client.DtoService;
import com.denisk.appengine.nl.client.DtoServiceAsync;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class BaseShopItemPersister implements ShopItemPersister {
	protected static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	protected ClickHandler redrawAfterItemCreatedCallback;
	
	@Override
	public void setRedrawAfterItemCreatedCallback(ClickHandler callback){
		redrawAfterItemCreatedCallback = callback;
	}

	@Override
	public void afterEntitySaved(String keyStr) {
		redrawAfterItemCreatedCallback.onClick(null);
	}
	
	
}
