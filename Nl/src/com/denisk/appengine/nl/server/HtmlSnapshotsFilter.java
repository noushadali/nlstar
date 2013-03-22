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
		if (i == -1) {
			i = queryStringSb.indexOf("?_escaped_fragment_=");
		}
		if (i != -1) {
			StringBuilder tmpSb = new StringBuilder(queryStringSb.substring(0,
					i - 1));
			System.out.println("|" + tmpSb + "|");
			tmpSb.append("#!");
			System.out.println("|" + tmpSb + "|");
			tmpSb.append(URLDecoder.decode(
					queryStringSb.substring(i + 20, queryStringSb.length()),
					"UTF-8"));
			System.out.println("|" + tmpSb + "|");
			queryStringSb = tmpSb;
		}
		return queryStringSb.toString();
	}

	private FilterConfig filterConfig = null;

	/**
	 * Destroys the filter configuration.
	 */
	public void destroy() {
		this.filterConfig = null;
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
			pageNameSb.append(queryString);

			final WebClient webClient = new WebClient(
					BrowserVersion.FIREFOX_3_6);
			webClient.setJavaScriptEnabled(true);
			String pageName = pageNameSb.toString();
			HtmlPage page = webClient.getPage(pageName);
			webClient.waitForBackgroundJavaScriptStartingBefore(2000);

			res.setContentType("text/html;charset=UTF-8");
			PrintWriter out = res.getWriter();
			out.println("<hr>");
			out.println("<center><h3>You are viewing a non-interactive page that is intended for the crawler.  "
					+ "You probably want to see this page: <a href=\""
					+ pageName + "\">" + pageName + "</a></h3></center>");
			out.println("<hr>");

			out.println(page.asXml());
			webClient.closeAllWindows();
			out.close();

		} else {
			
				chain.doFilter(request, response);
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
