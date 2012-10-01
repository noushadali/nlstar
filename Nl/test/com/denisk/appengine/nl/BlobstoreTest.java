package com.denisk.appengine.nl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
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
		FileService fileService = FileServiceFactory.getFileService();
		AppEngineFile file = fileService.createNewBlobFile("image/jpeg");
		FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
		PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8")); 
		out.println("Hello");
		out.close();
		writeChannel.closeFinally();
		
		BlobKey bk = fileService.getBlobKey(file);
		String imageKeyStr = bk.getKeyString();
		
		assertNotNull(imageKeyStr);
		System.out.println(imageKeyStr);
	}

}
