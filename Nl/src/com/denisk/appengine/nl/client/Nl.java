package com.denisk.appengine.nl.client;

import javax.swing.GroupLayout.Alignment;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Nl implements EntryPoint {
	private static final String THUMB_WIDTH = "200";
	private static final String THUMB_HEIGHT = "100";
	
	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private Label categoriesInfo = new Label();
	
	private final FlowPanel goods = new FlowPanel();
	private final FlowPanel categories = new FlowPanel(); 
	private final Label status = new Label();
	private final RootPanel rootPanel = RootPanel.get("container");
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		updateLabel(status);
		
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		rootPanel.add(categories);
		outputCategories(categories);
		
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			
			@Override
			public void onSuccess(UserStatus userStatus) {
				switch(userStatus) {
				case ADMIN:
					final EditCategoryForm form = new EditCategoryForm();
					Button clearButton = new Button("Clear all");
					Button newButton = new Button("New category");
					rootPanel.add(clearButton);
					rootPanel.add(newButton);
					newButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							form.show();
						}
					});
					clearButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dtoService.clearData(new AsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									updateLabel(status);
									outputCategories(categories);
								}

								@Override
								public void onFailure(Throwable caught) {
								}
							});
						}
					});
					form.setSubmitCallback(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							updateLabel(status);
							outputCategories(categories);
						}
					});
					createLogoutUrl();
					break;
				case NOT_LOGGED_IN:
					dtoService.getLoginUrl(new AsyncCallback<String>() {
						
						@Override
						public void onSuccess(String result) {
							HTML link = new HTML();
							link.setHTML("<a href='" + result + "'>Login</a>");
							rootPanel.add(link);
						}
						
						@Override
						public void onFailure(Throwable caught) {
						}
					});
				
				break;
				case NOT_ADMIN:
					createLogoutUrl();
					break;
				}
			}
			private void createLogoutUrl() {
				dtoService.getLogoutUrl(new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						HTML link = new HTML();
						link.setHTML("<a href='" + result + "'>Logout</a>");
						rootPanel.add(link);
					}
					
					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});

	}
	
	private void outputCategories(final Panel panel) {
		panel.clear();
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				JsArray<CategoryJavascriptObject> arrayFromJson = CategoryJavascriptObject.getArrayFromJson(result);
				categoriesInfo.setText("");
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < arrayFromJson.length(); i++){
					CategoryJavascriptObject categoryJson = arrayFromJson.get(i);
					
					LayoutPanel categoryPanel = createItemPanel(categoryJson);
					
					panel.add(categoryPanel);
				}
//				rootPanel.add(image);
				categoriesInfo.setText(sb.toString());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				categoriesInfo.setText("Categories Parse failed");
			}
		});
	}
	private void updateLabel(final Label status) {
		dtoService.countEntities(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				status.setText(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				status.setText("Can't calculate entities");
			}
		});
	}

	private LayoutPanel createItemPanel(
			final ShopItem itemJson) {
		LayoutPanel categoryPanel = createShopItemPanel(itemJson);
		
		categoryPanel.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				categories.clear();
				dtoService.getGoodsJson(itemJson.getKeyStr(), new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String goodsJson) {
						JsArray<GoodJavascriptObject> goodsJsArray = GoodJavascriptObject.<GoodJavascriptObject>getArrayFromJson(goodsJson);
						outputGoods(goods, goodsJsArray);
					}
					
					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
		}, ClickEvent.getType());

		return categoryPanel;
	}

	private LayoutPanel createShopItemPanel(final ShopItem itemJson) {
		final Label name = new Label(itemJson.getName());
		Label description = new Label(itemJson.getDescription());
		Image image = new Image("/nl/thumb?key=" + itemJson.getImageKey() + "&w=" + THUMB_WIDTH + "&h=" + THUMB_HEIGHT);
		
		LayoutPanel categoryPanel = new LayoutPanel();

		categoryPanel.addStyleName("category");
		
		categoryPanel.add(name);
		categoryPanel.add(image);
		categoryPanel.add(description);
		
		categoryPanel.setWidgetLeftRight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		categoryPanel.setWidgetTopHeight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		
		categoryPanel.setWidgetLeftRight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
		categoryPanel.setWidgetBottomHeight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
		
		categoryPanel.setWidgetLeftRight(image, 0, Style.Unit.PX, 10, Style.Unit.PX);
		categoryPanel.setWidgetBottomHeight(image, 10, Style.Unit.PX, 150, Style.Unit.PX);
		categoryPanel.setWidgetHorizontalPosition(image, com.google.gwt.layout.client.Layout.Alignment.END);
		return categoryPanel;
	}
	
	private void outputGoods(FlowPanel goods,
			JsArray<GoodJavascriptObject> goodsJsArray) {
		for(int i = 0; i < goodsJsArray.length(); i++){
			GoodJavascriptObject goodJs = goodsJsArray.get(i);
			LayoutPanel panel = createItemPanel(goodJs);
			goods.add(panel);
		}
	}


}
