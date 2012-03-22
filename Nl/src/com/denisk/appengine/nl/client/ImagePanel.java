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
	private static final String THUMB_HEIGHT = "40";
	private static final String THUMB_WIDTH = "50";

	private static ImagePanelUiBinder uiBinder = GWT
			.create(ImagePanelUiBinder.class);
	@UiField FileUpload image;
	@UiField Image imageThumbnail;
	@UiField Label imageDelete;
	@UiField Label label;

	interface ImagePanelUiBinder extends UiBinder<Widget, ImagePanel> {
	}

	public ImagePanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void setLabel(String labelText){
		label.setText(labelText);
	}
	@UiHandler("imageDelete")
	void onImageDeleteClick(ClickEvent event) {
		hideImagePreview();
	}
	
	public void showImagePreview(String imageBlobKey) {
		imageThumbnail.setUrl("/nl/thumb?key=" + imageBlobKey + "&w=" + THUMB_WIDTH + "&h=" + THUMB_HEIGHT);
		imageThumbnail.setVisible(true);
		imageDelete.setVisible(true);
	}

	public void hideImagePreview() {
		image.setVisible(true);
		imageThumbnail.setVisible(false);
		imageDelete.setVisible(false);
	}

}
