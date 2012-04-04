package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;

public abstract class BaseEditForm extends Composite implements EditForm {
	@UiField EditItemForm itemForm;

	public EditItemForm getItemForm() {
		return itemForm;
	}

	@Override
	public void show() {
		itemForm.show();
		itemForm.imagePanel.hideImagePreview();
	}

	@Override
	public void hide() {
		itemForm.hide();
	}

	public void showForEdit(CategoryJavascriptObject input) {
		itemForm.name.setText(input.getName());
		itemForm.description.setText(input.getDescription());
		
		populateFields();
		show();
		
		itemForm.imagePanel.image.setVisible(false);
		String imageBlobKey = input.getImageBlobKey();
		if(imageBlobKey == null || imageBlobKey.isEmpty()){
			itemForm.imagePanel.hideImagePreview();
		} else {
			itemForm.imagePanel.showImagePreview(imageBlobKey);
		}
	}

	protected abstract void populateFields();
}
