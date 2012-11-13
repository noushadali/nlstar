package com.denisk.appengine.nl.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Carousel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusEvent;
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

	private static final int ITEM_WIDTH = 300;
	private static final int ITEM_HEIGHT = 200;
	private static final double ANIMATION_DELAY = 0.1;
	private static final double ANIMATION_SPEED = 0.5;
	private static final int CATEGORIES_MARGIN = 10;
	private static final int TOP_OFFSET = 100;

	private static final String CATEGORY_URL_PREFIX = "category/";
	private static final String GOOD_URL_PREFIX = "good/";
	
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

	// state fields
	private String selectedCategoryKeyStr;
	private ArrayList<ArrayList<Widget>> widgetMatrix;

	private ClickHandler categoriesClearButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (Window
					.confirm("Are you sure you want to delete all categories and items in these categories?")) {
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
		}
	};
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
						setCategoriesAdminButtonHandlers();
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
				
				if(token.startsWith(CATEGORY_URL_PREFIX)){
					String categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/";
					RegExp p = RegExp.compile(categoryKeyRegexp);
					MatchResult m = p.exec(token);
					if(m == null){
						Window.alert("There is no '" + CATEGORY_URL_PREFIX + " in the URL provided");
						renderAllCategories();
						return;
					}
					String categoryKey = m.getGroup(1);
					
					Function<List<Photo>, Void> callback;
					
					if(token.contains(GOOD_URL_PREFIX)){
						//we need to render good
						RegExp goodRegexp = RegExp.compile(".+" + GOOD_URL_PREFIX + "(.+)/");
						MatchResult goodMatch = goodRegexp.exec(token);
						if(goodMatch == null){
							Window.alert("Wrong format for good in URL, should be '" + GOOD_URL_PREFIX + "'");
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
										PhotoFocusEvent event = new PhotoFocusEvent();
										event.setPhoto(photo);
										
										carousel.fireEvent(event);
									}
								}
								return null;
							}
						};
					} else {
						callback = new Function<List<Photo>, Void>(){
							@Override
							public Void apply(List<Photo> input) {
								//do nothing - we're just rendering a category and doing nothing afterwards
								return null;
							}
						};
					}
					//render goods and execute a callback which will be empty if '/good/' part is absent
					//and will pop single good menu otherwise
					renderGoods(categoryKey, callback);

				} else {
					Window.alert("URL must start with '" + CATEGORY_URL_PREFIX + "' token");
					renderAllCategories();
					return;
				}
			}
		});
		
		History.fireCurrentHistoryState();
	}

	private void outputControlsForCategories() {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus userStatus) {
				switch (userStatus) {
				case ADMIN:
					createLogoutUrl();

					setCategoriesAdminButtonHandlers();
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

	private void setCategoriesAdminButtonHandlers() {
		if (newButtonClickHandlerRegistration != null) {
			newButtonClickHandlerRegistration.removeHandler();
		}

		if (newButton != null) {
			newButtonClickHandlerRegistration = newButton
					.addClickHandler(categoriesNewButtonHandler);
		}
		if (clearButtonHandlerRegistration != null) {
			clearButtonHandlerRegistration.removeHandler();
		}
		if (clearButton != null) {
			clearButtonHandlerRegistration = clearButton
					.addClickHandler(categoriesClearButtonClickHandler);
		}
	}

	private void setGoodsAdminButtonsHandlers() {
		if (clearButtonHandlerRegistration != null) {
			clearButtonHandlerRegistration.removeHandler();
		}

		clearButtonHandlerRegistration = clearButton
				.addClickHandler(goodsClearButtonClickHandler);
		editGoodForm.setParentCategoryItemKeyStr(selectedCategoryKeyStr);

		if (newButtonClickHandlerRegistration != null) {
			newButtonClickHandlerRegistration.removeHandler();
		}

		newButtonClickHandlerRegistration = newButton
				.addClickHandler(goodsNewButtonHandler);
	}

	private void outputCategories(final Panel panel) {
		backButton.setVisible(false);
		dtoService.getCategoriesJson(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				createShopItemsFromJson(panel, categoryPanelCreation,
						editableCategoryPanelCreation, json);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
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
					setGoodsAdminButtonsHandlers();
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

	private <T extends ShopItem> void createShopItemsFromJson(
			final Panel panel, final Function<T, LayoutPanel> creation,
			final Function<T, LayoutPanel> editableCeation, final String json) {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus result) {
				final JsArray<T> arrayFromJson = ShopItem
						.getArrayFromJson(json);
				panel.clear();
				ArrayList<LayoutPanel> categories;
				switch (result) {
				case ADMIN:
					categories = createTiles(arrayFromJson, editableCeation);
					break;
				default:
					categories = createTiles(arrayFromJson, creation);
				}
				animateWidgetGridAppearenceAndAddToPanel(categories, panel);
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

	private <T extends ShopItem> ArrayList<LayoutPanel> createTiles(
			JsArray<T> arrayFromJson, Function<T, LayoutPanel> panelCreation) {
		ArrayList<LayoutPanel> result = new ArrayList<LayoutPanel>();
		for (int i = 0; i < arrayFromJson.length(); i++) {
			final T categoryJson = arrayFromJson.get(i);
			LayoutPanel itemPanel = panelCreation.apply(categoryJson);
			result.add(itemPanel);
		}

		return result;
	}

	/**
	 * Creates panel for category from json
	 */
	private LayoutPanel createCategoryPanel(
			final CategoryJavascriptObject categoryJson) {
		LayoutPanel itemPanel = createShopItemPanel(categoryJson);

		Label backgroundLabel = new Label(categoryJson.getBackgroundBlobKey());
		itemPanel.add(backgroundLabel);
		itemPanel.setWidgetTopHeight(backgroundLabel, 40, Style.Unit.PX, 20,
				Style.Unit.PX);
		ClickHandler categoryClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String keyStr = categoryJson.getKeyStr();
				// categories disappearance animation goes here
				setTransitionTimeouts(ANIMATION_SPEED, ANIMATION_DELAY,
						widgetMatrix, false);
				moveWidgetsOutOfTheScreen(Window.getClientWidth(),
						Window.getClientHeight(), widgetMatrix);
				final HashSet<Widget> allWidgets = new HashSet<Widget>();
				for (List<Widget> l : widgetMatrix) {
					allWidgets.addAll(l);
				}

				Timer t = new Timer() {
					@Override
					public void run() {
						if (allWidgetsOutsideTheScreen(allWidgets)) {
							// at this point, all categories are outside the
							// screen
							
							//Append /category/id/ to the URL
							History.newItem(getCategoryURLPart(keyStr));
							
							renderGoods(keyStr);
							
							cancel();
						}
					}
				};
				t.scheduleRepeating(300);

				
			}
		};

		itemPanel.addDomHandler(categoryClickHandler, ClickEvent.getType());
		return itemPanel;
	}

	/**
	 * Determines whether all widgets are completely outside the screen (beyond the left side or the bottom)  
	 */
	private boolean allWidgetsOutsideTheScreen(
			Collection<? extends Widget> widgets) {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		for (Widget w : widgets) {
			if (w.getElement().getAbsoluteLeft() < clientWidth
					&& w.getElement().getAbsoluteTop() < clientHeight) {
				return false;
			}
		}
		return true;
	}

	private void setTransition(Style style, String params) {
		style.setProperty("WebkitTransition", params);
		style.setProperty("MozTransition", params);
		style.setProperty("OTransition", params);
		style.setProperty("MsTransition", params);
		style.setProperty("Transition", params);
	}

	/**
	 * This is called when categories are appeared on the screen
	 * 
	 * @param widgets
	 *            - categories wigdets, are not yet added to panel
	 * @param panel
	 *            - future parent of Widgets
	 */
	private void animateWidgetGridAppearenceAndAddToPanel(
			List<? extends Widget> widgets, Panel panel) {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		int widgetCount = widgets.size();
		if (widgetCount == 0) {
			return;
		}

		// getting and caching a widget matrix. This will set left and top
		// properties of widgets as they were put on grid
		widgetMatrix = gitWidgetMatrix(widgets, clientWidth, ITEM_WIDTH,
				ITEM_HEIGHT, CATEGORIES_MARGIN, TOP_OFFSET);

		// at this point, widgets have their left and top values set to
		// destination values (put on grid). Persisting them in
		// destinationDimentions
		final HashMap<Widget, Dimention> destinationDimentions = new HashMap<Widget, Dimention>();
		for (Widget w : widgets) {
			Style style = w.getElement().getStyle();
			String top = style.getTop();
			String left = style.getLeft();
			destinationDimentions.put(w, new Dimention(getAmount(left),
					getAmount(top)));
		}

		addWidgetsToPanel(widgets, panel);

		// move widgets out the screen
		moveWidgetsOutOfTheScreen(clientWidth, clientHeight, widgetMatrix);
		// =============================
		// set animation delays on widgets

		setTransitionTimeouts(ANIMATION_SPEED, ANIMATION_DELAY, widgetMatrix,
				true);

		// ===================================
		// set destination dementions
		Timer timer = new Timer() {
			@Override
			public void run() {
				animate(destinationDimentions);
			}
		};
		// it seems that DOM needs some time to add object appropriately, so we
		// schedule animation to 1 second in the future
		timer.schedule(1000);
	}

	/**
	 * This cuts 'px' suffix from given string and returns int amount of the
	 * value
	 */
	private int getAmount(String style) {
		return Integer.parseInt(style.substring(0, style.length() - 2));
	}

	/**
	 * This takes an array of widgets and converts builds a grid (matrix) from
	 * them. It sets left and top properties widgets as if they were put on a
	 * grid
	 * 
	 * @param widgets
	 *            array of widgets, not added to any parent
	 * @param clientWidth
	 *            - screen width
	 * @param itemWidth
	 *            widget width - will be set
	 * @param itemHeight
	 *            widget height - will be set
	 * @param margin
	 *            margin top-bottom-left-right of widgets
	 * @param topOffset
	 *            offset from the top of the screen
	 */
	private ArrayList<ArrayList<Widget>> gitWidgetMatrix(
			List<? extends Widget> widgets, int clientWidth, int itemWidth,
			int itemHeight, int margin, int topOffset) {
		/**
		 * A list of list of widgets in appropriate 'matrix' order
		 */
		int currentX = margin;
		int currentY = topOffset + margin;
		ArrayList<ArrayList<Widget>> widgetMatrix = new ArrayList<ArrayList<Widget>>();
		// init first row
		ArrayList<Widget> currentRowList = new ArrayList<Widget>();
		currentRowList.add(widgets.get(0));
		widgetMatrix.add(currentRowList);

		int wCount = widgets.size();
		for (int i = 0; i < wCount; i++) {
			Widget widget = widgets.get(i);
			widget.setWidth(itemWidth + "px");
			widget.setHeight(itemHeight + "px");
			Style style = widget.getElement().getStyle();
			style.setMargin(margin, Unit.PX);

			// one of this will be overridden later
			style.setLeft(currentX, Unit.PX);
			style.setTop(currentY, Unit.PX);

			int nextX = currentX + margin * 2 + itemWidth;
			if (nextX + itemWidth + margin > clientWidth) {
				// next one will be new line
				currentX = margin;
				currentY += itemHeight + margin * 2;
				if (i + 1 < wCount) {
					// push next widget (if any) into the matrix, on a new row
					ArrayList<Widget> nextRow = new ArrayList<Widget>();
					nextRow.add(widgets.get(i + 1));
					widgetMatrix.add(nextRow);
				}
			} else {
				// line continues, will increment row
				currentX = nextX;
				if (i + 1 < wCount) {
					// push next widget into same row
					widgetMatrix.get(widgetMatrix.size() - 1).add(
							widgets.get(i + 1));
				}
			}

		}
		return widgetMatrix;
	}

	private void addWidgetsToPanel(List<? extends Widget> widgets, Panel panel) {
		for (Widget w : widgets) {
			panel.add(w);
		}
	}

	private void animate(HashMap<Widget, Dimention> destinationDimentions) {
		for (Map.Entry<Widget, Dimention> entry : destinationDimentions
				.entrySet()) {
			Style style = entry.getKey().getElement().getStyle();
			Dimention dimention = entry.getValue();
			style.setLeft(dimention.getX(), Unit.PX);
			style.setTop(dimention.getY(), Unit.PX);
		}
	}

	/**
	 * Sets CSS3 transition timeouts for grid of widgets
	 * 
	 * @param animationSpeed
	 * @param delay
	 *            amount of delay to execute animation with
	 * @param widgetMatrix
	 * @param in
	 *            true if widgets should appear, false - if they should
	 *            disappear
	 */
	private void setTransitionTimeouts(double animationSpeed, double delay,
			ArrayList<ArrayList<Widget>> widgetMatrix, boolean in) {
		int currentDiagonalIndex = 0;
		int diagonalLength = getDiagonalLength(widgetMatrix);

		// this is used only when in == false
		int longestSide = Math.max(widgetMatrix.size(), widgetMatrix.get(0)
				.size());
		double maxDelay = delay * longestSide;

		// do for every row/column of the diagonal
		while (currentDiagonalIndex < diagonalLength) {
			ArrayList<Widget> currentRow = widgetMatrix
					.get(currentDiagonalIndex);
			Style style = currentRow.get(currentDiagonalIndex).getElement()
					.getStyle();
			double diagonalCellDelay;
			if (in) {
				diagonalCellDelay = 0;
			} else {
				diagonalCellDelay = maxDelay;
			}
			String diagonalTransitionParams = getTransitionParams(
					animationSpeed, diagonalCellDelay);
			setTransition(style, diagonalTransitionParams);

			int derivation = 1;
			double currentDelay;
			if (in) {
				currentDelay = delay;
			} else {
				currentDelay = maxDelay - delay;
			}
			// do for every row and column simultaneously. If one finishes, the
			// other will still be executed.
			// Finishes when both row and column are finished
			while (true) {
				boolean hitMatrix = false;
				if (currentRow.size() > currentDiagonalIndex + derivation) {
					Style derivedStyle = currentRow
							.get(currentDiagonalIndex + derivation)
							.getElement().getStyle();
					setTransition(derivedStyle,
							getTransitionParams(animationSpeed, currentDelay));
					hitMatrix = true;
				}
				if (widgetMatrix.size() > currentDiagonalIndex + derivation) {
					ArrayList<Widget> derivedRow = widgetMatrix
							.get(currentDiagonalIndex + derivation);
					if (derivedRow.size() > currentDiagonalIndex) {
						Style derivedStyle = derivedRow
								.get(currentDiagonalIndex).getElement()
								.getStyle();
						setTransition(
								derivedStyle,
								getTransitionParams(animationSpeed,
										currentDelay));
						hitMatrix = true;
					}
				}
				if (hitMatrix) {
					// there are still cells in row/column
					derivation++;
					if (in) {
						currentDelay += delay;
					} else {
						currentDelay -= delay;
					}
				} else {
					// there are no cells in row/column. Proceed to the next
					// diagonal cell and its row/column
					break;
				}
			}
			currentDiagonalIndex++;
		}
	}

	private int getDiagonalLength(ArrayList<ArrayList<Widget>> widgetMatrix) {
		int diagonalLength = 0;
		while (true) {
			if (widgetMatrix.size() > diagonalLength
					&& widgetMatrix.get(diagonalLength).size() > diagonalLength) {
				diagonalLength++;
			} else {
				break;
			}
		}
		return diagonalLength;
	}

	private void moveWidgetsOutOfTheScreen(int clientWidth, int clientHeight,
			ArrayList<ArrayList<Widget>> widgetMatrix) {
		int widgetsToMoveToBottom = 1;
		for (int r = 0; r < widgetMatrix.size(); r++) {
			ArrayList<Widget> rows = widgetMatrix.get(r);
			for (int i = 0; i < rows.size(); i++) {
				Style style = rows.get(i).getElement().getStyle();
				if (i < widgetsToMoveToBottom) {
					style.setTop(clientHeight, Unit.PX);
				} else {
					style.setLeft(clientWidth, Unit.PX);
				}
			}
			if (r + 1 > 1 && (r + 1) % 2 == 0) {
				widgetsToMoveToBottom += 2;
			}
		}
	}

	private String getTransitionParams(double animationSpeed, double delay) {
		return "all " + animationSpeed + "s ease-in-out " + delay + "s";
	}

	/**
	 * @param callbacks list of callbacks to call when carousel photos are retrieved
	 */
	private void renderGoods(String categoryKey, Function<List<Photo>, Void>... callbacks) {
		this.selectedCategoryKeyStr = categoryKey;
		outputGoodsForCategory(categoryKey, outputPanel, callbacks);
		outputControlsForGoods();
	}

	private void renderAllCategories() {
		setCategoriesAdminButtonHandlers();
		outputCategories(outputPanel);
		outputControlsForCategories();
	}

	public static String getCategoryURLPart(String categoryKeyStr) {
		return CATEGORY_URL_PREFIX + categoryKeyStr + "/";
	}
	
	public static String getGoodURLPart(String goodKeyStr){
		return GOOD_URL_PREFIX + goodKeyStr + "/";
	}
	
	private static class Dimention {
		private int x;
		private int y;

		protected Dimention(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Dimention [x=").append(x).append(", y=").append(y)
					.append("]");
			return builder.toString();
		}

	}
}
