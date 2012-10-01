package com.denisk.appengine.nl.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;

import com.denisk.appengine.nl.shared.UploadStatus;
import com.google.appengine.api.datastore.Entity;

public class PersisterServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String KIND_FIELD = "kind";
	private final static String KEY_FIELD = "key";
	private final static String PARENT_KEY_FIELD = "parentKey";
	private DataHandler dh = new DataHandler();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		DtoServiceServlet.checkCredentials();
		
		if(! ServletFileUpload.isMultipartContent(req)) {
			System.out.println("Request to Persister was not multipart/form request");
			return;
		}
		ServletFileUpload upload = new ServletFileUpload();

		// Parse the request
		FileItemIterator iter;
		String kind = null;
		String key = null;
		String parentKey = null;
		HashMap<String, UploadStatus> uploadStatuses = new HashMap<String, UploadStatus>();
		HashMap<String, ByteArrayHolder> uploadContents = new HashMap<String, ByteArrayHolder>();
		HashMap<String, String> regularProperties = new HashMap<String, String>();
		try {
			iter = upload.getItemIterator(req);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String fieldName = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					String value = Streams.asString(stream);
					System.out.println("Form field " + fieldName + " with value "
							+ value + " detected.");
					if(KEY_FIELD.equals(fieldName)){
						key = value;
					} else if(PARENT_KEY_FIELD.equals(fieldName)) {
						parentKey = value;
					} else if(KIND_FIELD.equals(fieldName)){
						kind = value;
					} else if(fieldName.startsWith(UploadStatus.FLAG_PREFIX)){
						String name = fieldName.substring(UploadStatus.FLAG_PREFIX.length());
						UploadStatus status = UploadStatus.valueOf(UploadStatus.class, value);
						uploadStatuses.put(name, status);
					} else {
						regularProperties.put(fieldName, value);
					}
				} else {
					System.out.println("File field " + fieldName
							+ " with file name " + item.getName()
							+ " detected.");
					byte[] content = IOUtils.toByteArray(stream);
					uploadContents.put(fieldName, new ByteArrayHolder(content));
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException(e);
		}
		Entity entity;
		
		if(key == null || key.isEmpty()){
			//create new
			entity = dh.createEntity(kind, parentKey, regularProperties);
		} else {
			//update existing
			entity = dh.find(key);
			dh.setProperties(entity, regularProperties);
			dh.save(entity);
		}

		for(String blobField: uploadStatuses.keySet()){
			UploadStatus status = uploadStatuses.get(blobField);
			switch(status){
			case DELETE:
				if(entity == null){
					throw new IllegalArgumentException("Attempt to delete blob " + blobField + " of entity that does not exist, key = " + key);
				}
				dh.deleteBlob(entity, blobField);
				break;
			case NO_CHANGE:
				//do_nothing
				break;
			case UPDATE:
				ByteArrayHolder byteArrayHolder = uploadContents.get(blobField);
				if(byteArrayHolder == null){
					throw new IllegalStateException("No corresponding content for entity that needed to be updated: " + blobField);
				}
				dh.updateBlob(entity, blobField, byteArrayHolder.getBytes());
				break;
				
			}
		}
	}
	
	private static class ByteArrayHolder {
		private byte[] bytes;
		
		public ByteArrayHolder(byte[] bytes){
			this.bytes = bytes;
		}
		
		public byte[] getBytes(){
			return bytes;
		}
	}
}
