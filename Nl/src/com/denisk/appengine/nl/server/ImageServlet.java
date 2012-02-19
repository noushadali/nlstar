package com.denisk.appengine.nl.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageServlet extends HttpServlet {

	private static final int BUFFER_SIZE = 10240;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String imageId = req.getParameter("id");
		
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		resp.setDateHeader("Last-Modified", 1000L);
		resp.setDateHeader("Expires", 1529669009426L);

		try {
			input = new BufferedInputStream(new URL("http://4.bp.blogspot.com/_4aRLuh8AW_A/Su248EToqTI/AAAAAAAAAa0/qaRqn7n2rpI/S220/165816.jpg").openStream(), BUFFER_SIZE);
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
		if(resourse != null){
			try {
				resourse.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
}
