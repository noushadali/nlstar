package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class ImagePanel extends Composite {

	private static ImagePanelUiBinder uiBinder = GWT
			.create(ImagePanelUiBinder.class);
	@UiField FileUpload image;
	@UiField Image imageThumbnail;
	@UiField Label imageDelete;

	interface ImagePanelUiBinder extends UiBinder<Widget, ImagePanel> {
	}

	public ImagePanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("imageDelete")
	void onImageDeleteClick(ClickEvent event) {
		imageThumbnail.setVisible(false);
		image.setVisible(true);
		imageDelete.setVisible(false);
	}
}
