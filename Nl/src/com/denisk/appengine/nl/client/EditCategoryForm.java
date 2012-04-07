package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.server.data.Category;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class EditCategoryForm extends BaseEditForm<CategoryJavascriptObject> {
	private static EditCategoryFormUiBinder uiBinder = GWT.create(EditCategoryFormUiBinder.class);
	
	@UiField UploadPanel backgroundImagePanel;

	interface EditCategoryFormUiBinder extends
			UiBinder<Widget, EditCategoryForm> {
	}

	public EditCategoryForm() {
		initWidget(uiBinder.createAndBindUi(this));
		initFormCompleteHandler();
	}
	
	@Override
	protected void populateFields(CategoryJavascriptObject input){
		super.populateFields(input);
	}

	@Override
	protected void clearUploads() {
		super.clearUploads();
		backgroundImagePanel.clearImageUpload();
	}

	@Override
	public void showForCreation() {
		super.showForCreation();
		backgroundImagePanel.hideUploadPreview();
	}

	@Override
	public void showForEdit(CategoryJavascriptObject input) {
		super.showForEdit(input);
		initPreview(input.getBackgroundBlobKey(), backgroundImagePanel);
	}

	@Override
	protected String getKind() {
		return Category.KIND;
	}
	
	
}
