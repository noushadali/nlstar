package com.denisk.appengine.nl.client.ui.parts;

import java.util.List;

import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.views.AbstractItemsView;
import com.denisk.appengine.nl.client.util.Function;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ProductsList extends Composite {

	private static ProductsListUiBinder uiBinder = GWT
			.create(ProductsListUiBinder.class);
	@UiField HTMLPanel root;
	@UiField(provided=true) CellList<ShopItem> cellList = new CellList<ShopItem>(new AbstractCell<ShopItem>(){
		@Override
		public void render(Context context, ShopItem value, SafeHtmlBuilder sb) {
			sb.appendHtmlConstant("<div class='listItem'>");
			sb.appendHtmlConstant("<img src=\"" + AbstractItemsView.getImageUrl(value.getImageBlobKey(), 50, 50) + "\"/>");
			sb.appendHtmlConstant("<div>" + value.getName() + "</div>");
			sb.appendHtmlConstant("</div>");
		}
	});

	private Function<ShopItem, Void> clickCallback;
	
	interface ProductsListUiBinder extends UiBinder<Widget, ProductsList> {
	}

	public ProductsList() {
		initWidget(uiBinder.createAndBindUi(this));
		final SingleSelectionModel<ShopItem> selectionModel = new SingleSelectionModel<ShopItem>();
		
		selectionModel.addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if(clickCallback != null){
					clickCallback.apply(selectionModel.getSelectedObject());
				}
			}
		});
		
		cellList.setSelectionModel(selectionModel);
	}
	
	public ProductsList(Function<ShopItem, Void> clickCallback){
		this();
		this.clickCallback = clickCallback;
	}
	
	public void setItems(List<? extends ShopItem> items){
		cellList.setRowData(items);
	}
}
