package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.service.DtoService;
import com.denisk.appengine.nl.client.service.DtoServiceAsync;
import com.denisk.appengine.nl.client.ui.parts.EditCategoryForm;
import com.denisk.appengine.nl.client.util.CategoriesAnimator;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class CategoriesView extends AbstractItemsView{

	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);
	private CategoriesAnimator categoriesAnimator = new CategoriesAnimator();

	private EditCategoryForm editCategoryForm = new EditCategoryForm();

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

	protected <T extends ShopItem> void createShopItemsFromJson(
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
		
		//TODO majority of this method should go into CategoriesAnimator
		
		ClickHandler categoryClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
							History.newItem(getCategoryURLPart(keyStr), false);
							
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

	private void outputCategories(final Panel panel) {
		
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


	private void renderAllCategories() {
		setCategoriesAdminButtonHandlers();
		outputCategories(outputPanel);
		outputControlsForCategories();
	}


}
