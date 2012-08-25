package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem; 
import com.denisk.appengine.nl.client.util.Function;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

public abstract class BaseEditForm<T extends ShopItem> extends Composite implements EditForm<T> {
	@UiField EditItemForm itemForm;

	private MySubmitCompleteHandler formCompleteHandler = new MySubmitCompleteHandler();
	public EditItemForm getItemForm() {
		return itemForm;
	}

	@Override
	public void showForCreation() {
		itemForm.setKind(getKind());
		clearUploads();
		itemForm.show();
		itemForm.imagePanel.hideUploadPreview();
		itemForm.clearKey();
	}

	@Override
	public void hide() {
		itemForm.hide();
	}

	@Override
	public void showForEdit(T input) {
		showForCreation();
		//override empty key
		itemForm.setKey(input.getKeyStr());
		populateFields(input);
		
		initPreview(input.getImageBlobKey(), itemForm.imagePanel);
	}

	protected void initPreview(String imageBlobKey, UploadPanel imagePanel) {
		if(imageBlobKey == null || imageBlobKey.isEmpty()){
			imagePanel.hideUploadPreview();
		} else {
			imagePanel.showUploadPreview(imageBlobKey);
		}
	}

	protected void populateFields(T input){
		itemForm.name.setText(input.getName());
		itemForm.description.setText(input.getDescription());
	}
	
	protected void clearUploads(){
		itemForm.imagePanel.clearImageUpload();
	}
	
	public void setRedrawAfterItemCreatedCallback(
			final Function<Void, Void> redrawAfterGoodCreatedCallback) {
		this.formCompleteHandler.setHandler(redrawAfterGoodCreatedCallback);
	}

	private static class MySubmitCompleteHandler implements SubmitCompleteHandler {
		private Function<Void, Void> handler;
		@Override
		public void onSubmitComplete(SubmitCompleteEvent event) {
			handler.apply(null);
		}
		
		public void setHandler(Function<Void, Void> handler){
			this.handler = handler;
		}
	}
	
	protected void initFormCompleteHandler(){
		this.itemForm.imageForm.addSubmitCompleteHandler(formCompleteHandler);
	}
	
	protected abstract String getKind();
}
