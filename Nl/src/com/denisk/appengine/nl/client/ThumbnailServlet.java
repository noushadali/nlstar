package com.denisk.appengine.nl.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class ThumbnailServlet extends HttpServlet {

	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 115;

	private static final int MAX_WIDTH = 500;
	private static final int MAX_HEIGHT = 300;

	private static final int BUFFER_SIZE = 10240;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("In ThumbnailServlet");
		String blobKey = req.getParameter("key");
		if (blobKey == null) {
			System.out.println("Key was null, exiting");
			return;
		}
		String header = req.getHeader("If-None-Match");
		if(header != null && header.equals(blobKey)){
			System.out.println("Not modified");
			resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		
		//todo - compose from key, x and y
		resp.setHeader("ETag", blobKey);
		String x = req.getParameter("x");
		String y = req.getParameter("y");

		int width;
		int height;

		if (x == null) {
			System.out.println("x was null, using default");
			width = DEFAULT_WIDTH;
		} else {
			try {
				width = Integer.parseInt(x);
				System.out.println("Got width " + width);
			} catch (NumberFormatException e) {
				width = DEFAULT_WIDTH;
			}
		}
		if (width > MAX_WIDTH) {
			width = DEFAULT_WIDTH;
		}

		if (y == null) {
			System.out.println("y was null, using default");
			height = DEFAULT_HEIGHT;
		} else {
			try {
				height = Integer.parseInt(y);
				System.out.println("got height=" + height);
			} catch (NumberFormatException e) {
				height = DEFAULT_HEIGHT;
			}
		}
		if (height > MAX_HEIGHT) {
			height = DEFAULT_HEIGHT;
		}

		MemcacheService memcacheService = MemcacheServiceFactory
				.getMemcacheService();
		Image image = (Image) memcacheService.get(blobKey);
		if (image == null) {
			System.out.println("Creating new image for: " + blobKey);
			image = ImagesServiceFactory
					.makeImageFromBlob(new BlobKey(blobKey));
			memcacheService.put(blobKey, image);
			
		}
		ImagesService is = ImagesServiceFactory.getImagesService();
		Transform resize = ImagesServiceFactory.makeResize(width, height);
		
		Image transformed = is.applyTransform(resize, image);
		
		
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		try {
			input = new BufferedInputStream(new ByteArrayInputStream(transformed.getImageData()), BUFFER_SIZE);
			output = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			close(input);
			close(output);
		}
	}

	private void close(Closeable resourse) {
		if (resourse != null) {
			try {
				resourse.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
