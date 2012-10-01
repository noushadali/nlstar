package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.shared.UploadStatus;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UploadPanel extends Composite {
	private static final String THUMB_HEIGHT = "40" ;
	private static final String THUMB_WIDTH = "50";

	private static ImagePanelUiBinder uiBinder = GWT
			.create(ImagePanelUiBinder.class);
	@UiField FileUpload image;
	@UiField Image imageThumbnail;
	@UiField Label imageDeleteButton;
	@UiField Label label;
	@UiField TextBox flag;
	@UiField FlowPanel uploadPanel;

	private final ChangeHandler uploadChangeHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent event) {
			flag.setText(UploadStatus.UPDATE.toString());
		}
	};
	
	interface ImagePanelUiBinder extends UiBinder<Widget, UploadPanel> {
	}

	public UploadPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		image.addChangeHandler(uploadChangeHandler);
	}
	
	public void setLabel(String labelText){
		label.setText(labelText);
	}
	
	public void setName(String name){
		image.setName(name);
		flag.setName(UploadStatus.FLAG_PREFIX + name);
	}
	
	@UiHandler("imageDeleteButton")
	void onImageDeleteButtonClick(ClickEvent event) {
		hideUploadPreview();
		clearImageUpload();
		//override value
		flag.setText(UploadStatus.DELETE.name());
	}

	public void showUploadPreview(String imageBlobKey) {
		imageThumbnail.setUrl("/nl/thumb?key=" + imageBlobKey + "&w=" + THUMB_WIDTH + "&h=" + THUMB_HEIGHT);
		imageThumbnail.setVisible(true);
		imageDeleteButton.setVisible(true);
		image.setVisible(false);
	}

	public void hideUploadPreview() {
		image.setVisible(true);
		imageThumbnail.setVisible(false);
		imageDeleteButton.setVisible(false);
	}

	public void clearImageUpload() {
		FileUpload newImage = new FileUpload();
		newImage.setName(image.getName());

		image.removeFromParent();
		uploadPanel.clear();
		uploadPanel.add(newImage);
		newImage.addChangeHandler(uploadChangeHandler);
		
		flag.setText(UploadStatus.NO_CHANGE.name());
		
		image = newImage;
	}
}
