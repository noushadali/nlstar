package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.parts.EditCategoryForm;
import com.denisk.appengine.nl.client.util.CategoriesAnimator;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;

public class CategoriesView extends AbstractItemsView{

	private CategoriesAnimator categoriesAnimator;

	private EditCategoryForm editCategoryForm = new EditCategoryForm();

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
		LayoutPanel itemPanel = createShopItemPanel(categoryJson);
		Label backgroundLabel = new Label(categoryJson.getBackgroundBlobKey());
		final String keyStr = categoryJson.getKeyStr();

		itemPanel.add(backgroundLabel);
		itemPanel.setWidgetTopHeight(backgroundLabel, 40, Style.Unit.PX, 20,
				Style.Unit.PX);
		
		ClickHandler categoryClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
		return itemPanel;
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
		parent.showBusyIndicator();
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
		return categoriesNewButtonHandler;
	}

	@Override
	public ClickHandler getClearAllHandler() {
		return categoriesClearButtonClickHandler;
	}


}
