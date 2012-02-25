package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Nl implements EntryPoint {
	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private Label categoriesInfo = new Label();
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Button sendButton = new Button("Generate Data");
		Button clearButton = new Button("Clear all");
		Button newButton = new Button("New category");

		
		EditCategoryForm form = new EditCategoryForm();
		final PopupPanel categoryPopup = new PopupPanel();

		categoryPopup.setWidth("500px");
		categoryPopup.setHeight("165px");
		categoryPopup.add(form);

		form.getCancel().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				categoryPopup.hide();
			}
		});
		
		final Label status = new Label();
		updateLabel(status);
		
		final RootPanel rootPanel = RootPanel.get("container");
		rootPanel.add(sendButton);
		rootPanel.add(clearButton);
		rootPanel.add(newButton);
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		
		final FlowPanel categories = new FlowPanel(); 
		rootPanel.add(categories);
		outputCategories(categories);
		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dtoService.generateTestData(new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						updateLabel(status);
						outputCategories(categories);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						status.setText("Data load failed," + caught);
					}
				});
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dtoService.clearData(new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						updateLabel(status);
						outputCategories(categories);
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		
		newButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				categoryPopup.center();
			}
		});
	}
	
	private void outputCategories(final Panel panel) {
		panel.clear();
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				JsArray<CategoryJavascriptObject> arrayFromJson = CategoryJavascriptObject.getArrayFromJson(result);
				categoriesInfo.setText("");
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < arrayFromJson.length(); i++){
					CategoryJavascriptObject c = arrayFromJson.get(i);
					
					Label name = new Label(c.getName());
					Label description = new Label(c.getDescription());
					
					LayoutPanel p = new LayoutPanel();

					p.addStyleName("category");
					
					p.add(name);
					p.add(description);
					
					p.setWidgetLeftRight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
					p.setWidgetLeftRight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
					
					p.setWidgetTopHeight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
					p.setWidgetBottomHeight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
					
					panel.add(p);
				}
//				Image image = new Image("/nl/image?id=147");
//				rootPanel.add(image);
				categoriesInfo.setText(sb.toString());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				categoriesInfo.setText("Categories Parse failed");
			}
		});
	}
	private void updateLabel(final Label status) {
		dtoService.countEntities(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				status.setText(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				status.setText("Can't calculate entities");
			}
		});
	}
}
