package com.denisk.appengine.nl.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.denisk.appengine.nl.client.ui.views.Nl;
import com.denisk.appengine.nl.client.util.Function;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class HtmlSnapshotsFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, final ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		//if(req.getParameterMap().containsKey("_escaped_fragment_")){
		if(true){
			//we deal with crawler, return HTML snapshot
			final Nl nl = new Nl();
			nl.renderLayout();

			String fragment = req.getParameter("_escaped_fragment_");
			final ObjectHolder holder = new ObjectHolder();
			
			//if(fragment.contains(Nl.CATEGORY)){
			if(false){
				if(fragment.contains(Nl.GOOD_URL_PREFIX)){
					//this is good request
					
				} else {
					//this is category request
				}
			} else {
				//this is all categories request
				Function<ArrayList<Panel>, Void> callback = new Function<ArrayList<Panel>, Void>() {
					@Override
					public Void apply(ArrayList<Panel> input) {
						nl.getCategoriesView().getCategoriesAnimator().placeWidgetsOnGrid(input, nl.getOutputPanel());
						holder.set();
						return null;
					}
				};
				nl.getCategoriesView().render(nl.getOutputPanel(), callback);
				
				final Timer t = new Timer() {
					@Override
					public void run() {
						if(holder.isSet()){
							try {
								resp.getWriter().write(RootPanel.get().getElement().getInnerHTML());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							cancel();
						}
					}
				};
				t.scheduleRepeating(500);
			}
		} else {
			//this is a regular request
			chain.doFilter(req, resp);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	private static class ObjectHolder {
		private boolean set = false;

		public boolean isSet() {
			return set;
		}

		public void set() {
			this.set = true;
		}
	}
}
