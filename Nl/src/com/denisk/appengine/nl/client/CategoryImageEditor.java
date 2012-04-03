package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CategoryImageEditor extends Composite {

	private static CategoryImageEditorUiBinder uiBinder = GWT
			.create(CategoryImageEditorUiBinder.class);

	interface CategoryImageEditorUiBinder extends
			UiBinder<Widget, CategoryImageEditor> {
	}

	public CategoryImageEditor() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
