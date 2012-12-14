package com.denisk.appengine.nl.client.ui.parts;

import com.denisk.appengine.nl.client.overlay.ShopItem;

public interface EditForm<T extends ShopItem> {
	void showForCreation();
	
	void showForEdit(T input);

	void hide();
}
