package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;

import com.denisk.appengine.nl.client.overlay.CategoryJavascriptObject;
import com.denisk.appengine.nl.client.overlay.ShopItem;
import com.denisk.appengine.nl.client.ui.parts.EditCategoryForm;
import com.denisk.appengine.nl.client.ui.parts.ProductsList;
import com.denisk.appengine.nl.client.util.CategoriesAnimator;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class CategoriesView extends AbstractItemsView {

	private CategoriesAnimator categoriesAnimator;

	private EditCategoryForm editCategoryForm = new EditCategoryForm();
	
	//true when category panel is clicked. Reseted on render
	private boolean clicked = false;
	
	private Function<CategoryJavascriptObject, Panel> categoryPanelCreation = new Function<CategoryJavascriptObject, Panel>() {
		@Override
		public Panel apply(CategoryJavascriptObject input) {
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
			System.out.println("redrawCategoriesCallback...");
			render(parent.getOutputPanel(), null);

			return null;
		}
	};
	private Function<CategoryJavascriptObject, Panel> editableCategoryPanelCreation = new Function<CategoryJavascriptObject, Panel>() {
		@Override
		public Panel apply(final CategoryJavascriptObject category) {
			Panel panel = categoryPanelCreation.apply(category);
			
			FlowPanel editContainer = new FlowPanel();
			editContainer.addStyleName("editContainer");
			panel.add(editContainer);
			
			buildEditButton(category, editContainer, editCategoryForm);
			buildDeleteButton(category, editContainer, categoryDeletion);
			
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

	protected Panel createShopItemPanel(final ShopItem itemJson) {
		HeadingElement name = Document.get().createHElement(3);
		name.setInnerText(itemJson.getName());
		name.addClassName("categoryName");
		
		HTML description = new HTML(itemJson.getDescription());
		description.addStyleName("categoryDescription");

		Image image = new Image(
				getImageUrl(itemJson.getImageBlobKey(), THUMB_WIDTH, THUMB_HEIGHT));
		image.addStyleName("categoryImage");

		FlowPanel result = new FlowPanel();
		result.addStyleName("category");

		result.getElement().appendChild(name);
		FlowPanel imageWrapper = new FlowPanel();
		imageWrapper.addStyleName("categoryImageWrapper");
		imageWrapper.add(image);
		
		result.add(imageWrapper);
		result.add(description);

		return result;
	}

	protected <T extends ShopItem> void createShopItemsFromJson(
			final Panel panel, final Function<T, ? extends Panel> creation,
			final Function<T, ? extends Panel> editableCeation, final String json) {
		parent.getDtoService().isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus result) {
				final JsArray<T> arrayFromJson = ShopItem
						.getArrayFromJson(json);
				panel.clear();
				
				addCategoriesList(panel, arrayFromJson);
				
				ArrayList<Panel> categories;
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

	private <T extends ShopItem> void addCategoriesList(
			final Panel panel, final JsArray<T> arrayFromJson) {
		Function<ShopItem, Void> callback = new Function<ShopItem, Void>() {
			
			@Override
			public Void apply(ShopItem input) {
				getCategoryClickHandler(input.getKeyStr()).onClick(null);
				return null;
			}
		};
		ProductsList list = new ProductsList(callback);
		ArrayList<T> items = new ArrayList<T>();
		for(int i = 0; i < arrayFromJson.length(); i++){
			items.add(arrayFromJson.get(i));
		}
		list.setItems(items);
		panel.add(list);
	}

	/**
	 * Creates panel for category from json
	 */
	private Panel createCategoryPanel(
			final CategoryJavascriptObject categoryJson) {
		final String keyStr = categoryJson.getKeyStr();

		ClickHandler categoryClickHandler = getCategoryClickHandler(keyStr);

		Panel itemPanel = createShopItemPanel(categoryJson);
		itemPanel.addDomHandler(categoryClickHandler, ClickEvent.getType());
		
		//init background image
		final String backgroundBlobKey = categoryJson.getBackgroundBlobKey();

		initBackgroundImage(itemPanel, backgroundBlobKey);
		
		initBorders(itemPanel);

		return itemPanel;
	}

	private void initBorders(Panel itemPanel) {
		final FlowPanel border = new FlowPanel();
		border.addStyleName("categoryBorder");
		itemPanel.add(border);
		//border style handlers
		itemPanel.addDomHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				border.addStyleName("pulsing");
			}

		}, MouseOverEvent.getType());

		itemPanel.addDomHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				border.removeStyleName("pulsing");
			}

		}, MouseOutEvent.getType());
	}

	private void initBackgroundImage(Panel itemPanel,
			final String backgroundBlobKey) {
		if (backgroundBlobKey != null && !backgroundBlobKey.isEmpty()) {
			final Image background = createAndSetupBackground(backgroundBlobKey);
			
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
	}

	private ClickHandler getCategoryClickHandler(final String keyStr) {
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
		return categoryClickHandler;
	}

	public Image createAndSetupBackground(final String backgroundBlobKey) {
		final Image background = new Image(AbstractItemsView.getImageUrl(
				backgroundBlobKey, -1, -1));
		background.getElement().getStyle().setOpacity(0);
		background.addStyleName("background");
		
		RootPanel.get("backgroundsContainer").add(background);
		return background;
	}

	private <T extends ShopItem> ArrayList<Panel> createTiles(
			JsArray<T> arrayFromJson, Function<T, ? extends Panel> panelCreation) {
		ArrayList<Panel> result = new ArrayList<Panel>();
		for (int i = 0; i < arrayFromJson.length(); i++) {
			final T categoryJson = arrayFromJson.get(i);
			Panel itemPanel = panelCreation.apply(categoryJson);
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
