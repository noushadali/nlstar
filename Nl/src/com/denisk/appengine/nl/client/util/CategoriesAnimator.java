package com.denisk.appengine.nl.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.denisk.appengine.nl.client.ui.views.Nl;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class CategoriesAnimator {
	private static final int ITEM_WIDTH = 500;
	private static final int ITEM_HEIGHT = 700;

	//time between panel waves
	private static final double ANIMATION_DELAY = 0.1;
	//overall time of the animation
	private static final double ANIMATION_SPEED = 0.5;
	
	private static final int CATEGORIES_MARGIN = 10;
	private static final int TOP_OFFSET = 100;

	private ArrayList<ArrayList<Widget>> widgetMatrix;
	
	private Nl parent;

	public CategoriesAnimator(Nl parent) {
		this.parent = parent;
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
	/**
	 * This cuts 'px' suffix from given string and returns int amount of the
	 * value
	 */
	private int getAmount(String style) {
		return Integer.parseInt(style.substring(0, style.length() - 2));
	}


	private void addWidgetsToPanel(List<? extends Widget> widgets, Panel panel) {
		for (Widget w : widgets) {
			panel.add(w);
		}
	}

	/**
	 * This is called when categories are appeared on the screen
	 * 
	 * @param widgets
	 *            - categories widgets, are not yet added to panel
	 * @param panel
	 *            - future parent of Widgets
	 */
	public void animateWidgetGridAppearenceAndAddToPanel(
			List<? extends Widget> widgets, Panel panel) {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		int widgetCount = widgets.size();
		if (widgetCount == 0) {
			parent.hideBusyIndicator();
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
		moveWidgetsOutOfTheScreen();
		// =============================

		// ===================================
		// set destination dementions
		Timer timer = new Timer() {
			@Override
			public void run() {
				// set animation delays on widgets
				setTransitionTimeouts(true);

				animate(destinationDimentions);
				
				parent.hideBusyIndicator();
			}
		};
		// it seems that DOM needs some time to add object appropriately, so we
		// schedule animation to 1 second in the future
		timer.schedule(1000);
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
	 * Determines whether all widgets are completely outside the screen (beyond the left side or the bottom)  
	 */
	public boolean allWidgetsOutsideTheScreen() {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		
		final HashSet<Widget> allWidgets = new HashSet<Widget>();
		for (List<Widget> l : widgetMatrix) {
			allWidgets.addAll(l);
		}

		for (Widget w : allWidgets) {
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
	 * Sets CSS3 transition timeouts for grid of widgets
	 * 
	 * @param in
	 *            true if widgets should appear, false - if they should
	 *            disappear
	 */
	public void setTransitionTimeouts(boolean in) {
		double animationSpeed = ANIMATION_SPEED;
		double delay = ANIMATION_DELAY;
		
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

	public void moveWidgetsOutOfTheScreen() {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		
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


	private static class Dimention {
		private int x;
		private int y;

		protected Dimention(int x, int y) {
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
