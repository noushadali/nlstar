package com.denisk.appengine.nl.client.ui.views;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.parts.EditForm;
import com.denisk.appengine.nl.client.util.Function;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class AbstractItemsView {
	public static final String THUMB_WIDTH = "480";
	public static final String THUMB_HEIGHT = "400";
	
	protected Nl parent;

	public AbstractItemsView(Nl parent){
		this.parent = parent;
	}

	protected<T extends ShopItem> void buildEditButton(final T item,
			Panel panel, final EditForm<T> editForm) {
		Label edit = new Label("Edit");
		edit.addStyleName("editControl");
		edit.addClickHandler(getEditClickHandler(item, editForm));
		
		panel.add(edit);
	}

	/**
	 * Deletes an item and redraws the panel
	 */
	protected <T extends ShopItem> void buildDeleteButton(final T item,
			Panel panel, final Function<T, Void> deletion) {
		Label delete = new Label("Delete");
		delete.addStyleName("editControl");

		delete.addClickHandler(getDeleteClickHandler(item, deletion));

		panel.add(delete);
	}

	protected AsyncCallback<Void> getRedrawingCallback(
			final Function<Void, Void> redrawing) {
		return new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error processing deletion of the item, exception is "
						+ caught);
			}

			@Override
			public void onSuccess(Void result) {
				redrawing.apply(null);
			}
		};
	}

	protected <T extends ShopItem> ClickHandler getDeleteClickHandler(
			final T item, final Function<T, Void> deletion) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (Window.confirm("Are you sure you want to delete "
						+ item.getName() + "?")) {
					deletion.apply(item);
				}
			}
		};
	}

	protected <T extends ShopItem> ClickHandler getEditClickHandler(final T item,
			final EditForm<T> editForm) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				editForm.showForEdit(item);
			}
		};
	}

	public static String getImageUrl(final String imageBlobKey, String width,
			String height) {
		return "/nl/thumb?key=" + imageBlobKey + "&w=" + width
				+ "&h=" + height;
	}

	protected void clearBackgrounds(){
		RootPanel.get("backgroundsContainer").clear();
	}
	
	public abstract void render(Panel panel, Function callback);
	public abstract ClickHandler getNewItemHandler();
	public abstract ClickHandler getClearAllHandler();
}
