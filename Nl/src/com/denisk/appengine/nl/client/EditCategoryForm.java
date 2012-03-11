package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.persisters.CategoryPersister;
import com.denisk.appengine.nl.client.persisters.ShopItemPersister;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

public class EditCategoryForm extends Composite implements EditForm {

	private static EditCategoryFormUiBinder uiBinder = GWT.create(EditCategoryFormUiBinder.class);
	
	@UiField FileUpload backgroundImage;
	@UiField EditItemForm itemForm;
	@UiField FormPanel backgroundImageFormPanel;

	private CategoryPersister categoryPersister = new CategoryPersister();

	interface EditCategoryFormUiBinder extends
			UiBinder<Widget, EditCategoryForm> {
	}

	public EditCategoryForm() {
		initWidget(uiBinder.createAndBindUi(this));
		itemForm.setMisterPersister(categoryPersister);
		categoryPersister.setBackgroundImageFormPanel(backgroundImageFormPanel);
	}

	public EditItemForm getItemForm() {
		return itemForm;
	}

	@Override
	public ShopItemPersister getPersister() {
		return categoryPersister;
	}

	@Override
	public void show() {
		itemForm.show();
	}

	@Override
	public void hide() {
		itemForm.hide();
	}
}
