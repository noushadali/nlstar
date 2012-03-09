/**
 * 
 */
package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author denisk
 *
 */
public class EditCategoryForm extends Composite {
	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private static EditCategoryFormUiBinder uiBinder = GWT.create(EditCategoryFormUiBinder.class);
	@UiField TextBox name;
	@UiField TextBox description;
	@UiField FileUpload image;
	@UiField Button save;
	@UiField Button cancel;
	@UiField PopupPanel popup;
	@UiField PopupPanel loading;
	@UiField FormPanel imageForm;

	private ClickHandler redrawPageAfterItemPersistedCallback;
	private AsyncCallback<Void> afterItemPersistedCallback = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			loading.hide();
			popup.hide();
			System.out.println("Updated entity");
			redrawPageAfterItemPersistedCallback.onClick(null);
		}
		
		@Override
		public void onFailure(Throwable caught) {
		}
	};
	private ShopItemPersister misterPersister;
	
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
	
	@UiHandler("save")
	void onSaveClick(ClickEvent event) {
		dtoService.getUploadUrl(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String url) {
				imageForm.setAction(url);
				loading.center();
				imageForm.submit();
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	@UiHandler("imageForm")
	void onFormSubmitComplete(SubmitCompleteEvent event) {
		String imageId = event.getResults();
		if (imageId.startsWith("<pre>")) {
			//cut <pre>...</pre>
			imageId = imageId.substring(5, imageId.length() - 6);
		}
		final ShopItem item = ShopItem.createObject().cast();
		item.setImageBlobKey(imageId);
		item.setName(name.getValue());
		item.setDescription(description.getValue());
		
		
		misterPersister.persistItem(item, afterItemPersistedCallback);
	}

	public void setRedrawPageAfterItemPersistedCallback(ClickHandler submitCallback) {
		this.redrawPageAfterItemPersistedCallback = submitCallback;
	}

	public void show() {
		popup.center();
	}

	public void setMisterPersister(ShopItemPersister misterPersister) {
		this.misterPersister = misterPersister;
	}
}
