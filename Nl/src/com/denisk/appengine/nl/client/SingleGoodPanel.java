/**
 * 
 */
package com.denisk.appengine.nl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Image;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.CarouselImage;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * @author denisk
 *
 */
public class SingleGoodPanel extends Composite {

	private static SingleGoodPanelUiBinder uiBinder = GWT
			.create(SingleGoodPanelUiBinder.class);
	@UiField Label title;
	@UiField Image image;
	@UiField Label content;

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
	}
	
	public void setPanelTitle(String text){
		this.title.setText(text);
	}
	
	public void setImageUrl(String url){
		this.image.setUrl(url);
	}
	
	public void setContent(String text){
		this.content.setText(text);
	}
}
