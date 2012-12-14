package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.service.DtoService;
import com.denisk.appengine.nl.client.service.DtoServiceAsync;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Carousel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.ui.parts.EditCategoryForm;
import com.denisk.appengine.nl.client.ui.parts.EditForm;
import com.denisk.appengine.nl.client.ui.parts.EditGoodForm;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
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
import com.google.gwt.user.client.ui.Widget;

public class Nl implements EntryPoint {
	public static final String THUMB_WIDTH = "200";
	public static final String THUMB_HEIGHT = "100";

	private static final String CATEGORY_URL_PREFIX = "category/";
	private static final String GOOD_URL_PREFIX = "good/";
	
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

	// state fields
	private String selectedCategoryKeyStr;

	//views
	private AbstractItemsView currentView;
	
	private CategoriesView categoriesView;
	private GoodsView goodsView;
	private SingleGoodView singleGoodView;
	
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

	private void setCategoriesAdminButtonHandlers(AbstractItemsView view) {
		if (newButtonClickHandlerRegistration != null) {
			newButtonClickHandlerRegistration.removeHandler();
		}

		if (newButton != null) {
			newButtonClickHandlerRegistration = newButton
					.addClickHandler(view.getNewItemHandler());
		}
		if (clearButtonHandlerRegistration != null) {
			clearButtonHandlerRegistration.removeHandler();
		}
		if (clearButton != null) {
			clearButtonHandlerRegistration = clearButton
					.addClickHandler(view.getClearAllHandler());
		}
	}


	// redraw callbacks==============================
	private Function<Void, Void> redrawCategoriesCallback = new Function<Void, Void>() {
		@Override
		public Void apply(Void input) {
			editCategoryForm.hide();
			updateLabel(status);
			backButton.setVisible(false);
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
	// ==============================================
	private EditGoodForm editGoodForm = new EditGoodForm();

	private AsyncCallback<Void> getRedrawingCallback(
			final Function<Void, Void> redrawing) {
		return new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error processing deletion of the item, exception is "
						+ caught);
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
			dtoService.deleteCategory(input.getKeyStr(),
					input.getImageBlobKey(), input.getBackgroundBlobKey(),
					getRedrawingCallback(redrawCategoriesCallback));
			return null;
		}
	};

	private Function<GoodJavascriptObject, Void> goodDeletion = new Function<GoodJavascriptObject, Void>() {
		@Override
		public Void apply(GoodJavascriptObject input) {
			dtoService.deleteGood(input.getKeyStr(), input.getImageBlobKey(),
					getRedrawingCallback(redrawGoodsCallback));
			return null;
		}
	};

