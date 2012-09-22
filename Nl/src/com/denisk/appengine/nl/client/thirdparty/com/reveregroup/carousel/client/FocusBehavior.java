package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
import com.denisk.appengine.nl.client.SingleGoodPanel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusHandler;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class FocusBehavior {
	protected Carousel target;
	protected HandlerManager handlerManager;

	protected PopupPanel popup;
	protected SingleGoodPanel panel;

	protected Widget focusDecoratorWidget = null;
	
	protected PhotoFocusEvent lastFocusEvent = null;
	
	protected List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>(2);

	protected Button edit;
	protected Button delete;
	
	protected HandlerRegistration editRegistration;
	protected HandlerRegistration deleteRegistration;
	
	public FocusBehavior(Carousel carousel) {
		this.target = carousel;
		handlerManager = new HandlerManager(this);
	}
	
	public void start() {
		if (eventHandlers.size() > 0)
			return; //already started
		
		eventHandlers.add(target.addPhotoClickHandler(new PhotoClickHandler() {
			public void photoClicked(PhotoClickEvent event) {
				if (event.getPhotoIndex() == target.getPhotoIndex()) {
					PhotoFocusEvent evt = new PhotoFocusEvent();
					evt.setPhoto(event.getPhoto());
					evt.setPhotoIndex(event.getPhotoIndex());
					lastFocusEvent = evt;
					handlerManager.fireEvent(evt);
				}
			}
		}));
		
		eventHandlers.add(addPhotoFocusHandler(new PhotoFocusHandler() {
			public void photoFocused(PhotoFocusEvent event) {
				if (popup == null) {
				    popup = new PopupPanel(true,true);
					panel = new SingleGoodPanel();
					FlowPanel buttonsPanel = new FlowPanel();
					popup.add(buttonsPanel);
					edit = new Button("Edit");
					delete = new Button("Delete");
					buttonsPanel.add(edit);
					buttonsPanel.add(delete);
					//todo
				    popup.add(panel);
				    popup.getElement().getStyle().setProperty("zIndex", "150");
					popup.addCloseHandler(new CloseHandler<PopupPanel>(){
						public void onClose(CloseEvent<PopupPanel> event) {
							popup.hide();
							PhotoUnfocusEvent evt = new PhotoUnfocusEvent();
							evt.setPhotoIndex(lastFocusEvent.getPhotoIndex());
							evt.setPhoto(lastFocusEvent.getPhoto());
							handlerManager.fireEvent(evt);
						}
					});
					popup.addStyleName("good");
					popup.setGlassEnabled(true);
					popup.setAnimationEnabled(true);
				}
				
				Photo photo = event.getPhoto();
				panel.setPanelTitle(photo.getTitle());
				panel.setImageUrl(photo.getUrl());
				panel.setContent(photo.getText());
				
				if(photo.getDeleteClickHandler() != null){
					delete.setEnabled(true);
					if(deleteRegistration != null){
						deleteRegistration.removeHandler();
					}
					deleteRegistration = delete.addClickHandler(photo.getDeleteClickHandler());
				} else {
					delete.setEnabled(false);
				}
				
				if(photo.getEditClickHandler() != null){
					edit.setEnabled(true);
					if(editRegistration != null){
						editRegistration.removeHandler();
					}
					editRegistration = edit.addClickHandler(photo.getEditClickHandler());
				} else {
					edit.setEnabled(false);
				}
				
				popup.center();
			}
		}));		
	}
	
	public void stop() {
		for (HandlerRegistration handler : eventHandlers) {
			handler.removeHandler();
		}
		eventHandlers.clear();		
	}
	
	public HandlerRegistration addPhotoFocusHandler(PhotoFocusHandler handler) {
		return handlerManager.addHandler(PhotoFocusEvent.getType(), handler);
	}
	
	public HandlerRegistration addPhotoUnfocusHandler(PhotoUnfocusHandler handler) {
		return handlerManager.addHandler(PhotoUnfocusEvent.getType(), handler);
	}
	
}
