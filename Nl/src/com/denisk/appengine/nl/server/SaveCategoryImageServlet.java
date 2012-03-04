package com.denisk.appengine.nl.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class SaveCategoryImageServlet extends HttpServlet {
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	private DataHandler dh = new DataHandler();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BlobKey imageKey = null;

		Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
		if(uploads == null) {
			throw new ServletException("No uploads in request, wrong form");
		}
		List<BlobKey> blobs = uploads.get("image");
		if(blobs == null) {
			logger.warning("No image was uploaded for category ");
		} else {
			imageKey = blobs.get(0);
			if(imageKey == null) {
				logger.warning("No image key. Using empty string");
			} else {
				logger.warning("Got BlobKey " + imageKey.getKeyString());
			}
		}
		
		resp.getOutputStream().write(imageKey.getKeyString().getBytes("UTF-8"));
	}
	
}
