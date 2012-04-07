package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.server.data.Good;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class EditGoodForm extends BaseEditForm<GoodJavascriptObject> {

	private static EditGoodFormUiBinder uiBinder = GWT.create(EditGoodFormUiBinder.class);
	
	interface EditGoodFormUiBinder extends UiBinder<Widget, EditGoodForm> {}
	

	public EditGoodForm() {
		initWidget(uiBinder.createAndBindUi(this));
		initFormCompleteHandler();
	}
	
	@Override
	protected void populateFields(GoodJavascriptObject input) {
		super.populateFields(input);
	}

	public void setParentCategoryItemKeyStr(String selectedCategoryKeyStr) {
		itemForm.setParentKey(selectedCategoryKeyStr);
	}

	@Override
	protected String getKind() {
		return Good.KIND;
	}

}
