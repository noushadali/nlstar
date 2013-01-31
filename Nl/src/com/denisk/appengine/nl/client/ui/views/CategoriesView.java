package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;
import java.util.HashMap;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.parts.EditCategoryForm;
import com.denisk.appengine.nl.client.util.CategoriesAnimator;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class CategoriesView extends AbstractItemsView{

	private CategoriesAnimator categoriesAnimator;

	private EditCategoryForm editCategoryForm = new EditCategoryForm();
	
	//true when category panel is clicked. Reseted on render
	private boolean clicked = false;
	
	private Function<CategoryJavascriptObject, LayoutPanel> categoryPanelCreation = new Function<CategoryJavascriptObject, LayoutPanel>() {
		@Override
		public LayoutPanel apply(CategoryJavascriptObject input) {
			return createCategoryPanel(input);
		}
	};

	private Function<CategoryJavascriptObject, Void> categoryDeletion = new Function<CategoryJavascriptObject, Void>() {
		@Override
		public Void apply(CategoryJavascriptObject input) {
			parent.getDtoService().deleteCategory(input.getKeyStr(),
					input.getImageBlobKey(), input.getBackgroundBlobKey(),
					getRedrawingCallback(redrawCategoriesCallback));
			return null;
		}
	};
	private Function<Void, Void> redrawCategoriesCallback = new Function<Void, Void>() {
		@Override
		public Void apply(Void input) {
			editCategoryForm.hide();
			parent.updateLabel();
			parent.getBackButton().setVisible(false);
			render(parent.getOutputPanel(), null);

			return null;
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

	private ClickHandler categoriesNewButtonHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			editCategoryForm.showForCreation();
		}
	};
	private ClickHandler categoriesClearButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (Window
					.confirm("Are you sure you want to delete all categories and items in these categories?")) {
				parent.getDtoService().clearData(new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						parent.updateLabel();
						render(parent.getOutputPanel(), null);
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
		}
	};

	
	protected CategoriesView(Nl parent) {
		super(parent);
		categoriesAnimator = new CategoriesAnimator(parent);
		editCategoryForm.setRedrawAfterItemCreatedCallback(redrawCategoriesCallback);
	}

	protected <T extends ShopItem> void createShopItemsFromJson(
			final Panel panel, final Function<T, LayoutPanel> creation,
			final Function<T, LayoutPanel> editableCeation, final String json) {
		parent.getDtoService().isAdmin(new AsyncCallback<UserStatus>() {
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
				categoriesAnimator.animateWidgetGridAppearenceAndAddToPanel(categories, panel);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	/**
	 * Creates panel for category from json
	 */
	private LayoutPanel createCategoryPanel(
			final CategoryJavascriptObject categoryJson) {
		final LayoutPanel itemPanel = createShopItemPanel(categoryJson);
		final String keyStr = categoryJson.getKeyStr();

		ClickHandler categoryClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CategoriesView.this.clicked = true;
				
				parent.showBusyIndicator();
				
				// categories disappearance animation goes here
				categoriesAnimator.setTransitionTimeouts(false);
				categoriesAnimator.moveWidgetsOutOfTheScreen();

				Timer t = new Timer() {
					@Override
					public void run() {
						if (categoriesAnimator.allWidgetsOutsideTheScreen()) {
							// at this point, all categories are outside the
							// screen
							
							//Append /category/id/ to the URL
							History.newItem(Nl.getCategoryURLPart(keyStr), false);
							
							parent.setSelectedCategoryKeyStr(keyStr);
							parent.switchToGoodsView();
							parent.renderView(null);
							
							cancel();
						}
					}
				};
				t.scheduleRepeating(300);
			}
		};

		itemPanel.addDomHandler(categoryClickHandler, ClickEvent.getType());
		
		//init background image
		final String backgroundBlobKey = categoryJson.getBackgroundBlobKey();
		if (backgroundBlobKey != null && !backgroundBlobKey.isEmpty()) {
			final Image background = new Image(AbstractItemsView.getImageUrl(
					backgroundBlobKey, "-1", "-1"));
			background.getElement().getStyle().setOpacity(0);
			background.addStyleName("background");
			
			RootPanel.get("backgroundsContainer").add(background);
			
			//background handlers
			itemPanel.addDomHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					background.getElement().getStyle().setOpacity(1);
				}

			}, MouseOverEvent.getType());

			itemPanel.addDomHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					if (! clicked) {
						background.getElement().getStyle().setOpacity(0);
					}
				}

			}, MouseOutEvent.getType());
		}
		//border style handlers
		itemPanel.addDomHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				itemPanel.getWidget(0).addStyleName("pulsing");
			}

		}, MouseOverEvent.getType());

		itemPanel.addDomHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				itemPanel.getWidget(0).removeStyleName("pulsing");
			}

		}, MouseOutEvent.getType());

		return itemPanel;
	}

	private int getIntFromPx(String str) {
		return Integer.parseInt(str.substring(0, str.length() - 2));
	}

	private void clearBackgrounds(){
		RootPanel.get("backgroundsContainer").clear();
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
	

	@Override
	public void render(final Panel panel, Function callback) {
		this.clicked = false; 
		
		parent.showBusyIndicator();
		
		clearBackgrounds();
		
		parent.getDtoService().getCategoriesJson(new AsyncCallback<String>() {
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


	@Override
	public ClickHandler getNewItemHandler() {
		return this.categoriesNewButtonHandler;
	}

	@Override
	public ClickHandler getClearAllHandler() {
		return this.categoriesClearButtonClickHandler;
	}
}
