package com.denisk.appengine.nl.client;

import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;


public class Nl implements EntryPoint {
	private static final String THUMB_WIDTH = "200";
	private static final String THUMB_HEIGHT = "100";

	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private Label categoriesInfo = new Label();
	
	private final FlowPanel outputPanel = new FlowPanel(); 
	private final Label status = new Label();
	private final RootPanel rootPanel = RootPanel.get("container");
	private HandlerRegistration clearButtonHandlerRegistration;
	
	private Button clearButton;
	private Button newButton;
	private Button backButton;
	
	private String selectedCategoryKeyStr;
	
	private ClickHandler categoriesClearButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			dtoService.clearData(new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					updateLabel(status);
					outputCategories(outputPanel);
				}

				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
	};
	private ClickHandler newCategoryFormSubmitCallback = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			updateLabel(status);
			outputCategories(outputPanel);
		}
	};
	private CategoryPersister categoryPersister = new CategoryPersister();
	private GoodPersister goodPersister = new GoodPersister();
	
	private EditCategoryForm form = new EditCategoryForm();
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		updateLabel(status);
		
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		rootPanel.add(outputPanel);
		
		backButton = new Button("Back");
		backButton.setVisible(false);
		rootPanel.add(backButton);
		backButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setCategoriesControls();
				outputCategories(outputPanel);
			}
		});
		
		outputCategories(outputPanel);
		
		outputCategoriesControls();

	}

	private void outputCategoriesControls() {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus userStatus) {
				switch(userStatus) {
				case ADMIN:
					clearButton = new Button("Clear all");
					newButton = new Button("New item");
					rootPanel.add(clearButton);
					rootPanel.add(newButton);
					newButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							form.show();
						}
					});
					createLogoutUrl();
					
					setCategoriesControls();
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

			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private void setCategoriesControls() {
		if(clearButtonHandlerRegistration != null){
			clearButtonHandlerRegistration.removeHandler();
		}
		clearButtonHandlerRegistration = clearButton.addClickHandler(categoriesClearButtonClickHandler);
		form.setRedrawPageAfterItemPersistedCallback(newCategoryFormSubmitCallback);
		form.setMisterPersister(categoryPersister);
	}
	
	private void outputCategories(final Panel panel) {
		backButton.setVisible(false);
		
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				panel.clear();
				JsArray<ShopItem> arrayFromJson = ShopItem.getArrayFromJson(result);
				for(int i = 0; i < arrayFromJson.length(); i++){
					final ShopItem itemJson = arrayFromJson.get(i);
					LayoutPanel itemPanel = createShopItemPanel(itemJson);
					
					itemPanel.addDomHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
								backButton.setVisible(true);
								String keyStr = itemJson.getKeyStr();
								outputGoodsForCategory(keyStr);
								outputControlsForGoods();
								selectedCategoryKeyStr = keyStr;
						}
					}, ClickEvent.getType());

					panel.add(itemPanel);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
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

	private void outputControlsForGoods() {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus result) {
				switch(result){
				case ADMIN:
					if(clearButtonHandlerRegistration != null){
						clearButtonHandlerRegistration.removeHandler();
					}
					if (clearButton != null) {
						ClickHandler goodsClearButtonClickHandler = new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if(selectedCategoryKeyStr != null) {
									dtoService.clearGoodsForCategory(selectedCategoryKeyStr, new AsyncCallback<Void>() {
										@Override
										public void onSuccess(Void result) {
											outputGoodsForCategory(selectedCategoryKeyStr);
											updateLabel(status);
										}
										
										@Override
										public void onFailure(Throwable caught) {
										}
									});
								}
							}
						};
						
						clearButtonHandlerRegistration = clearButton.addClickHandler(goodsClearButtonClickHandler);
						goodPersister.setParentCategoryItemKeyStr(selectedCategoryKeyStr);

						form.setMisterPersister(goodPersister);
						form.setRedrawPageAfterItemPersistedCallback(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								updateLabel(status);
								outputGoodsForCategory(selectedCategoryKeyStr);
							}
						});
					}
					break;
				case NOT_ADMIN:
					break;
				case NOT_LOGGED_IN:
					break;
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private void outputGoodsForCategory(String categoryKeyStr) {
		dtoService.getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				outputPanel.clear();
				JsArray<ShopItem> arrayFromJson = ShopItem.getArrayFromJson(result);
				for(int i = 0; i < arrayFromJson.length(); i++){
					final ShopItem itemJson = arrayFromJson.get(i);
					LayoutPanel itemPanel = createShopItemPanel(itemJson);
					outputPanel.add(itemPanel);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
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
	
	protected LayoutPanel createShopItemPanel(final ShopItem itemJson) {
		final Label name = new Label(itemJson.getName());
		Label description = new Label(itemJson.getDescription());
		Image image = new Image("/nl/thumb?key=" + itemJson.getImageBlobKey() + "&w=" + THUMB_WIDTH + "&h=" + THUMB_HEIGHT);
		
		LayoutPanel itemPanel = new LayoutPanel();

		itemPanel.addStyleName("category");
		
		itemPanel.add(name);
		itemPanel.add(image);
		itemPanel.add(description);
		
		itemPanel.setWidgetLeftRight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		itemPanel.setWidgetTopHeight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		
		itemPanel.setWidgetLeftRight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(description, 5, Style.Unit.PX, 20, Style.Unit.PX);
		
		itemPanel.setWidgetLeftRight(image, 0, Style.Unit.PX, 10, Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(image, 10, Style.Unit.PX, 150, Style.Unit.PX);
		itemPanel.setWidgetHorizontalPosition(image, com.google.gwt.layout.client.Layout.Alignment.END);
		return itemPanel;
	}


}
