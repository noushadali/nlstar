package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Nl implements EntryPoint {
	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Generate Data_edited");
		final Label status = new Label();
		status.setVisible(false);
		
		RootPanel rootPanel = RootPanel.get("container");
		rootPanel.add(sendButton);
		rootPanel.add(new TestBinder("Denis"));
		rootPanel.add(status);
		sendButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dtoService.generateTestData(new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						status.setText("Data loaded");
						status.setVisible(true);
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						status.setText("Data load failed");
						status.setVisible(true);
					}
				});
			}
		});
	}
}