	private Function<GoodJavascriptObject, LayoutPanel> goodPanelCreation = new Function<GoodJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(GoodJavascriptObject input) {
			return createShopItemPanel(input);
		}
	};

	/**
	 * This is not used anymore, since we use Carousel
	 */
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
		edit.addClickHandler(getEditClickHandler(item, editForm));
		panel.add(edit);

		panel.setWidgetRightWidth(edit, 60, Style.Unit.PX, 30, Style.Unit.PX);
		panel.setWidgetTopHeight(edit, 10, Style.Unit.PX, 20, Style.Unit.PX);
	}

	/**
	 * Deletes an item and redraws the panel
	 */
	private <T extends ShopItem> void buildDeleteButton(final T item,
			LayoutPanel panel, final Function<T, Void> deletion) {
		HTML delete = new HTML("<a href='javascript://'>Delete</a>");

		delete.addClickHandler(getDeleteClickHandler(item, deletion));

		panel.add(delete);

		panel.setWidgetRightWidth(delete, 15, Style.Unit.PX, 40, Style.Unit.PX);
		panel.setWidgetTopHeight(delete, 10, Style.Unit.PX, 20, Style.Unit.PX);

	}

	private <T extends ShopItem> ClickHandler getDeleteClickHandler(
			final T item, final Function<T, Void> deletion) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (Window.confirm("Are you sure you want to delete "
						+ item.getName() + "?")) {
					deletion.apply(item);
				}
			}
		};
	}

	
	private <T extends ShopItem> ClickHandler getEditClickHandler(final T item,
			final EditForm<T> editForm) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				editForm.showForEdit(item);
			}
		};
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		editGoodForm.setRedrawAfterItemCreatedCallback(redrawGoodsCallback);
		editCategoryForm
				.setRedrawAfterItemCreatedCallback(redrawCategoriesCallback);

		updateLabel(status);
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		rootPanel.add(outputPanel);

		clearButton = new Button("Clear all");
		newButton = new Button("New item");
		rootPanel.add(clearButton);
		rootPanel.add(newButton);

		outputPanel.addStyleName("outputPanel");
		backButton = new Button("Back");
		backButton.setVisible(false);
		rootPanel.add(backButton);
		backButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// set transitioned style to the carousel

				carousel.addStyleName("carouselDownAnimated");
				Timer t = new Timer() {

					@Override
					public void run() {
						// move carousel far down
						// this will last 2 seconds
						carousel.getElement().getStyle()
								.setTop(Window.getClientHeight(), Unit.PX);
					}
				};
				t.schedule(500);

				Timer t1 = new Timer() {
					@Override
					public void run() {
						setCategoriesAdminButtonHandlers(categoriesView);
						backButton.setVisible(false);
						outputCategories(outputPanel);
						//this clears everything in the URL starting from '#' inclusive
						History.newItem("");
					}
				};
				t1.schedule(2000 + 500 + 100/* just in case */);
			}
		});

		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String token = event.getValue();
				if(token == null || token.isEmpty()){
					renderAllCategories();
					return;
				}
				String categoryKeyRegexp;
				Function<List<Photo>, Void> callback; 
				if(token.startsWith(CATEGORY_URL_PREFIX) && !token.contains(GOOD_URL_PREFIX)){
					categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/";
					callback = new Function<List<Photo>, Void>() {
						@Override
						public Void apply(List<Photo> input) {
							return null;
						}
					};
				} else if(token.startsWith(CATEGORY_URL_PREFIX) && token.contains(GOOD_URL_PREFIX)){
					categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/good";
				
					RegExp goodRegexp = RegExp.compile(".+" + GOOD_URL_PREFIX + "(.+)/");
					MatchResult goodMatch = goodRegexp.exec(token);
					if(goodMatch == null){
						Window.alert("Wrong format for good in URL, should be '" + GOOD_URL_PREFIX + "'");
						History.newItem("", false);
						renderAllCategories();
						return;
					}
					final String goodKey = goodMatch.getGroup(1);
					
					callback = new Function<List<Photo>, Void>(){
						@Override
						public Void apply(List<Photo> input) {
							//pop single good window
							for(Photo photo: input){
								if(photo.getId().equals(goodKey)){
									PhotoClickEvent event = new PhotoClickEvent();
									event.setPhoto(photo);
									event.setShouldChangeURL(false);
									
									carousel.fireEvent(event);
								}
							}
							return null;
						}
					};
				}else {
					Window.alert("URL must start with '" + CATEGORY_URL_PREFIX + "' token");
					History.newItem("", false);
					renderAllCategories();
					return;
				}
				
				RegExp p = RegExp.compile(categoryKeyRegexp);
				MatchResult m = p.exec(token);
				if(m == null){
					Window.alert("There is no '" + CATEGORY_URL_PREFIX + " in the URL provided");
					renderAllCategories();
					return;
				}
				String categoryKey = m.getGroup(1);
				
				renderGoods(categoryKey, callback);
			}
		});
		
		History.fireCurrentHistoryState();
	}

	/**
	 * Calculates total items count and updates corresponding label
	 */
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
		backButton.setVisible(true);
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus result) {
				switch (result) {
				case ADMIN:
					editGoodForm.setParentCategoryItemKeyStr(selectedCategoryKeyStr);
					setCategoriesAdminButtonHandlers(goodsView);
					break;
				case NOT_ADMIN:
					break;
				case NOT_LOGGED_IN:
					break;
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Cannot determine if a user is admin: " + caught.getMessage());
			}
		});
	}

	/**
	 * This method creates fills carousel with good items and adds carousel to
	 * outputPanel
	 * @param callbacks 
	 */
	private void outputGoodsForCategory(final String categoryKeyStr,
			final FlowPanel panel, final Function<List<Photo>, Void>... callbacks) {

		dtoService.getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				// move carousel far down
				carousel.getElement().getStyle()
						.setTop(Window.getClientHeight(), Unit.PX);

				panel.clear();
				final JsArray<GoodJavascriptObject> goods = GoodJavascriptObject
						.getArrayFromJson(json);
				if (goods.length() > 0) {
					panel.add(carousel);
					final ArrayList<Photo> photos = new ArrayList<Photo>(goods
							.length());
					for (int i = 0; i < goods.length(); i++) {
						GoodJavascriptObject good = goods.get(i);
						String imageUrl = getImageUrl(good, "600", "600");
						Photo photo = new Photo(imageUrl, good.getName(), good
								.getDescription(), good.getKeyStr());
						photos.add(photo);
					}
					dtoService.isAdmin(new AsyncCallback<UserStatus>() {

						@Override
						public void onSuccess(UserStatus result) {
							if (UserStatus.ADMIN == result) {
								for (int i = 0; i < photos.size(); i++) {
									Photo photo = photos.get(i);
									GoodJavascriptObject good = goods.get(i);

									photo.setEditClickHandler(getEditClickHandler(
											good, editGoodForm));
									photo.setDeleteClickHandler(getDeleteClickHandler(
											good, goodDeletion));
								}
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							Window.alert("Can't determine credentials for user");
						}
					});
					carousel.setPhotos(photos);
					
					for(Function<List<Photo>, Void> callback: callbacks){
						callback.apply(photos);
					}
					
					// Slide the carousel from the bottom
					Timer t = new Timer() {

						@Override
						public void run() {
							carousel.removeStyleName("carouselDownAnimated");
							carousel.addStyleName("carouselAnimated");
						}
					};
					t.schedule(500);
					// remove 'top' style after the carousel has arrived
					Timer t1 = new Timer() {

						@Override
						public void run() {
							// remove 'top' property from the carousel
							carousel.getElement().getStyle()
									.setTop(100, Unit.PX);
							carousel.removeStyleName("carouselAnimated");
						}
					};
					t1.schedule(2000 + 500);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("There is no category with identifier " + categoryKeyStr);
			}
		});
	}


	protected LayoutPanel createShopItemPanel(final ShopItem itemJson) {
		final Label name = new Label(itemJson.getName());
		Label description = new Label(itemJson.getDescription());
		Image image = new Image(
				getImageUrl(itemJson, THUMB_WIDTH, THUMB_HEIGHT));

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

	public static String getImageUrl(final ShopItem itemJson, String width,
			String height) {
		return "/nl/thumb?key=" + itemJson.getImageBlobKey() + "&w=" + width
				+ "&h=" + height;
	}

	/**
	 * @param callbacks list of callbacks to call when carousel photos are retrieved
	 */
	private void renderGoods(String categoryKey, Function<List<Photo>, Void>... callbacks) {
		this.selectedCategoryKeyStr = categoryKey;
		outputGoodsForCategory(categoryKey, outputPanel, callbacks);
		outputControlsForGoods();
	}

	public static String getCategoryURLPart(String categoryKeyStr) {
		return CATEGORY_URL_PREFIX + categoryKeyStr + "/";
	}
	
	public static String getGoodURLPart(String goodKeyStr){
		return GOOD_URL_PREFIX + goodKeyStr + "/";
	}
	
}
