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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

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
	@UiField FormPanel form;
	@UiField PopupPanel popup;
	@UiField PopupPanel loading;

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

	public PopupPanel getPopup() {
		return popup;
	}

	@UiHandler("cancel")
	void onCancelClick(ClickEvent event) {
		popup.hide();
	}
	
	@UiHandler("form")
	void onFormSubmit(SubmitEvent event) {
	}
	
	@UiHandler("save")
	void onSaveClick(ClickEvent event) {
		loading.center();
		form.submit();
	}
	
	public void setUploadUrl(String url) {
		form.setAction(url);
	}
	
	@UiHandler("form")
	void onFormSubmitComplete(SubmitCompleteEvent event) {
		loading.hide();
		popup.hide();
		System.out.println("Got response: " + event.getResults());
	}
}
