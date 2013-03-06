package com.denisk.appengine.nl.client.ui.parts;

import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
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
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ProductsList extends Composite {

	private static ProductsListUiBinder uiBinder = GWT
			.create(ProductsListUiBinder.class);
	@UiField HTMLPanel root;
	@UiField(provided=true) CellList<GoodJavascriptObject> cellList = new CellList<GoodJavascriptObject>(new AbstractCell<GoodJavascriptObject>(){
		@Override
		public void render(Context context, GoodJavascriptObject value, SafeHtmlBuilder sb) {
			// TODO
		}
	});

	interface ProductsListUiBinder extends UiBinder<Widget, ProductsList> {
	}

	public ProductsList() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public ProductsList(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
