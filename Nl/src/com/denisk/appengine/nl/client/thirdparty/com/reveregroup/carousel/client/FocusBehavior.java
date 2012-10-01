package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.ArrayList;
import java.util.List;

import com.denisk.appengine.nl.client.SingleGoodPanel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
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
					DockPanel dockPanel = new DockPanel();
					edit = new Button("Edit");
					delete = new Button("Delete");
					edit.setVisible(false);
					delete.setVisible(false);
					ClickHandler closeHandler = new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							popup.hide();
						}
					};
					edit.addClickHandler(closeHandler);
					delete.addClickHandler(closeHandler);
					FlowPanel buttonsPanel = new FlowPanel();
					buttonsPanel.add(edit);
					buttonsPanel.add(delete);
					dockPanel.add(buttonsPanel, DockPanel.NORTH);
					dockPanel.add(panel, DockPanel.CENTER);
				    popup.add(dockPanel);
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
					delete.setVisible(true);
					if(deleteRegistration != null){
						deleteRegistration.removeHandler();
					}
					deleteRegistration = delete.addClickHandler(photo.getDeleteClickHandler());
				} else {
					delete.setVisible(false);
				}
				
				if(photo.getEditClickHandler() != null){
					edit.setVisible(true);
					if(editRegistration != null){
						editRegistration.removeHandler();
					}
					editRegistration = edit.addClickHandler(photo.getEditClickHandler());
				} else {
					edit.setVisible(false);
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
