package com.denisk.appengine.nl;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class BlobstoreTest {
	private final LocalServiceTestHelper helper =
		        new LocalServiceTestHelper(new LocalBlobstoreServiceTestConfig()); 

	@Before
	public void before() {
		helper.setUp();
	}
	
	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void updateCategoryBackgroundImage() throws IOException{
		BlobKey bk = persistBlob();
		String imageKeyStr = bk.getKeyString();
		
		assertNotNull(imageKeyStr);
		System.out.println(imageKeyStr);
	}

	@Test
	public void testImagesServiceFromBlob() throws IOException{
		BlobKey key = persistBlob();
		
		Image i = (Image) ImagesServiceFactory.makeImageFromBlob(new BlobKey("abc"));
		BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(key);
		if( blobInfo != null && blobInfo.getSize() > 0){
			System.out.println("Exists");
		} else {
			System.out.println("Does not exist");
		}
		
	}
	private BlobKey persistBlob() throws IOException, FileNotFoundException,
			FinalizationException, LockException {
		FileService fileService = FileServiceFactory.getFileService();
		AppEngineFile file = fileService.createNewBlobFile("image/jpeg");
		FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
		PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8")); 
		out.println("Hello");
		out.close();
		writeChannel.closeFinally();
		
		BlobKey bk = fileService.getBlobKey(file);
		return bk;
	}

}
