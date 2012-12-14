/**
 * 
 */
package com.denisk.appengine.nl.client.ui.parts;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author denisk
 *
 */
public class EditItemForm extends Composite implements HasWidgets {
	private static EditFormFormUiBinder uiBinder = GWT.create(EditFormFormUiBinder.class);
	
	@UiField TextBox kind;
	@UiField TextBox key;
	@UiField TextBox parentKey;
	@UiField TextBox name;
	@UiField TextBox description;
	@UiField Button save;
	@UiField Button cancel;
	@UiField PopupPanel loading;
	@UiField FormPanel imageForm;
	@UiField VerticalPanel customInputs;
	@UiField PopupPanel popup;
	@UiField UploadPanel imagePanel;
	

	
	private HashMap<String, Object> additionalProperties = new HashMap<String, Object>();
	
	interface EditFormFormUiBinder extends UiBinder<Widget, EditItemForm> {
	}

	public EditItemForm() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public HashMap<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@UiHandler("cancel")
	void onCancelClick(ClickEvent event) {
		popup.hide();
	}
	
	@UiHandler("save")
	void onSaveClick(ClickEvent event) {
		imageForm.submit();
	}
	
	public void show(){
		popup.center();
	}

	public void hide() {
		popup.hide();
	}

	@Override
	public void add(Widget w) {
		customInputs.add(w);
	}

	@Override
	public void clear() {
		customInputs.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return customInputs.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return customInputs.remove(w);
	}
	
	public void setKey(String keyStr){
		key.setText(keyStr);
	}
	
	public void clearKey(){
		key.setText("");
	}
	
	public void setParentKey(String parentKey){
		this.parentKey.setText(parentKey);
	}
	
	public void setKind(String kind){
		this.kind.setText(kind);
	}
	
	public void clearNameDescription(){
		this.name.setText("");
		this.description.setText("");
	}
}
