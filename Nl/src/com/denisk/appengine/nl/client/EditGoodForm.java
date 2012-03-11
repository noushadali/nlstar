package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.persisters.GoodPersister;
import com.denisk.appengine.nl.client.persisters.ShopItemPersister;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class EditGoodForm extends Composite implements EditForm {

	private GoodPersister goodPersister = new GoodPersister();
	private static EditGoodFormUiBinder uiBinder = GWT.create(EditGoodFormUiBinder.class);
	@UiField EditItemForm itemForm;
	
	interface EditGoodFormUiBinder extends UiBinder<Widget, EditGoodForm> {}
	

	public EditGoodForm() {
		initWidget(uiBinder.createAndBindUi(this));
		itemForm.setMisterPersister(goodPersister);
	}

	
	public EditItemForm getItemForm() {
		return itemForm;
	}


	public void setParentCategoryItemKeyStr(String selectedCategoryKeyStr) {
		goodPersister.setParentCategoryItemKeyStr(selectedCategoryKeyStr);
	}


	@Override
	public ShopItemPersister getPersister() {
		return goodPersister;
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
