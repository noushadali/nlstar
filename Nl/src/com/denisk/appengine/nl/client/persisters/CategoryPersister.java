package com.denisk.appengine.nl.client.persisters;

import java.util.Map;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.util.Util;
import com.denisk.appengine.nl.server.data.Category;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

public class CategoryPersister extends BaseShopItemPersister {

	private FormPanel backgroundImageFormPanel;
	
	@Override
	public void persistItem(ShopItem item, AsyncCallback<String> callback) {
		item.setParentKeyStr("");
		dtoService.persistCategory(item.toJson(), callback);
	}

	@Override
	public CategoryJavascriptObject createEntity() {
		return CategoryJavascriptObject.createObject().cast();
	}

	@Override
	public void setAdditionalProperties(ShopItem item, Map<String, Object> additionalProperties) {
		CategoryJavascriptObject category = item.cast();
		String backgroundImageKeyStr = (String) additionalProperties.get(Category.BACKGROUND_BLOB_KEY);
		category.setBackgroundBlobKey(backgroundImageKeyStr == null ? "" : backgroundImageKeyStr);
	}

	@Override
	public void afterEntitySaved(final String categoryKeyStr) {
		dtoService.getImageUploadUrl(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String url) {
				backgroundImageFormPanel.setAction(url);
				backgroundImageFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						String backgroundImageKeyStr = Util.cutPre(event.getResults());
						dtoService.updateCategoryBackground(categoryKeyStr, backgroundImageKeyStr, new AsyncCallback<Void>() {
							
							@Override
							public void onSuccess(Void result) {
								CategoryPersister.super.afterEntitySaved(categoryKeyStr);
							}
							
							@Override
							public void onFailure(Throwable caught) {
							}
						});
					}
				});

				backgroundImageFormPanel.submit();
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		
	}

	public void setBackgroundImageFormPanel(FormPanel backgroundImageFormPanel) {
		this.backgroundImageFormPanel = backgroundImageFormPanel;
	}
}
