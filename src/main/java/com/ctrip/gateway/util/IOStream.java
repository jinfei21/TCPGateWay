package com.ctrip.gateway.util;
import java.io.InputStream;
import java.net.URL;

public class IOStream {

	private IOStream(){
		
	}
	
	public static InputStream getResourceStream(String name){
		InputStream is = IOStream.class.getResourceAsStream(name);
		if (is == null){
			is = IOStream.class.getClassLoader().getResourceAsStream(name);
		}
		
		if(is == null){
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		}
		return is;
	}
	
	public static URL getResource(String name){
		
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
				
		if (url == null){
			url = IOStream.class.getClassLoader().getResource(name);
		}
		if (url == null){
			url = IOStream.class.getResource(name);
		}

		return url;
	}
}