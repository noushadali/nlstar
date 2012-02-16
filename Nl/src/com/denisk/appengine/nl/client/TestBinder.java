package com.denisk.appengine.nl.client;

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
import com.google.gwt.user.client.ui.Label;

public class TestBinder extends Composite implements HasText {

	private static TestBinderUiBinder uiBinder = GWT
			.create(TestBinderUiBinder.class);

	interface TestBinderUiBinder extends UiBinder<Widget, TestBinder> {
	}

	public TestBinder() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Button button;
	@UiField Label label;

	public TestBinder(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		button.setText(firstName);
		label.setText("Denisss");
	}

	@UiHandler("button")
	void onClick(ClickEvent e) {
		Window.alert("Hello!");
	}

	public void setText(String text) {
		button.setText(text);
	}

	public String getText() {
		return button.getText();
	}

}
