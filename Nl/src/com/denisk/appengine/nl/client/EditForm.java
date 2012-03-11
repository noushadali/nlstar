package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.persisters.ShopItemPersister;

public interface EditForm {
	ShopItemPersister getPersister();
	
	void show();
	
	void hide();
}
