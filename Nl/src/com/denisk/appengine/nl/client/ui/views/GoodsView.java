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
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

public class GoodsView extends AbstractItemsView {

	private Carousel carousel = new Carousel();
	private FlowPanel carouselContainer = new FlowPanel();
	private Image leftArrow = new Image("/images/arrow-left.png");
	private Image rightArrow = new Image("/images/arrow-right.png");
	
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
	
	private ClickHandler backButtonHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			Timer switchTimer = new Timer() {
				@Override
				public void run() {
					parent.getOutputPanel().removeStyleName("carouselDownAnimated");
					parent.getOutputPanel().getElement().getStyle().clearTop();
					//this is extra clear
					parent.getOutputPanel().clear();
					
					parent.switchToCategoriesView();
					parent.renderView(null);
				}
			};

			if (carousel.getPhotos().size() > 0) {
				parent.showBusyIndicator();
				// set transitioned style to the carousel
				parent.getOutputPanel().addStyleName("carouselDownAnimated");
				Timer modeDownTimer = new Timer() {

					@Override
					public void run() {
						// move carousel far down
						// this will last 2 seconds
						parent.getOutputPanel().getElement().getStyle()
								.setTop(Window.getClientHeight(), Unit.PX);
					}
				};
				modeDownTimer.schedule(500);
				switchTimer.schedule(2000 + 500 + 100/* just in case */);
			} else {
				switchTimer.run();
			}
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

		
		carouselContainer.addStyleName("carouselContainer");
							
		leftArrow.addStyleName("leftArrow");
		rightArrow.addStyleName("rightArrow");

		addArrowHandlers();
		
		carouselContainer.add(carousel);
		
		editGoodForm.setRedrawAfterItemCreatedCallback(redrawGoodsCallback);
		
		final Button backButton = parent.getBackButton();
		final Nl p = parent;
		backButton.addClickHandler(backButtonHandler);

	}

	private void addArrowHandlers() {
		leftArrow.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				carousel.prev();
			}
		});
		rightArrow.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				carousel.next();
			}
		});
		leftArrow.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				leftArrow.setUrl("/images/arrow-left-mouseover.png");
			}
		});
		rightArrow.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				rightArrow.setUrl("/images/arrow-right-mouseover.png");
			}
		});
		leftArrow.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				leftArrow.setUrl("/images/arrow-left.png");
			}
		});
		rightArrow.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				rightArrow.setUrl("/images/arrow-right.png");
			}
		});
		leftArrow.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				leftArrow.setUrl("/images/arrow-left-press.png");
				
			}
		});
		rightArrow.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				rightArrow.setUrl("/images/arrow-right-press.png");
			}
		});

		leftArrow.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				leftArrow.setUrl("/images/arrow-left.png");
			}
		});
		rightArrow.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				rightArrow.setUrl("/images/arrow-right.png");
			}
		});
	}

	/**
	 * This method fills carousel with good items and adds carousel to
	 * outputPanel
	 */
	private void outputGoodsForCategory(final Function<List<Photo>, ?> callback) {
		final String categoryKeyStr = parent.getSelectedCategoryKeyStr();
		//Append /category/id/ to the URL
		History.newItem(Nl.getCategoryURLPart(categoryKeyStr), false);

		parent.getOutputPanel().clear();
		
		parent.getDtoService().getGoodsJson(categoryKeyStr, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				// move carousel far down
				parent.getOutputPanel().getElement().getStyle()
						.setTop(Window.getClientHeight(), Unit.PX);

				//panel.clear();
				final JsArray<GoodJavascriptObject> goods = GoodJavascriptObject
						.getArrayFromJson(json);
				if (goods.length() > 0) {
					final Panel panel = parent.getOutputPanel();
					
					panel.add(leftArrow);
					panel.add(carouselContainer);
					panel.add(rightArrow);

					final ArrayList<Photo> photos = new ArrayList<Photo>(goods
							.length());
					for (int i = 0; i < goods.length(); i++) {
						GoodJavascriptObject good = goods.get(i);
						String imageUrl = getImageUrl(good.getImageBlobKey(), 600, 600);
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
							parent.getOutputPanel().removeStyleName("carouselDownAnimated");
							parent.getOutputPanel().addStyleName("carouselAnimated");
						}
					};
					t.schedule(500);
					// remove 'top' style after the carousel has arrived
					Timer t1 = new Timer() {

						@Override
						public void run() {
							// remove 'top' property from the carousel
							parent.getOutputPanel().getElement().getStyle()
									.setTop(100, Unit.PX);
							parent.getOutputPanel().removeStyleName("carouselAnimated");
							
							parent.hideBusyIndicator();
						}
					};
					t1.schedule(2000 + 500);
				} else {
					carousel.getPhotos().clear();
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
		//event.setShouldChangeURL(false);
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
