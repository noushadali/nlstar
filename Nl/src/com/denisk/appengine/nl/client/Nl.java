package com.denisk.appengine.nl.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
	
	//state fields
	private String selectedCategoryKeyStr;
	private ArrayList<ArrayList<Widget>> widgetMatrix;
	

	private ClickHandler categoriesClearButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if(Window.confirm("Are you sure you want to delete all categories and items in these categories?")){
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
		edit.addClickHandler(getEditClickHandler(item, editForm));
		panel.add(edit);

		panel.setWidgetRightWidth(edit, 60, Style.Unit.PX, 30, Style.Unit.PX);
		panel.setWidgetTopHeight(edit, 10, Style.Unit.PX, 20, Style.Unit.PX);
	}

	/**
	 * Deletes an item and redraws the panel
	 */
	private <T extends ShopItem> void buildDeleteButton(final T item, LayoutPanel panel, final Function<T, Void> deletion){
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
				if(Window.confirm("Are you sure you want to delete " + item.getName() + "?")){
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

	/**
	 * This method creates fills carousel with good items and adds carousel to outputPanel
	 */
	private void outputGoodsForCategory(String categoryKeyStr,
			final FlowPanel panel) {
		
		dtoService.getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				panel.clear();
				final JsArray<GoodJavascriptObject> goods = GoodJavascriptObject.getArrayFromJson(json);
				if(goods.length()> 0) {
					panel.add(carousel);
					final ArrayList<Photo> photos = new ArrayList<Photo>(goods.length());
					for(int i = 0; i < goods.length(); i++){
						GoodJavascriptObject good = goods.get(i);
						String imageUrl = getImageUrl(good, "600", "600");
						Photo photo = new Photo(imageUrl, good.getName(), good.getDescription());
						photos.add(photo);
					}
					dtoService.isAdmin(new AsyncCallback<UserStatus>() {
						
						@Override
						public void onSuccess(UserStatus result) {
							if(UserStatus.ADMIN == result){
								for(int i = 0; i < photos.size(); i++){
									Photo photo = photos.get(i);
									GoodJavascriptObject good = goods.get(i);

									photo.setEditClickHandler(getEditClickHandler(good, editGoodForm));
									photo.setDeleteClickHandler(getDeleteClickHandler(good, goodDeletion));
								}
							}
						}
						
						@Override
						public void onFailure(Throwable caught) {
							Window.alert("Can't determine credentials for user");
						}
					});
					carousel.setPhotos(photos);
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

	private <T extends ShopItem> ArrayList<LayoutPanel> createTiles(JsArray<T> arrayFromJson, Function<T, LayoutPanel> panelCreation) {
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
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				backButton.setVisible(true);
				final String keyStr = categoryJson.getKeyStr();
				//animate categories here
				 Timer t = new Timer() {
					@Override
					public void run() {
						if(true) {
							outputGoodsForCategory(keyStr, outputPanel);
							outputControlsForGoods();
							
							this.cancel();
						}
					}
				};
				t.schedule(100);
				selectedCategoryKeyStr = keyStr;
			}
		};

		itemPanel.addDomHandler(clickHandler, ClickEvent.getType());
		return itemPanel;
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
	 * @param widgets - categories wigdets, are not yet added to panel
	 * @param panel - future parent of Widgets
	 */
	private void animateWidgetGridAppearenceAndAddToPanel(List<? extends Widget> widgets, Panel panel) {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		int itemWidth = 300;
		int itemHeight = 200;
		//We use the same amount of margin for top, bottom, left and right values
		int margin = 10;
		//todo calculate topOffset based on top panel height
		int topOffset = 100;
		double delay = 0.3;//seconds between waves of items
		double animationSpeed = 1;//seconds for each wave
		
		int widgetCount = widgets.size();
		if(widgetCount == 0){
			return;
		}
		
		//getting widget matrix. This will set left and top properties of widgets as they were put on grid
		widgetMatrix = gitWidgetMatrix(widgets, clientWidth, itemWidth, itemHeight, margin, topOffset);
		
		//at this point, widgets have their left and top values set to destination values (put on grid). Persisting them in destinationDimentions
		final HashMap<Widget, Dimention> destinationDimentions = new HashMap<Widget, Dimention>();
		for(Widget w: widgets){
			Style style = w.getElement().getStyle();
			String top = style.getTop();
			String left = style.getLeft();
			destinationDimentions.put(w, new Dimention(Integer.parseInt(left.substring(0, left.length() - 2)), Integer.parseInt(top.substring(0, top.length() - 2))));
		}
		
		addWidgetsToPanel(widgets, panel);

		//move widgets out the screen
		moveWidgetsOutOfTheScreen(clientWidth, clientHeight, widgetMatrix);
		//=============================
		//set animation delays on widgets
		int diagonalLength = getDiagonalLength(widgetMatrix);

		setTransitionTimeouts(animationSpeed, delay, widgetMatrix, diagonalLength);
		
		//===================================
		//set destination dementions
		Timer timer = new Timer() {
			@Override
			public void run() {
				animate(destinationDimentions);
			}
		};
		//it seems that DOM needs some time to add object appropriately, so we schedule animation to 1 second in the future
		timer.schedule(1000);
	}

	private ArrayList<ArrayList<Widget>> gitWidgetMatrix(
			List<? extends Widget> widgets, int clientWidth, int itemWidth,
			int itemHeight, int margin, int topOffset) {
		/**
		 * A list of list of widgets in appropriate 'matrix' order
		 */
		int currentX = margin;
		int currentY = topOffset + margin;
		ArrayList<ArrayList<Widget>> widgetMatrix = new ArrayList<ArrayList<Widget>>();
		//init first row
		ArrayList<Widget> currentRowList = new ArrayList<Widget>();
		currentRowList.add(widgets.get(0));
		widgetMatrix.add(currentRowList);
		
		int wCount = widgets.size();
		for(int i = 0; i < wCount; i++){
			Widget widget = widgets.get(i);
			widget.setWidth(itemWidth + "px");
			widget.setHeight(itemHeight + "px");
			Style style = widget.getElement().getStyle();
			style.setMargin(margin, Unit.PX);

			//one of this will be overridden later
			style.setLeft(currentX, Unit.PX);
			style.setTop(currentY, Unit.PX);
			
			int nextX = currentX + margin*2 + itemWidth;
			if(nextX + itemWidth + margin > clientWidth){
				//next one will be new line
				currentX = margin;
				currentY += itemHeight + margin*2;
				if(i+1 < wCount){
					//push next widget (if any) into the matrix, on a new row
					ArrayList<Widget> nextRow = new ArrayList<Widget>();
					nextRow.add(widgets.get(i+1));
					widgetMatrix.add(nextRow);
				}
			} else {
				//line continues, will increment row
				currentX = nextX;
				if(i+1 < wCount){
					//push next widget into same row
					widgetMatrix.get(widgetMatrix.size() - 1).add(widgets.get(i + 1));
				}
			}
			
		}
		return widgetMatrix;
	}

	private void addWidgetsToPanel(List<? extends Widget> widgets, Panel panel) {
		for(Widget w: widgets){
			panel.add(w);
		}
	}

	private void animate(HashMap<Widget, Dimention> destinationDimentions) {
		for(Map.Entry<Widget, Dimention> entry: destinationDimentions.entrySet()){
			Style style = entry.getKey().getElement().getStyle();
			Dimention dimention = entry.getValue();
			style.setLeft(dimention.getX(), Unit.PX);
			style.setTop(dimention.getY(), Unit.PX);
		}
	}

	private void setTransitionTimeouts(double animationSpeed, double delay,
			ArrayList<ArrayList<Widget>> widgetMatrix, int diagonalLength) {
		int currentDiagonalIndex = 0;
		while(currentDiagonalIndex < diagonalLength){
			ArrayList<Widget> currentRow = widgetMatrix.get(currentDiagonalIndex);
			Style style = currentRow.get(currentDiagonalIndex).getElement().getStyle();
			String diagonalTransitionParams = getTransitionParams(animationSpeed, 0);
			setTransition(style, diagonalTransitionParams);

			int derivation = 1;
			double currentDelay = delay;
			while(true){
				boolean hitMatrix = false;
				if(currentRow.size() > currentDiagonalIndex + derivation){
					Style derivedStyle = currentRow.get(currentDiagonalIndex + derivation).getElement().getStyle();
					setTransition(derivedStyle, getTransitionParams(animationSpeed, currentDelay));
					hitMatrix = true;
				}
				if(widgetMatrix.size() > currentDiagonalIndex + derivation){
					ArrayList<Widget> derivedRow = widgetMatrix.get(currentDiagonalIndex + derivation);
					if(derivedRow.size() > currentDiagonalIndex){
						Style derivedStyle = derivedRow.get(currentDiagonalIndex).getElement().getStyle();
						setTransition(derivedStyle, getTransitionParams(animationSpeed, currentDelay));
						hitMatrix = true;
					}
				}
				if(hitMatrix){
					derivation++;
					currentDelay += delay;
				} else {
					break;
				}
			}
			currentDiagonalIndex ++;
		}
	}

	private int getDiagonalLength(ArrayList<ArrayList<Widget>> widgetMatrix) {
		int diagonalLength = 0;
		while(true){
			if(widgetMatrix.size() > diagonalLength && widgetMatrix.get(diagonalLength).size() > diagonalLength){
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
		for(int r = 0; r < widgetMatrix.size(); r++){
			ArrayList<Widget> rows = widgetMatrix.get(r);
			for(int i = 0; i < rows.size(); i++){
				Style style = rows.get(i).getElement().getStyle();
				if(i < widgetsToMoveToBottom){
					style.setTop(clientHeight, Unit.PX);
				} else {
					style.setLeft(clientWidth, Unit.PX);
				}
			}
			if(r+1 > 1 && (r+1)%2 == 0){
				widgetsToMoveToBottom += 2;
			}
		}
	}

	private String getTransitionParams(double animationSpeed, double delay) {
		return "all " + animationSpeed + "s ease-in-out " + delay + "s";
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
