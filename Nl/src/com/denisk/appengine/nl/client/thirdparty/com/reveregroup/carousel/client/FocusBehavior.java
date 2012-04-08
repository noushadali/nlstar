package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
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

	protected Element lightbox;
	protected PopupPanel popup;
	protected DockPanel dockPanel = new DockPanel();
	protected Image popupImage;

	protected Widget focusDecoratorWidget = null;
	
	protected PhotoFocusEvent lastFocusEvent = null;
	
	protected List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>(2);

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
					lightbox = DOM.createDiv();
				    lightbox.setClassName("lightbox");
				    if (Utils.getUserAgent().contains("MSIE")) {
				    	lightbox.setClassName("lightbox lightboxIE");
				    }
				    lightbox.getStyle().setProperty("zIndex", "100");
				    RootPanel.getBodyElement().appendChild(lightbox);
				    popup = new PopupPanel(true,true);
					popupImage = new Image();
					dockPanel.add(popupImage, DockPanel.CENTER);
				    popup.add(dockPanel);
				    popup.getElement().getStyle().setProperty("zIndex", "150");
					popup.addCloseHandler(new CloseHandler<PopupPanel>(){
						public void onClose(CloseEvent<PopupPanel> event) {
							popup.hide();
							lightbox.getStyle().setProperty("display", "none");
							PhotoUnfocusEvent evt = new PhotoUnfocusEvent();
							evt.setPhotoIndex(lastFocusEvent.getPhotoIndex());
							evt.setPhoto(lastFocusEvent.getPhoto());
							handlerManager.fireEvent(evt);
						}
					});
				}
				
				popupImage.setUrl(event.getPhoto().getUrl());
				lightbox.getStyle().setProperty("display", "block");
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
	
	public void setFocusDecoratorWidget(Widget widget, DockLayoutConstant position) {
		if (focusDecoratorWidget != null && focusDecoratorWidget.getParent() == dockPanel) {
			focusDecoratorWidget.removeFromParent();
		}
		focusDecoratorWidget = widget;
		dockPanel.add(widget, position);
	}
}
