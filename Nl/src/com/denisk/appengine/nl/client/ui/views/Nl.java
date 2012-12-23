package com.denisk.appengine.nl.client.ui.views;

import java.util.List;

import com.denisk.appengine.nl.client.service.DtoService;
import com.denisk.appengine.nl.client.service.DtoServiceAsync;
import com.denisk.appengine.nl.client.thirdparty.com.reveregroup.carousel.client.Photo;
import com.denisk.appengine.nl.client.util.Function;
import com.denisk.appengine.nl.shared.UserStatus;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Nl implements EntryPoint {
	private static final String CATEGORY_URL_PREFIX = "category/";
	private static final String GOOD_URL_PREFIX = "good/";

	private static DtoServiceAsync dtoService = GWT.create(DtoService.class);

	private Label categoriesInfo = new Label();

	private final FlowPanel outputPanel = new FlowPanel();
	private final Label status = new Label();
	private final RootPanel rootPanel = RootPanel.get("container");
	
	private HandlerRegistration newButtonClickHandlerRegistration;
	private HandlerRegistration clearButtonHandlerRegistration;

	private Button clearButton;
	private Button newButton;
	private Button backButton;
	
	private HTML loginUrl;
	private HTML logoutUrl;

	private Image busyIndicator;
	
	// state fields
	private String selectedCategoryKeyStr;
	private AbstractItemsView currentView;
	
	//views
	private CategoriesView categoriesView;
	private GoodsView goodsView;
	
	private void createLogoutUrl() {
		dtoService.getLogoutUrl(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				logoutUrl = new HTML();
				logoutUrl.setHTML("<a href='" + result + "'>Logout</a>");
				logoutUrl.setVisible(false);
				rootPanel.add(logoutUrl);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private void createLoginUrl() {
		dtoService.getLoginUrl(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				loginUrl = new HTML();
				loginUrl.setHTML("<a href='" + result + "'>Login</a>");
				loginUrl.setVisible(false);
				rootPanel.add(loginUrl);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	private void setAdminButtonHandlers() {
		if (newButtonClickHandlerRegistration != null) {
			newButtonClickHandlerRegistration.removeHandler();
		}

		if (newButton != null) {
			newButtonClickHandlerRegistration = newButton
					.addClickHandler(currentView.getNewItemHandler());
		}
		if (clearButtonHandlerRegistration != null) {
			clearButtonHandlerRegistration.removeHandler();
		}
		if (clearButton != null) {
			clearButtonHandlerRegistration = clearButton
					.addClickHandler(currentView.getClearAllHandler());
		}
	}



	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		updateLabel();
		rootPanel.add(status);
		rootPanel.add(categoriesInfo);
		rootPanel.add(outputPanel);

		clearButton = new Button("Clear all");
		newButton = new Button("New item");
		clearButton.setVisible(false);
		newButton.setVisible(false);
		rootPanel.add(clearButton);
		rootPanel.add(newButton);

		outputPanel.addStyleName("outputPanel");
		backButton = new Button("Back");
		backButton.setVisible(false);
		rootPanel.add(backButton);
		
		busyIndicator = new Image();
		busyIndicator.setUrl("/images/loading.gif");
		rootPanel.add(busyIndicator);

		createLoginUrl();
		createLogoutUrl();
		
		categoriesView = new CategoriesView(this);
		goodsView = new GoodsView(this);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String token = event.getValue();
				if(token == null || token.isEmpty()){
					switchToCategoriesView();
					renderView(null);
					return;
				}
				String categoryKeyRegexp;
				Function<List<Photo>, Void> callback = null;
				if(token.startsWith(CATEGORY_URL_PREFIX) && !token.contains(GOOD_URL_PREFIX)){
					categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/";
				} else if(token.startsWith(CATEGORY_URL_PREFIX) && token.contains(GOOD_URL_PREFIX)){
					categoryKeyRegexp = CATEGORY_URL_PREFIX + "(.+)/good";
				
					RegExp goodRegexp = RegExp.compile(".+" + GOOD_URL_PREFIX + "(.+)/");
					MatchResult goodMatch = goodRegexp.exec(token);
					if(goodMatch == null){
						Window.alert("Wrong format for good in URL, should be '" + GOOD_URL_PREFIX + "'");
						History.newItem("", false);
						switchToCategoriesView();
						renderView(callback);
						return;
					}
					final String goodKey = goodMatch.getGroup(1);
					
					callback = new Function<List<Photo>, Void>(){
						@Override
						public Void apply(List<Photo> input) {
							//pop single good window
							for(Photo photo: input){
								if(photo.getId().equals(goodKey)){
									goodsView.selectPhoto(photo);
								}
							}
							return null;
						}
					};
				}else {
					Window.alert("URL must start with '" + CATEGORY_URL_PREFIX + "' token");
					History.newItem("", false);
					switchToCategoriesView();
					renderView(callback);
					return;
				}
				
				RegExp p = RegExp.compile(categoryKeyRegexp);
				MatchResult m = p.exec(token);
				if(m == null){
					Window.alert("There is no '" + CATEGORY_URL_PREFIX + " in the URL provided");
					switchToCategoriesView();
					renderView(callback);
					return;
				}
				String categoryKey = m.getGroup(1);
				setSelectedCategoryKeyStr(categoryKey);
				switchToGoodsView();
				renderView(callback);			}
		});
		
		History.fireCurrentHistoryState();
	}

	public void renderView(Function<?, ?> callback) {
		currentView.render(outputPanel, callback);
		outputCommonControls();
	}
	
	public void outputCommonControls() {
		dtoService.isAdmin(new AsyncCallback<UserStatus>() {
			@Override
			public void onSuccess(UserStatus userStatus) {
				switch (userStatus) {
				case ADMIN:
					logoutUrl.setVisible(true);
					loginUrl.setVisible(false);

					newButton.setVisible(true);
					clearButton.setVisible(true);
					
					setAdminButtonHandlers();
					break;
				case NOT_LOGGED_IN:
					logoutUrl.setVisible(false);
					loginUrl.setVisible(true);

					newButton.setVisible(false);
					newButton.setVisible(false);
					break;
				case NOT_ADMIN:
					logoutUrl.setVisible(true);
					loginUrl.setVisible(false);

					newButton.setVisible(false);
					newButton.setVisible(false);
					break;
				}
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}



	/**
	 * Calculates total items count and updates corresponding label
	 */
	public void updateLabel() {
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


	public void switchToCategoriesView() {
		currentView = categoriesView;
		setAdminButtonHandlers();
		backButton.setVisible(false);
		//this clears everything in the URL starting from '#' inclusive
		History.newItem("");

	}
	
	public void switchToGoodsView() {
		goodsView.getEditGoodForm().setParentCategoryItemKeyStr(selectedCategoryKeyStr);
		backButton.setVisible(true);
		this.currentView = goodsView;
	}

	public void showBusyIndicator(){
		this.busyIndicator.setVisible(true);
	}
	
	public void hideBusyIndicator(){
		this.busyIndicator.setVisible(false);
	}
	public static String getCategoryURLPart(String categoryKeyStr) {
		return CATEGORY_URL_PREFIX + categoryKeyStr + "/";
	}
	
	public static String getGoodURLPart(String goodKeyStr){
		return GOOD_URL_PREFIX + goodKeyStr + "/";
	}

	public String getSelectedCategoryKeyStr() {
		return selectedCategoryKeyStr;
	}

	public void setSelectedCategoryKeyStr(String selectedCategoryKeyStr) {
		this.selectedCategoryKeyStr = selectedCategoryKeyStr;
	}

	public DtoServiceAsync getDtoService() {
		return dtoService;
	}


	public FlowPanel getOutputPanel() {
		return outputPanel;
	}

	public Button getBackButton() {
		return backButton;
	}
	
}
