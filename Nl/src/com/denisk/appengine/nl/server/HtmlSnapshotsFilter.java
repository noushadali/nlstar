package com.denisk.appengine.nl.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.denisk.appengine.nl.client.ui.views.Nl;
import com.denisk.appengine.nl.client.util.Function;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class HtmlSnapshotsFilter implements Filter {
	
	private static String rewriteQueryString(String queryString)
			throws UnsupportedEncodingException {
		StringBuilder queryStringSb = new StringBuilder(queryString);
		int i = queryStringSb.indexOf("&_escaped_fragment_=");
		boolean fragmentAtBeginning = false;
		if (i == -1) {
			i = queryStringSb.indexOf("_escaped_fragment_=");
			fragmentAtBeginning = true;
		}
		if (i != -1) {
			//before fragment
			StringBuilder sb = new StringBuilder();
			if(! fragmentAtBeginning){
				String beforeFragment = queryStringSb.substring(0, i);
				sb.append(beforeFragment);
			}
			sb.append("#!");
			//if fragment is at the beginning, there is no & in front of it
			sb.append(URLDecoder.decode(
					queryStringSb.substring(i + (fragmentAtBeginning ? "_escaped_fragment_=".length() : "&_escaped_fragment_=".length()), queryStringSb.length()),
					"UTF-8"));
			queryStringSb = sb;
		}
		return queryStringSb.toString();
	}

	@Override
	public void doFilter(ServletRequest request,
			final ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String queryString = req.getQueryString();

		if ((queryString != null)
				&& (queryString.contains("_escaped_fragment_"))) {
			StringBuilder pageNameSb = new StringBuilder("http://");
			pageNameSb.append(req.getServerName());
			if (req.getServerPort() != 0) {
				pageNameSb.append(":");
				pageNameSb.append(req.getServerPort());
			}
			pageNameSb.append(req.getRequestURI());
			queryString = rewriteQueryString(queryString);
			pageNameSb.append("?");
			pageNameSb.append(queryString);

			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
			webClient.setJavaScriptTimeout(10000);
			
			String pageName = pageNameSb.toString();
			HtmlPage page = webClient.getPage(pageName);

			res.setContentType("text/html;charset=UTF-8");
			PrintWriter out = res.getWriter();
			out.println("<hr>");
			out.println("<center><h3>You are viewing a non-interactive page that is intended for the crawler.  "
					+ "You probably want to see this page: <a href=\""
					+ pageName + "\">" + pageName + "</a></h3></center>");
			out.println("<hr>");

			webClient.waitForBackgroundJavaScriptStartingBefore(10000);

			out.println(page.asXml());
			webClient.closeAllWindows();
			out.close();

		} else {
			
				chain.doFilter(request, response);
		}
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

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
