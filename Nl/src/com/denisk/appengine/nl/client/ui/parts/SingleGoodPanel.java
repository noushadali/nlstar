/**
 * 
 */
package com.denisk.appengine.nl.client.ui.parts;

import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author denisk
 *
 */
public class SingleGoodPanel extends Composite {

	private static SingleGoodPanelUiBinder uiBinder = GWT
			.create(SingleGoodPanelUiBinder.class);
	@UiField Label name;
	@UiField Image image;
	@UiField PopupPanel popup;
	@UiField Label edit;
	@UiField Label delete;
	@UiField Label close;
	@UiField HTMLPanel description;
	@UiField FlowPanel editContainer;
	
	protected HandlerRegistration editRegistration;
	protected HandlerRegistration deleteRegistration;
	
	private CloseHandler<PopupPanel> popupCloseHandler;
	
	private ClickHandler closeHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			popup.hide();
		}
	};


	interface SingleGoodPanelUiBinder extends UiBinder<Widget, SingleGoodPanel> {
	}

	/**
	 * Because this class has a default constructor, it can
	 * be used as a binder template. In other words, it can be used in other
	 * *.ui.xml files as follows:
	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 *   xmlns:g="urn:import:**user's package**">
	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
	 * </ui:UiBinder>
	 * Note that depending on the widget that is used, it may be necessary to
	 * implement HasHTML instead of HasText.
	 */
	public SingleGoodPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.image.getElement().setAttribute("align", "right");
		this.description.add(this.image);
		edit.addClickHandler(closeHandler);
		delete.addClickHandler(closeHandler);
		close.addClickHandler(closeHandler);
	}
	
	public void setName(String text){
		this.name.setText(text);
	}
	
	public void setImageUrl(String url){
		this.image.setUrl(url);
	}
	
	public void setDescription(String text){
		this.description.clear();
		
		//this is a hack because we can add children only to the bottom of htmlPanel
		this.description.getElement().setInnerHTML("<span id='anchor'></span>" + text);
		this.description.addAndReplaceElement(this.image, "anchor");
	}
	
	public void setPopupCloseHandler(CloseHandler<PopupPanel> handler){
		popupCloseHandler = handler;
	}
	
	@UiHandler("popup")
	void onPopupClose(CloseEvent<PopupPanel> event) {
		popupCloseHandler.onClose(event);
	}
	
	public void show(){
		popup.center();
	}

	public void hide(){
		popup.hide();
	}
	
	public void setEditClickHandler(ClickHandler handler){
		if(handler == null){
			throw new IllegalArgumentException("Edit click handler can't be null");
		}
		if(editRegistration != null){
			editRegistration.removeHandler();
		}
		editRegistration = edit.addClickHandler(handler);
		edit.setVisible(true);
	}
	
	public void setDeleteClickHandler(ClickHandler handler){
		if(handler == null){
			throw new IllegalArgumentException("Delete click handler can't be null");
		}
		if(deleteRegistration != null){
			deleteRegistration.removeHandler();
		}
		deleteRegistration = delete.addClickHandler(handler);
		delete.setVisible(true);
	}
	
	public void hideEditContainer(){
		description.getElement().getStyle().setBottom(0, Unit.PX);
		editContainer.setVisible(false);
	}
	public void showEditContainer(){
		description.getElement().getStyle().setBottom(50, Unit.PX);
		editContainer.setVisible(true);
	}
}
