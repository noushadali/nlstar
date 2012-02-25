package com.denisk.appengine.nl.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.denisk.appengine.nl.server.data.Category;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;

public class NewCategoryServlet extends HttpServlet {

	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	private DataHandler dh = new DataHandler();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String name = null;
		String description = null;
		BlobKey imageKey = null;
		
		try {
			FileItemIterator itemIterator = new ServletFileUpload().getItemIterator(req);
			while(itemIterator.hasNext()) {
				FileItemStream item = itemIterator.next();
				InputStream st = item.openStream();
				String fieldName = item.getFieldName();
				if(item.isFormField()) {
					String value = Streams.asString(st);
					if("name".equals(fieldName)){
						System.out.println("Name is " + value);
						name = value;
					} else if("description".equals(fieldName)) {
						description = value;
						System.out.println("Description is:" + value);
					} else {
						throw new ServletException("Unknown field " + fieldName);
					}
				} else {
					//this is probably blob, leave it to blobstore service...
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Can't parse multipart request", e);
		}
		
		Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
		if(uploads == null) {
			throw new ServletException("No uploads in request, wrong form");
		}
		List<BlobKey> blobs = uploads.get("image");
		if(blobs == null) {
			System.out.println("No image was uploaded for category " + name);
		} else {
			imageKey = blobs.get(0);
			if(imageKey == null) {
				System.out.println("No image key. Using empty string");
			} else {
				System.out.println("Got BlobKey " + imageKey.getKeyString());
			}
		}
		
		Category category = new Category();
		category.setName(name);
		category.setDescription(description);
		category.setImageBlobKey(imageKey.getKeyString());
		
		Key key = dh.saveCategoryWithGoods(category);
		
		blobstoreService.serve(imageKey, resp);
//		resp.getOutputStream().write(("Category created sucessfully, key is " + key.toString()).getBytes());
	}
	
}
