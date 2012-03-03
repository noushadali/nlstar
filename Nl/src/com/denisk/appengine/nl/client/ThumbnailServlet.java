package com.denisk.appengine.nl.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.denisk.appengine.nl.server.ImageCacheService;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class ThumbnailServlet extends HttpServlet {

	private static final int BUFFER_SIZE = 10240;

	private ImageCacheService imageCacheService = new ImageCacheService();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("In ThumbnailServlet - GET");

		processRequest(req, resp, true);
	}

	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("In ThumbnailServlet - HEAD");
		processRequest(req, resp, false);
	}


	private void processRequest(HttpServletRequest req, HttpServletResponse resp, boolean content)
			throws IOException, ServletException {
		String ifNoteMatch = req.getHeader("If-None-Match");
		if(ifNoteMatch != null && imageExists(ifNoteMatch)){
			System.out.println("Not modified based on If-None-Match header");
			resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		String blobKeyStr = req.getParameter("key");
		if (blobKeyStr == null) {
			System.out.println("Key was null, exiting");
			return;
		}
		String widthStr = req.getParameter("w");
		String heightStr = req.getParameter("h");
		int w;
		int h;
		
		try {
			w = Integer.parseInt(widthStr);
		} catch (NumberFormatException e){
			throw new ServletException("Width is not a number", e);
		}
		
		try {
			h = Integer.parseInt(heightStr);
		} catch (NumberFormatException e){
			throw new ServletException("Height is not a number", e);
		}
		
		String combinedKey = imageCacheService.buildCombinedKey(new BlobKey(blobKeyStr), w, h);
		
		String ifModifiedSince = req.getHeader("If-Modified-Since");
		if(ifModifiedSince != null && imageExists(combinedKey)){
			//we don't care what 'ifModifiedSince' value is, the images can't be updated in blobstore - only overridden
			//so in case of update we would get new blobKey anyway
			System.out.print("Not modified based on If-Modified-Since presence");
			resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		
		
		resp.setHeader("ETag", combinedKey);

		Image image = imageCacheService.getImage(new BlobKey(blobKeyStr), w, h);
		if(image == null){
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (content) {
			BufferedInputStream input = null;
			BufferedOutputStream output = null;
			try {
				input = new BufferedInputStream(new ByteArrayInputStream(
						image.getImageData()), BUFFER_SIZE);
				output = new BufferedOutputStream(resp.getOutputStream(),
						BUFFER_SIZE);

				byte[] buffer = new byte[BUFFER_SIZE];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
			} finally {
				close(input);
				close(output);
			}
		}
	}

	private boolean imageExists(String key) {
		if(! key.matches(".+_\\d{1,4}_\\d{1,4}")){
			System.out.println("Header has wrong format: " + key);
			return false;
		}
		StringTokenizer st = new StringTokenizer(key, "_");

		String gotBlobKey = st.nextToken();
		String gotX = st.nextToken();
		String gotY = st.nextToken();
		
		return getImage(gotBlobKey, gotX, gotY) != null;
	}

	private Image getImage(String key, String w, String h) {
		return imageCacheService.getImage(new BlobKey(key), Integer.parseInt(w), Integer.parseInt(h));
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
