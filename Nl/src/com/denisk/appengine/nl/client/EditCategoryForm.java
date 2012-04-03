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

public class EditCategoryForm extends BaseEditForm {
	protected CategoryPersister categoryPersister = new CategoryPersister();
	private static EditCategoryFormUiBinder uiBinder = GWT.create(EditCategoryFormUiBinder.class);
	
	interface EditCategoryFormUiBinder extends
			UiBinder<Widget, EditCategoryForm> {
	}

	@Override
	public ShopItemPersister getPersister() {
		return categoryPersister;
	}

	public EditCategoryForm() {
		initWidget(uiBinder.createAndBindUi(this));
		itemForm.setMisterPersister(categoryPersister);
	}
	
	@Override
	protected void populateFields(){
		
	}
}
