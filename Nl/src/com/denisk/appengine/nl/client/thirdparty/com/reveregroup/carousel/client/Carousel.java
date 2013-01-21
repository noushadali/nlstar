package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.List;

import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoFocusHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoToFrontEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoToFrontHandler;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoUnfocusHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class Carousel extends Composite {
	private List<Photo> photos;
	private CarouselImage[] images;

	// Panels and label for the UI
	private DockPanel carouselDock;

	private AbsolutePanel imagePanel;

	private Label caption;
	
	private Button nextButton;
	private Button prevButton;

	private double currentRotation = 0.0;

	private int currentPhotoIndex = 0; // the photo that is currently in front

	private int carouselSize = 9;

	private int preLoadSize = 3;

	boolean timerOn;
	double velocity;
	Timer timer = new RotationTimer();
	long lastTime;

	double acceleration = .998;
	double velocityThreshold = .00002;

	private boolean mouseMoved = false;

	private MouseBehavior mouseBehavior;

	private FocusBehavior focusBehavior;
	
	public Carousel() {
		this(true, true);
	}
	
	public Carousel(boolean useDefaultMouseBehavior, boolean useDefaultFocusBehavior) {
		// Set up UI structure
		carouselDock = new DockPanel();
		imagePanel = new AbsolutePanel();
		prevButton = new Button("Previous");
		nextButton = new Button("Next");
		imagePanel.setSize("100%", "100%");
		caption = new Label();
		carouselDock.add(caption, DockPanel.SOUTH);
		carouselDock.add(imagePanel, DockPanel.NORTH);
		carouselDock.setCellHeight(caption, "15");
		carouselDock.setCellHeight(imagePanel, "100%");
		carouselDock.setCellHorizontalAlignment(caption, DockPanel.ALIGN_CENTER);
		imagePanel.getElement().getStyle().setProperty("overflow", "hidden");
		carouselDock.setStyleName("photoCarousel");
		caption.setStyleName("photoCarouselCaption");
		prevButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				prev();
			}
		});
		nextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				next();
			}
		});
		carouselDock.add(prevButton, DockPanel.NORTH);
		carouselDock.add(nextButton, DockPanel.NORTH);
		
		// Set up images
		images = new CarouselImage[this.carouselSize + (this.preLoadSize * 2)];
		for (int i = 0; i < images.length; i++) {
			images[i] = new CarouselImage();
			images[i].setSize("1", "1");
			Utils.preventDrag(images[i]);
			Utils.preventSelection(images[i].getElement());
			images[i].getElement().getStyle().setProperty("display", "none");
			images[i].addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (mouseMoved)
						return; // make sure a photo click is not registered
					// when the mouse is dragged.
					Image img = (Image) event.getSource();
					for (int i = 0; i < carouselSize; i++) {
						if (images[i + preLoadSize] == img) {
							int pIndex = i - 4 + currentPhotoIndex;
							pIndex = Utils.modulus(pIndex, photos.size());
							// fire off photo clicked event
							PhotoClickEvent pcEvent = new PhotoClickEvent();
							pcEvent.setPhotoIndex(pIndex);
							pcEvent.setPhoto(photos.get(pIndex));
							fireEvent(pcEvent);
							break;
						}
					}
				}
			});
			imagePanel.add(images[i]);
		}
		this.initWidget(carouselDock);

		// Sync caption with front-most photo.
		addPhotoToFrontHandler(new PhotoToFrontHandler() {
			public void photoToFront(PhotoToFrontEvent event) {
				caption.setText(event.getPhoto().getTitle());
			}
		});

		// Rotate when mouse is dragged
		mouseBehavior = new MouseBehavior(this);
		if (useDefaultMouseBehavior)
			mouseBehavior.start();
		// Focus when current photo clicked
		focusBehavior = new FocusBehavior(this);
		if (useDefaultFocusBehavior) {
			focusBehavior.start();
		}

		// These are used to help make sure a photo click is not registered when
		// the mouse is dragged.
		addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {
				mouseMoved = false;
			}
		});
		addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				mouseMoved = true;
			}
		});
		addMouseWheelHandler(new MouseWheelHandler(){
			public void onMouseWheel(MouseWheelEvent event){
				if(event.isNorth()){
					next();
				} else if(event.isSouth()){
					prev();
				}
			}
		});
	}
	
	public void setUseDefaultMouseBehavior(boolean useDefaultMouseBehavior) {
		if (useDefaultMouseBehavior) {
			mouseBehavior.start();
		} else {
			mouseBehavior.stop();
		}
	}
	
	public void setUseDefaultFocusBehavior(boolean useDefaultFocusBehavior) {
		if (useDefaultFocusBehavior) {
			focusBehavior.start();
		} else {
			focusBehavior.stop();
		}
	}

	/**
	 * Lay out the images based on the current rotation.
	 */
	public void placeImages() {
		// The size of the container the holds the images.
		int containerWidth = imagePanel.getOffsetWidth();
		int containerHeight = imagePanel.getElement().getClientHeight();

		// The base dimensions for each image. Images are scaled from these base
		// dimensions.
		double boxHeight = containerHeight * 3.0 / 8.0;
		double boxWidth = boxHeight * 1.2;
		// The radius of the ellipse that the images are set around.
		double xRadius = (containerWidth - boxWidth) / 2.0;
		double yRadius = boxHeight / 3.0;
		// A factor for achieving the spiral affect. The greater this value, the
		// more pronounced the spiral effect.
		double spiralSpread = yRadius * .5;

		// The fraction that the images are offset from a whole number rotation.
		// This value will be between -0.5 and 0.5.
		double decimalOffset = currentRotation - Math.round(currentRotation);
		// The angle (in radians) that the images are offset from the base
		// positions. Base positions are 0*, 45*, 90*, 135*, etc. This value
		// will be between -22.5* and 22.5*.
		double angleOffset = -(decimalOffset * ((Math.PI) / 4));

		for (int i = 0; i < carouselSize; i++) {
			CarouselImage image = images[i + preLoadSize];
			// The actual angle of the given image from the front.
			double angle = ((i * Math.PI) / 4) + angleOffset;
			// These are the simple x and y coordinates of the angel in a unit
			// circle. We flipped some of the signs and dimensions around
			// because our coordinate plane is a little turned around.
			double x = -Math.sin(angle);
			double y = -Math.cos(angle);
			// The factor by which to scale the image (i.e. make it smaller or
			// larger). This is based solely on the 'y' coordinate.
			double scale = Math.pow(2, y);
			// set the zindex so that images in the front appear on top of
			// images behind.
			int zindex = (int) (y * 10) + 10;
			image.getElement().getStyle().setProperty("zIndex", Integer.toString(zindex));

			// set the size of the image. The aspect ratio of the image is
			// maintained as the image is scaled so that it fits inside the
			// correct "box" dimensions.
			image.sizeToBounds((int) (scale * boxWidth), (int) (scale * boxHeight));

			// The x coordinate is obtained by simply scaling the unit-circle x
			// coordinate to fit the container.
			int xcoord = (int) Math.round((x * xRadius) + (containerWidth - image.getWidth()) / 2.0);
			// The y coordinate is similarly calculated, except that the spiral
			// factor is also added. Basically, the farther the image is around
			// the circle, the farther down it is shifted to give the spiral
			// effect.
			int ycoord = (int) Math.round((y * yRadius) + containerHeight - boxHeight - yRadius - image.getHeight()
					/ 2.0 - Math.round(spiralSpread * (i - 4 - decimalOffset)));

			imagePanel.setWidgetPosition(image, xcoord, ycoord);

			// Finally, fade out the images that are at the very back. Make sure
			// the rest have full opacity.
			if (i == 0) {
				image.setOpacity(.5 - decimalOffset);
			} else if (i == carouselSize - 1) {
				image.setOpacity(.5 + decimalOffset);
			} else {
				image.setOpacity(1.0);
			}
		}
	}

	/**
	 * Set a list of photos to be displayed in the carousel.
	 */
	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
		currentPhotoIndex = Utils.modulus(currentPhotoIndex, photos.size());
		for (int i = 0; i < images.length; i++) {
			int pIndex = i - preLoadSize - 4 + currentPhotoIndex;
			pIndex = Utils.modulus(pIndex, photos.size());
			images[i].setUrl(photos.get(pIndex).getUrl());
		}
		for (int i = 0; i < carouselSize; i++) {
			images[i + preLoadSize].getElement().getStyle().setProperty("display", "");
		}
		PhotoToFrontEvent evt = new PhotoToFrontEvent();
		evt.setPhotoIndex(currentPhotoIndex);
		evt.setPhoto(photos.get(currentPhotoIndex));
		fireEvent(evt);
		Timer t = new Timer() {
			
			@Override
			public void run() {
				placeImages();
			}
		};
		t.schedule(1000);
	}

	private void setCurrentPhotoIndex(int photoIndex) {
		if (photoIndex == this.currentPhotoIndex){
			return;
		}
		photoIndex = Utils.modulus(photoIndex, photos.size());
		if (this.currentPhotoIndex == photoIndex) {
			return;
		} else {
			int shiftOffset = photoIndex - this.currentPhotoIndex;

			int halfPhotos = photos.size() / 2;
			if (shiftOffset < -halfPhotos) {
				shiftOffset += photos.size();
			} else if (shiftOffset > halfPhotos) {
				shiftOffset -= photos.size();
			}
			if (shiftOffset > 0) {
				// Next
				// Creating temp array of images to hold shifted images
				CarouselImage[] temps = new CarouselImage[shiftOffset];
				for (int j = 0; j < temps.length; j++) {
					temps[j] = images[j];
				}
				for (int i = 0; i < images.length - (shiftOffset); i++) {
					images[i] = images[i + (shiftOffset)];
				}
				// update from large array
				for (int k = 0; k < temps.length; k++) {
					int pIndex = photoIndex - 4 + carouselSize + preLoadSize - shiftOffset + k;
					pIndex = Utils.modulus(pIndex, photos.size());
					images[k + images.length - shiftOffset] = temps[k];
					temps[k].setUrl(photos.get(pIndex).getUrl());
				}
			} else if (shiftOffset < 0) {
				shiftOffset *= -1;
				// Prev
				CarouselImage[] temps = new CarouselImage[shiftOffset];
				for (int j = 0; j < temps.length; j++) {
					temps[j] = images[j + images.length - shiftOffset];
				}
				for (int i = images.length - 1; i >= shiftOffset; i--) {
					images[i] = images[i - shiftOffset];
				}
				// update from large array
				for (int k = 0; k < temps.length; k++) {
					int pIndex = photoIndex - 4 - preLoadSize + k;
					pIndex = Utils.modulus(pIndex, photos.size());
					images[k] = temps[k];
					temps[k].setUrl(photos.get(pIndex).getUrl());
				}
			}
			for (int i = 0; i < preLoadSize; i++) {
				images[i].getElement().getStyle().setProperty("display", "none");
				images[images.length - i - 1].getElement().getStyle().setProperty("display", "none");
			}
			for (int i = 0; i < carouselSize; i++) {
				images[i + preLoadSize].getElement().getStyle().setProperty("display", "");
			}
			this.currentPhotoIndex = photoIndex;
		}
	}

	/**
	 * Acceleration determines how fast the carousel slows down or speeds up as
	 * it rotates. A value of 1.0 will cause the carousel to rotate at a
	 * constant speed. A value greater than 1.0 will cause it to speed up over
	 * time. A value less than 1.0 will cause it to slow down.
	 */
	public double getAcceleration() {
		return acceleration;
	}

	/**
	 * Acceleration determines how fast the carousel slows down or speeds up as
	 * it rotates. A value of 1.0 will cause the carousel to rotate at a
	 * constant speed. A value greater than 1.0 will cause it to speed up over
	 * time. A value less than 1.0 will cause it to slow down.
	 */
	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	private class RotationTimer extends Timer {
		public void run() {
			long currentTime = System.currentTimeMillis();
			int ticks = (int) (currentTime - lastTime);
			lastTime = currentTime;

			if (acceleration == 1.0) {
				setRotation(currentRotation + ticks * velocity);
			} else {
				double newVelocity = velocity * Math.pow(acceleration, ticks);
				if (newVelocity < velocityThreshold && newVelocity > -velocityThreshold) {
					setRotation(currentRotation
							+ Utils.distanceFromStartingVelocity(velocity, acceleration, velocityThreshold));
					setVelocity(0.0);
				} else {
					setRotation(currentRotation + Utils.distanceForXTicks(velocity, acceleration, ticks));
					setVelocity(velocity * Math.pow(acceleration, ticks));
				}
			}
		}
	}

	/**
	 * Set the speed for the carousel to rotate.
	 */
	public void setVelocity(double velocity) {
		this.velocity = velocity;
		if (velocity > -velocityThreshold && velocity < velocityThreshold) {
			if (timerOn) {
				timer.cancel();
				timerOn = false;
			}
			this.velocity = 0;
		} else if (!timerOn) {
			lastTime = System.currentTimeMillis();
			timer.scheduleRepeating(20);
			timerOn = true;
			timer.run();
		}
	}
	
	public double getVelocity() {
		return velocity;
	}

	/**
	 * The current rotational position of the carousel. Rotation is based on
	 * indices in the photo list. So if the 3rd photo in the list is in the
	 * front of the carousel, currentRotation will be 2.0 (indicies are 0
	 * based).
	 */
	public double getRotation() {
		return currentRotation;
	}

	/**
	 * The current rotational position of the carousel. Rotation is based on
	 * indices in the photo list. So if the 3rd photo in the list is in the
	 * front of the carousel, currentRotation will be 2.0 (indicies are 0
	 * based).
	 */
	public void setRotation(double value) {
		int pi = getPhotoIndex();
		currentRotation = Utils.modulus(value, photos.size());
		setCurrentPhotoIndex((int) Math.round(currentRotation));
		if (pi != getPhotoIndex()) {
			PhotoToFrontEvent event = new PhotoToFrontEvent();
			event.setPhoto(photos.get(getPhotoIndex()));
			event.setPhotoIndex(getPhotoIndex());
			fireEvent(event);
		}
		placeImages();
	}

	/**
	 * Start an animated rotation to the given position. Position is based on
	 * indices in the photo list. So to rotate to the 3rd photo in the list,
	 * pass 2.0 (indicies are 0 based) as the position.
	 */
	public void rotateTo(double position) {
		if (acceleration >= 1.0) {
			setRotation(position);
			return;
		}
		double distance = Utils.modulus(position, photos.size()) - currentRotation;
		if (distance > photos.size() / 2) {
			distance -= photos.size();
		} else if (distance < photos.size() / -2) {
			distance += photos.size();
		}
		setVelocity(Utils.velocityForDistance(distance, acceleration, velocityThreshold));
	}

	/**
	 * Start an animated rotation that will rotate the carousel by the given
	 * distance. A distance of 1.0 is equivalent to moving by one photo in the
	 * carousel, and 2.0 is two photos, etc.
	 */
	public void rotateBy(double distance) {
		if (acceleration >= 1.0) {
			setRotation(currentRotation + distance);
			return;
		}
		setVelocity(Utils.velocityForDistance(distance, acceleration, velocityThreshold));
	}

	/**
	 * Start an animated rotation to the previous photo.
	 */
	public void prev() {
		rotateBy(- 1.0);
	}

	/**
	 * Start an animated rotation to the next photo.
	 */
	public void next() {
		rotateBy(+ 1.0);
	}

	/**
	 * Returns the zero-based index in the photo list of the photo that is
	 * currently in front.
	 */
	public int getPhotoIndex() {
		return currentPhotoIndex;
	}
	
	public Photo getCurrentPhoto() {
		return photos.get(currentPhotoIndex);
	}

	/**
	 * This handler is fired each time a photo rotates to the front, becoming
	 * the current photo.
	 */
	public HandlerRegistration addPhotoToFrontHandler(PhotoToFrontHandler handler) {
		return addHandler(handler, PhotoToFrontEvent.getType());
	}

	/**
	 * This handler is fired when any photo in the carousel is clicked.
	 */
	public HandlerRegistration addPhotoClickHandler(PhotoClickHandler handler) {
		return addHandler(handler, PhotoClickEvent.getType());
	}

	/**
	 * This handler is fired when a photo is focused (i.e. it is clicked and
	 * opens full-size).
	 */
	public HandlerRegistration addPhotoFocusHandler(PhotoFocusHandler handler) {
		if (focusBehavior == null)
			return null;
		return focusBehavior.addPhotoFocusHandler(handler);
	}

	/**
	 * This handler is fired when a photo is un-focused (i.e. when the expanded
	 * photo box is closed).
	 */
	public HandlerRegistration addPhotoUnfocusHandler(PhotoUnfocusHandler handler) {
		if (focusBehavior == null)
			return null;
		return focusBehavior.addPhotoUnfocusHandler(handler);
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return imagePanel.addDomHandler(handler, ClickEvent.getType());
	}

	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return imagePanel.addDomHandler(handler, MouseDownEvent.getType());
	}

	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return imagePanel.addDomHandler(handler, MouseMoveEvent.getType());
	}

	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return imagePanel.addDomHandler(handler, MouseUpEvent.getType());
	}
	
	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler){
		return imagePanel.addDomHandler(handler, MouseWheelEvent.getType());

	}
}
