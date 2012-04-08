package com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickEvent;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.events.PhotoClickHandler;
/**
 * Copied from http://code.google.com/p/spiral-carousel-gwt/
 */

public class MouseBehavior {
	private final double maxVelocity = .03;
	
	private Carousel target;
	int lastXValue;
	long lastTime;
	boolean mouseDown = false;
	
	int avgDist;
	int avgTime;

	protected List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>(4);
	
	public MouseBehavior(Carousel carousel) {
		this.target = carousel;
	}
	
	public void start() {
		if (eventHandlers.size() > 0)
			return; //already started
		
		//rotate when mouse dragged
		eventHandlers.add(target.addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {
				mouseDown = true;
				if ((event.getNativeButton() & NativeEvent.BUTTON_LEFT) != 0) {
					lastXValue = event.getX();
					lastTime = System.currentTimeMillis();
					avgDist = 0;
					avgTime = 0;
					target.setVelocity(0.0);
				}
			}
		}));
		eventHandlers.add(target.addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				if (mouseDown == true) {
					long curTime = System.currentTimeMillis();
					int distance = event.getX() - lastXValue;
					int ticks = (int) (curTime - lastTime);
					lastTime = curTime;
					
					if ((distance < 0 && avgDist > 0) || (distance > 0 && avgDist < 0)) {
						avgDist = distance;
					} else {
						avgDist = (avgDist == 0) ? distance : ((4 * avgDist + distance) / 5);
					}
					avgTime = (avgTime == 0) ? ticks : ((4 * avgTime + ticks) / 5);
					
					//Utils.log(distance + ":" + avgDist + " / " + ticks + ":" + avgTime);

					if (avgTime != 0) {
						double velocity = avgDist / ((double)avgTime) / ((double)target.getOffsetWidth()) * -4.0;
						if (velocity > maxVelocity)
							velocity = maxVelocity;
						if (velocity < -maxVelocity)
							velocity = -maxVelocity;
						target.setVelocity(velocity);
					}
					lastXValue = event.getX();
				}
			}
		}));
		eventHandlers.add(Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				if (event.getTypeInt() == Event.ONMOUSEUP) {
					mouseDown = false;
				}
			}
		}));
		
		//Rotate to an image when clicked.
		eventHandlers.add(target.addPhotoClickHandler(new PhotoClickHandler() {
			public void photoClicked(PhotoClickEvent event) {
				target.rotateTo(event.getPhotoIndex());
			}
		}));
	}
	
	public void stop() {
		for (HandlerRegistration handler : eventHandlers) {
			handler.removeHandler();
		}
		eventHandlers.clear();
	}
}
