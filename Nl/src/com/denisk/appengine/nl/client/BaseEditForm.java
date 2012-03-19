package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;

public abstract class BaseEditForm extends Composite implements EditForm {
	private static final String THUMB_HEIGHT = "40";
	private static final String THUMB_WIDTH = "50";
	@UiField EditItemForm itemForm;

	public EditItemForm getItemForm() {
		return itemForm;
	}

	@Override
	public void show() {
		itemForm.show();
		itemForm.imagePanel.image.setVisible(true);
		itemForm.imagePanel.imageThumbnail.setVisible(false);
		itemForm.imagePanel.imageDelete.setVisible(false);
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
		itemForm.imagePanel.imageThumbnail.setUrl("/nl/thumb?key=" + input.getImageBlobKey() + "&w=" + THUMB_WIDTH + "&h=" + THUMB_HEIGHT);
		itemForm.imagePanel.imageThumbnail.setVisible(true);
		itemForm.imagePanel.imageDelete.setVisible(true);
	}

	protected abstract void populateFields();
}
