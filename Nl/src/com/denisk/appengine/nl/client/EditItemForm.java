/**
 * 
 */
package com.denisk.appengine.nl.client;

import java.util.HashMap;
import java.util.Iterator;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.persisters.ShopItemPersister;
import com.denisk.appengine.nl.client.util.Util;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author denisk
 *
 */
public class EditItemForm extends Composite implements HasWidgets {
	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private static EditFormFormUiBinder uiBinder = GWT.create(EditFormFormUiBinder.class);
	
	@UiField TextBox name;
	@UiField TextBox description;
	@UiField Button save;
	@UiField Button cancel;
	@UiField PopupPanel loading;
	@UiField FormPanel imageForm;
	@UiField VerticalPanel customInputs;
	@UiField PopupPanel popup;
	@UiField ImagePanel imagePanel;
	

	private ShopItemPersister misterPersister;
	
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
	
//	@UiHandler("imageForm")
//	void onFormSubmitComplete(SubmitCompleteEvent event) {
//		String imageId = Util.cutPre(event.getResults());
//
//		ShopItem shopItem = misterPersister.createEntity();
//
//		shopItem.setImageBlobKey(imageId);
//		shopItem.setName(name.getValue());
//		shopItem.setDescription(description.getValue());
//
//		misterPersister.setAdditionalProperties(shopItem, additionalProperties);
//		
//		misterPersister.persistItem(shopItem, new AsyncCallback<String>() {
//			@Override
//			public void onSuccess(String result) {
//				loading.hide();
//				System.out.println("Updated entity");
//				misterPersister.afterEntitySaved(result);
//			}
//			
//			@Override
//			public void onFailure(Throwable caught) {
//			}
//		});
//	}
	
	public void show(){
		popup.center();
	}

	public void setMisterPersister(ShopItemPersister misterPersister) {
		this.misterPersister = misterPersister;
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
}
