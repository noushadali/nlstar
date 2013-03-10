package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.ArrayList;
import java.util.List;

import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusHandler;
import com.denisk.appengine.nl.client.ui.parts.SingleGoodPanel;
import com.denisk.appengine.nl.client.ui.views.Nl;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
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

	protected SingleGoodPanel panel = new SingleGoodPanel();

	protected Widget focusDecoratorWidget = null;
	
	protected PhotoFocusEvent lastFocusEvent = null;
	
	protected List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>(2);

	protected Button edit;
	protected Button delete;
	
	public FocusBehavior(Carousel carousel) {
		this.target = carousel;
		handlerManager = new HandlerManager(this);
	}
	
	public void start() {
		if (eventHandlers.size() > 0) {
			return; //already started
		}
		
		eventHandlers.add(target.addPhotoClickHandler(new PhotoClickHandler() {
			public void photoClicked(PhotoClickEvent event) {
				PhotoFocusEvent evt = new PhotoFocusEvent();
				evt.setPhoto(event.getPhoto());
				evt.setPhotoIndex(event.getPhotoIndex());
				lastFocusEvent = evt;
				//put URL into the history
				String goodURLPart = Nl.getGoodURLPart(event.getPhoto()
						.getId());
				//this assumes that category part is already in the URL
				History.newItem(History.getToken() + goodURLPart, false);
				//this will be fired from History.onValueChange handler
				handlerManager.fireEvent(evt);
			}
		}));
		
		eventHandlers.add(addPhotoFocusHandler(new PhotoFocusHandler() {
			public void photoFocused(PhotoFocusEvent event) {
			    panel.setPopupCloseHandler(new CloseHandler<PopupPanel>() {
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						panel.hide();
						PhotoUnfocusEvent evt = new PhotoUnfocusEvent();
						evt.setPhotoIndex(lastFocusEvent.getPhotoIndex());
						evt.setPhoto(lastFocusEvent.getPhoto());
						//cut the good from the URL
						cutOutGoodHistory();

						handlerManager.fireEvent(evt);
					}
				});
			
			
				Photo photo = event.getPhoto();
				
				panel.setName(photo.getTitle());
				panel.setImageUrl(photo.getUrl());
				panel.setDescription(photo.getText());
				
				if(photo.getDeleteClickHandler() != null){
					panel.setDeleteClickHandler(photo.getDeleteClickHandler());
					panel.showEditContainer();
				} else {
					panel.hideEditContainer();
				}
				
				if(photo.getEditClickHandler() != null){
					panel.showEditContainer();
					panel.setEditClickHandler(photo.getEditClickHandler());
				} else {
					panel.hideEditContainer();
				}
				
				panel.show();
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

	private void cutOutGoodHistory() {
		String token = History.getToken();
		int goodStarts = token.indexOf("/good");
		String cutToken = token.substring(0, goodStarts + 1);
		History.newItem(cutToken, false);
	}
	
}
