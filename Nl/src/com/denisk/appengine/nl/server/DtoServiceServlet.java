package com.denisk.appengine.nl.server;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;

import com.denisk.appengine.nl.client.DtoService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DtoServiceServlet extends RemoteServiceServlet implements
		DtoService {
	private static final long serialVersionUID = 3021789825605473063L;
	private TestDataFiller df;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		df = new TestDataFiller();
	}


	@Override
	public void generateTestData() {
		System.out.println("In servlet_real");
		df.createTestDataSet(5, 10);
	}

	@Override
	public String countEntities() {
		return "Catogories: " + df.getDataHandler().countCategories() + ", goods: " + df.getDataHandler().countGoods();
	}


	@Override
	public String getCategoriesJson() {
		try {
			return df.getDataHandler().getCategoriesJson();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void clearData() {
		df.getDataHandler().clearAll();
	}


	
}
