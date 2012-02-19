package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
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
		final Button sendButton = new Button("Generate Data_edited");
		final Label status = new Label();
		updateLabel(status);
		
		final RootPanel rootPanel = RootPanel.get("container");
		rootPanel.add(sendButton);
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		
		outputCategories(rootPanel);
		sendButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dtoService.generateTestData(new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						updateLabel(status);
						outputCategories(rootPanel);
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						status.setText("Data load failed," + caught);
					}
				});
			}
		});
	}
	private void outputCategories(RootPanel rootPanel) {
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				JsArray<CategoryJavascriptObject> arrayFromJson = CategoryJavascriptObject.getArrayFromJson(result);
				categoriesInfo.setText("");
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < arrayFromJson.length(); i++){
					CategoryJavascriptObject c = arrayFromJson.get(i);
					sb.append("Name: ");
					sb.append(c.getName());
					sb.append(", Description:");
					sb.append(c.getDescription());
					sb.append(". ");
				}
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
