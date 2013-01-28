package com.denisk.appengine.nl.client.ui.views;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.parts.EditForm;
import com.denisk.appengine.nl.client.util.Function;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;

public abstract class AbstractItemsView {
	public static final String THUMB_WIDTH = "200";
	public static final String THUMB_HEIGHT = "100";

	
	protected Nl parent;

	public AbstractItemsView(Nl parent){
		this.parent = parent;
	}
	protected LayoutPanel createShopItemPanel(final ShopItem itemJson) {
		final Label name = new Label(itemJson.getName());
		Label description = new Label(itemJson.getDescription());
		Image image = new Image(
				getImageUrl(itemJson.getImageBlobKey(), THUMB_WIDTH, THUMB_HEIGHT));

		LayoutPanel itemPanel = new LayoutPanel();

		itemPanel.addStyleName("category");

		itemPanel.add(name);
		itemPanel.add(image);
		itemPanel.add(description);

		itemPanel.setWidgetLeftRight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		itemPanel.setWidgetTopHeight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);

		itemPanel.setWidgetLeftRight(description, 5, Style.Unit.PX, 20,
				Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(description, 5, Style.Unit.PX, 20,
				Style.Unit.PX);

		itemPanel
				.setWidgetLeftRight(image, 0, Style.Unit.PX, 10, Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(image, 10, Style.Unit.PX, 150,
				Style.Unit.PX);
		itemPanel.setWidgetHorizontalPosition(image,
				com.google.gwt.layout.client.Layout.Alignment.END);
		return itemPanel;
	}

	protected<T extends ShopItem> void buildEditButton(final T item,
			LayoutPanel panel, final EditForm<T> editForm) {
		HTML edit = new HTML("<a href=#>Edit</a>");
		edit.addClickHandler(getEditClickHandler(item, editForm));
		panel.add(edit);

		panel.setWidgetRightWidth(edit, 60, Style.Unit.PX, 30, Style.Unit.PX);
		panel.setWidgetTopHeight(edit, 10, Style.Unit.PX, 20, Style.Unit.PX);
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

	/**
	 * Deletes an item and redraws the panel
	 */
	protected <T extends ShopItem> void buildDeleteButton(final T item,
			LayoutPanel panel, final Function<T, Void> deletion) {
		HTML delete = new HTML("<a href='javascript://'>Delete</a>");

		delete.addClickHandler(getDeleteClickHandler(item, deletion));

		panel.add(delete);

		panel.setWidgetRightWidth(delete, 15, Style.Unit.PX, 40, Style.Unit.PX);
		panel.setWidgetTopHeight(delete, 10, Style.Unit.PX, 20, Style.Unit.PX);

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

	public abstract void render(Panel panel, Function callback);
	public abstract ClickHandler getNewItemHandler();
	public abstract ClickHandler getClearAllHandler();
}
