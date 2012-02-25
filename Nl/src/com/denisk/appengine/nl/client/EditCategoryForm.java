/**
 * 
 */
package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FileUpload;

/**
 * @author denisk
 *
 */
public class EditCategoryForm extends Composite {

	private static EditCategoryFormUiBinder uiBinder = GWT
			.create(EditCategoryFormUiBinder.class);
	@UiField TextBox name;
	@UiField TextBox description;
	@UiField FileUpload image;
	@UiField Button save;
	@UiField Button cancel;

	interface EditCategoryFormUiBinder extends
			UiBinder<Widget, EditCategoryForm> {
	}

	/**
	 * Because this class has a default constructor, it can
	 * be used as a binder template. In other words, it can be used in other
	 * *.ui.xml files as follows:
	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 *   xmlns:g="urn:import:**user's package**">
	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
	 * </ui:UiBinder>
	 * Note that depending on the widget that is used, it may be necessary to
	 * implement HasHTML instead of HasText.
	 */
	public EditCategoryForm() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public EditCategoryForm(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));

		// Can access @UiField after calling createAndBindUi
	}
	@UiHandler("save")
	void onSaveClick(ClickEvent event) {
		System.out.println("Saving: " + name.getText() + ", " + description.getText() + ", " + image.getFilename());
	}
	@UiHandler("cancel")
	void onCancelClick(ClickEvent event) {
		System.out.println("Cancelling");
	}

	public Button getSave() {
		return save;
	}

	public Button getCancel() {
		return cancel;
	}
	
}
