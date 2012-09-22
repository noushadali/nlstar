package com.denisk.appengine.nl.client;

import java.util.ArrayList;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Carousel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
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
	public static final String THUMB_WIDTH = "200"   ;
	public static final String THUMB_HEIGHT = "100";

	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private Label categoriesInfo = new Label();

	private final FlowPanel outputPanel = new FlowPanel();
	private final Label status = new Label();
	private final RootPanel rootPanel = RootPanel.get("container");
	private HandlerRegistration newButtonClickHandlerRegistration;
	private HandlerRegistration clearButtonHandlerRegistration;
	private Carousel carousel = new Carousel();
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
	// redraw callbacks==============================
	private Function<Void, Void> redrawCategoriesCallback = new Function<Void, Void>() {
		@Override
		public Void apply(Void input) {
			editCategoryForm.hide();
			updateLabel(status);
			outputCategories(outputPanel);

			return null;
		}
	};
	private Function<Void, Void> redrawGoodsCallback = new Function<Void, Void>() {
		@Override
		public Void apply(Void input) {
			editGoodForm.hide();
			updateLabel(status);
			outputGoodsForCategory(selectedCategoryKeyStr, outputPanel);

			return null;
		}
	};
	// ==============================================
	private ClickHandler goodsNewButtonHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			editGoodForm.showForCreation();
		}
	};
	private ClickHandler categoriesNewButtonHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			editCategoryForm.showForCreation();
		}
	};
	// ==============================================
	private EditGoodForm editGoodForm = new EditGoodForm();
	private EditCategoryForm editCategoryForm = new EditCategoryForm();
	
	private AsyncCallback<Void> getRedrawingCallback(final Function<Void, Void> redrawing){
		return new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error processing deletion of the item, exception is " + caught);
			}

			@Override
			public void onSuccess(Void result) {
				redrawing.apply(null);
			}
		};
	}

	private Function<CategoryJavascriptObject, Void> categoryDeletion = new Function<CategoryJavascriptObject, Void>() {
		@Override
		public Void apply(CategoryJavascriptObject input) {
			dtoService.deleteCategory(input.getKeyStr(), input.getImageBlobKey(), input.getBackgroundBlobKey(), getRedrawingCallback(redrawCategoriesCallback));
			return null;
		}
	};
	
	private Function<GoodJavascriptObject, Void> goodDeletion = new Function<GoodJavascriptObject, Void>() {
		@Override
		public Void apply(GoodJavascriptObject input) {
			dtoService.deleteGood(input.getKeyStr(), input.getImageBlobKey(), getRedrawingCallback(redrawGoodsCallback));
			return null;
		}
	};

	private Function<CategoryJavascriptObject, LayoutPanel> categoryPanelCreation = new Function<CategoryJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(CategoryJavascriptObject input) {
			return createCategoryPanel(input);
		}
	};

	private Function<CategoryJavascriptObject, LayoutPanel> editableCategoryPanelCreation = new Function<CategoryJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(final CategoryJavascriptObject category) {
			LayoutPanel panel = categoryPanelCreation.apply(category);
			buildEditButton(category, panel, editCategoryForm);
			buildDeleteButton(category, panel, categoryDeletion);
			return panel;
		}
	};

	private Function<GoodJavascriptObject, LayoutPanel> goodPanelCreation = new Function<GoodJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(GoodJavascriptObject input) {
			return createShopItemPanel(input);
		}
	};

	private Function<GoodJavascriptObject, LayoutPanel> editableGoodPanelCreation = new Function<GoodJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(GoodJavascriptObject input) {
			LayoutPanel panel = goodPanelCreation.apply(input);
			buildEditButton(input, panel, editGoodForm);
			buildDeleteButton(input, panel, goodDeletion);
			return panel;
		}
	};

	private <T extends ShopItem> void buildEditButton(final T item,
			LayoutPanel panel, final EditForm<T> editForm) {
		HTML edit = new HTML("<a href=#>Edit</a>");
		edit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				editForm.showForEdit(item);
			}
		});
		panel.add(edit);

		panel.setWidgetRightWidth(edit, 60, Style.Unit.PX, 30, Style.Unit.PX);
		panel.setWidgetTopHeight(edit, 10, Style.Unit.PX, 20, Style.Unit.PX);
	}

	/**
	 * Deletes an item and redraws the panel
	 */
	private <T extends ShopItem> void buildDeleteButton(final T item, LayoutPanel panel, final Function<T, Void> deletion){
		HTML delete = new HTML("<a href='javascript://'>Delete</a>");

		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if(Window.confirm("Are you sure you want to delete " + item.getName() + "?")){
					deletion.apply(item);
				}
			}
		});
		
		panel.add(delete);
		
		panel.setWidgetRightWidth(delete, 15, Style.Unit.PX, 40, Style.Unit.PX);
		panel.setWidgetTopHeight(delete, 10, Style.Unit.PX, 20, Style.Unit.PX);
		
	}
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		editGoodForm
				.setRedrawAfterItemCreatedCallback(redrawGoodsCallback);
		editCategoryForm
				.setRedrawAfterItemCreatedCallback(redrawCategoriesCallback);

		updateLabel(status);

		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		rootPanel.add(outputPanel);
		outputPanel.addStyleName("outputPanel");
		backButton = new Button("Back");
		backButton.setVisible(false);
		rootPanel.add(backButton);
		backButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setCategoriesControlsIfNeeded();
				outputCategories(outputPanel);
				if (newButtonClickHandlerRegistration != null) {
					newButtonClickHandlerRegistration.removeHandler();
				}

				if (newButton != null) {
					newButtonClickHandlerRegistration = newButton
							.addClickHandler(categoriesNewButtonHandler);
				}
			}
		});

		outputCategories(outputPanel);

		outputCategoriesControls();

	}

	private void outputCategoriesControls() {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus userStatus) {
				switch (userStatus) {
				case ADMIN:
					clearButton = new Button("Clear all");
					newButton = new Button("New item");
					rootPanel.add(clearButton);
					rootPanel.add(newButton);
					if (newButtonClickHandlerRegistration != null) {
						newButtonClickHandlerRegistration.removeHandler();
					}

					newButtonClickHandlerRegistration = newButton
							.addClickHandler(categoriesNewButtonHandler);
					createLogoutUrl();

					setCategoriesControlsIfNeeded();
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

	private void setCategoriesControlsIfNeeded() {
		if (clearButtonHandlerRegistration != null) {
			clearButtonHandlerRegistration.removeHandler();
		}
		if (clearButton != null) {
			clearButtonHandlerRegistration = clearButton
					.addClickHandler(categoriesClearButtonClickHandler);
		}
	}

	private void outputCategories(final Panel panel) {
		backButton.setVisible(false);
		final Function<CategoryJavascriptObject, LayoutPanel> editableCreation = editableCategoryPanelCreation;
		final Function<CategoryJavascriptObject, LayoutPanel> creation = categoryPanelCreation;
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				createShopItemsFromJson(panel, creation, editableCreation, json);
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
				switch (result) {
				case ADMIN:
					if (clearButtonHandlerRegistration != null) {
						clearButtonHandlerRegistration.removeHandler();
					}
					if (clearButton != null) {
						ClickHandler goodsClearButtonClickHandler = new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (selectedCategoryKeyStr != null) {
									dtoService.clearGoodsForCategory(
											selectedCategoryKeyStr,
											new AsyncCallback<Void>() {
												@Override
												public void onSuccess(
														Void result) {
													outputGoodsForCategory(
															selectedCategoryKeyStr,
															outputPanel);
													updateLabel(status);
												}

												@Override
												public void onFailure(
														Throwable caught) {
												}
											});
								}
							}
						};

						clearButtonHandlerRegistration = clearButton
								.addClickHandler(goodsClearButtonClickHandler);
						editGoodForm
								.setParentCategoryItemKeyStr(selectedCategoryKeyStr);

						if (newButtonClickHandlerRegistration != null) {
							newButtonClickHandlerRegistration.removeHandler();
						}

						newButtonClickHandlerRegistration = newButton
								.addClickHandler(goodsNewButtonHandler);
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

	private void outputGoodsForCategory(String categoryKeyStr,
			final FlowPanel panel) {
		final Function<GoodJavascriptObject, LayoutPanel> creation = goodPanelCreation;
		final Function<GoodJavascriptObject, LayoutPanel> editableCeation = editableGoodPanelCreation;
		dtoService.getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				outputPanel.clear();
				JsArray<GoodJavascriptObject> goods = GoodJavascriptObject.getArrayFromJson(json);
				if(goods.length()> 0) {
					outputPanel.add(carousel);
					ArrayList<Photo> photos = new ArrayList<Photo>();
					for(int i = 0; i < goods.length(); i++){
						GoodJavascriptObject good = goods.get(i);
						String imageUrl = getImageUrl(good, "200", "200");
						System.out.println("Adding photo: " + imageUrl);
						Photo photo = new Photo(imageUrl, good.getName(), good.getDescription());
						photos.add(photo);
					}
					System.out.println("Setting photos: " + photos.toString());
					carousel.setPhotos(photos);
					//createShopItemsFromJson(outputPanel, goodPanelCreation, editableCeation, json);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private <T extends ShopItem> void createShopItemsFromJson(
			final Panel panel, final Function<T, LayoutPanel> creation,
			final Function<T, LayoutPanel> editableCeation, final String json) {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus result) {
				final JsArray<T> arrayFromJson = ShopItem.getArrayFromJson(json);
				panel.clear();
				switch (result) {
				case ADMIN:
					createTiles(panel, arrayFromJson, editableCeation);
					break;
				default:
					createTiles(panel, arrayFromJson, creation);
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
		Image image = new Image(getImageUrl(itemJson, THUMB_WIDTH, THUMB_HEIGHT));

		LayoutPanel itemPanel = new LayoutPanel();

		itemPanel.addStyleName("category");

		itemPanel.add(name);
		itemPanel.add(image);
		itemPanel.add(description);

		itemPanel.setWidgetLeftRight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);
		itemPanel.setWidgetTopHeight(name, 5, Style.Unit.PX, 20, Style.Unit.PX);

		itemPanel.setWidgetLeftRight(description, 5, Style.Unit.PX, 20,
				Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(description, 5, Style.Unit.PX, 20,
				Style.Unit.PX);

		itemPanel
				.setWidgetLeftRight(image, 0, Style.Unit.PX, 10, Style.Unit.PX);
		itemPanel.setWidgetBottomHeight(image, 10, Style.Unit.PX, 150,
				Style.Unit.PX);
		itemPanel.setWidgetHorizontalPosition(image,
				com.google.gwt.layout.client.Layout.Alignment.END);
		return itemPanel;
	}

	private String getImageUrl(final ShopItem itemJson, String width, String height) {
		return "/nl/thumb?key=" + itemJson.getImageBlobKey()
				+ "&w=" + width + "&h=" + height;
	}

	private <T extends ShopItem> void createTiles(final Panel panel,
			JsArray<T> arrayFromJson, Function<T, LayoutPanel> panelCreation) {
		for (int i = 0; i < arrayFromJson.length(); i++) {
			final T categoryJson = arrayFromJson.get(i);
			LayoutPanel itemPanel = panelCreation.apply(categoryJson);
			panel.add(itemPanel);
		}
	}

	private LayoutPanel createCategoryPanel(
			final CategoryJavascriptObject categoryJson) {
		LayoutPanel itemPanel = createShopItemPanel(categoryJson);

		Label backgroundLabel = new Label(categoryJson.getBackgroundBlobKey());
		itemPanel.add(backgroundLabel);
		itemPanel.setWidgetTopHeight(backgroundLabel, 40, Style.Unit.PX, 20,
				Style.Unit.PX);
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				backButton.setVisible(true);
				String keyStr = categoryJson.getKeyStr();
				outputGoodsForCategory(keyStr, outputPanel);
				outputControlsForGoods();
				selectedCategoryKeyStr = keyStr;
			}
		};

		itemPanel.addDomHandler(clickHandler, ClickEvent.getType());
		return itemPanel;
	}

}
