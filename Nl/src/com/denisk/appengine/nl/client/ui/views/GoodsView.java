package com.denisk.appengine.nl.client.ui.views;

import java.util.ArrayList;
import java.util.List;

import com.denisk.appengine.nl.client.overlay.GoodJavascriptObject;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Carousel;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.ui.parts.EditGoodForm;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

public class GoodsView extends AbstractItemsView {

	private Carousel carousel = new Carousel();
	private EditGoodForm editGoodForm = new EditGoodForm();

	private ClickHandler goodsClearButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if(Window.confirm("Are you sure you want to delete all goods of category with key=" + parent.getSelectedCategoryKeyStr())){
				parent.showBusyIndicator();
				if (parent.getSelectedCategoryKeyStr() != null) {
					parent.getDtoService().clearGoodsForCategory(
							parent.getSelectedCategoryKeyStr(),
							new AsyncCallback<Void>() {
								@Override
								public void onSuccess(
										Void result) {
									outputGoodsForCategory(null);
									parent.updateLabel();
								}
	
								@Override
								public void onFailure(
										Throwable caught) {
									Window.alert("Can't delete all goods for category " + parent.getSelectedCategoryKeyStr());
								}
							});
				}
			}
		}
	};

	private ClickHandler goodsNewButtonHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			editGoodForm.showForCreation();
		}
	};
	// ==============================================

	private Function<GoodJavascriptObject, Void> goodDeletion = new Function<GoodJavascriptObject, Void>() {
		@Override
		public Void apply(GoodJavascriptObject input) {
			parent.getDtoService().deleteGood(input.getKeyStr(), input.getImageBlobKey(),
					getRedrawingCallback(redrawGoodsCallback));
			return null;
		}
	};

	private Function<Void, Void> redrawGoodsCallback = new Function<Void, Void>() {
		@Override
		public Void apply(Void input) {
			editGoodForm.hide();
			parent.updateLabel();
			outputGoodsForCategory(null);

			return null;
		}
	};

	public GoodsView(Nl parent) {
		super(parent);
		editGoodForm.setRedrawAfterItemCreatedCallback(redrawGoodsCallback);
		
		final Button backButton = parent.getBackButton();
		final Nl p = parent;
		backButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				p.showBusyIndicator();
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
						p.switchToCategoriesView();
						p.renderView(null);
					}
				};
				t1.schedule(2000 + 500 + 100/* just in case */);
			}
		});

	}

	/**
	 * This method fills carousel with good items and adds carousel to
	 * outputPanel
	 */
	private void outputGoodsForCategory(final Function<List<Photo>, ?> callback) {
		final Panel panel = parent.getOutputPanel();
		final String categoryKeyStr = parent.getSelectedCategoryKeyStr();
		
		parent.getDtoService().getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				// move carousel far down
				carousel.getElement().getStyle()
						.setTop(Window.getClientHeight(), Unit.PX);

				panel.clear();
				final JsArray<GoodJavascriptObject> goods = GoodJavascriptObject
						.getArrayFromJson(json);
				if (goods.length() > 0) {
					Image leftArrow = new Image("/images/arrow-left.png");
					Image rightArrow = new Image("/images/arrow-right.png");
										
					leftArrow.addStyleName("leftArrow");
					rightArrow.addStyleName("rightArrow");
					
					panel.add(leftArrow);
					
					FlowPanel carouselContainer = new FlowPanel();
					carouselContainer.addStyleName("carouselContainer");
					carouselContainer.add(carousel);
					panel.add(carouselContainer);
					
					panel.add(rightArrow);
					
					final ArrayList<Photo> photos = new ArrayList<Photo>(goods
							.length());
					for (int i = 0; i < goods.length(); i++) {
						GoodJavascriptObject good = goods.get(i);
						String imageUrl = getImageUrl(good.getImageBlobKey(), "600", "600");
						Photo photo = new Photo(imageUrl, good.getName(), good
								.getDescription(), good.getKeyStr());
						photos.add(photo);
					}
					parent.getDtoService().isAdmin(new AsyncCallback<UserStatus>() {

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
					
					if(callback != null){
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
							
							parent.hideBusyIndicator();
						}
					};
					t1.schedule(2000 + 500);
				} else {
					parent.hideBusyIndicator();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("There is no category with identifier " + categoryKeyStr);
			}
		});
	}

	public void selectPhoto(Photo photo){
		PhotoClickEvent event = new PhotoClickEvent();
		event.setPhoto(photo);
		event.setShouldChangeURL(false);
		carousel.fireEvent(event);
	}

	@Override
	public void render(Panel panel, Function callback) {
		parent.showBusyIndicator();
		outputGoodsForCategory(callback);
	}

	@Override
	public ClickHandler getNewItemHandler() {
		return goodsNewButtonHandler;
	}

	@Override
	public ClickHandler getClearAllHandler() {
		return goodsClearButtonClickHandler;
	}

	public EditGoodForm getEditGoodForm() {
		return editGoodForm;
	}

}
