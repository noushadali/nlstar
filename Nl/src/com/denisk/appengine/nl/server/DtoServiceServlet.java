package com.denisk.appengine.nl.server;

import com.denisk.appengine.nl.client.DtoService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DtoServiceServlet extends RemoteServiceServlet implements
		DtoService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3021789825605473063L;

	@Override
	public void generateTestData() {
		// new TestDataFiller().createTestDataSet(5, 10);
		System.out.println("In servlet");
	}

}
