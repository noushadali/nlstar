package com.denisk.appengine.nl.server;

import javax.servlet.ServletException;

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
		System.out.println("In servlet");
		df.createTestDataSet(5, 10);
	}

	@Override
	public String countEntities() {
		return "Catogories: " + df.getDataHandler().countCategories() + ", goods: " + df.getDataHandler().countGoods();
	}

}
