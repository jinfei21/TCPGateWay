package com.ctrip.gateway.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ctrip.gateway.util.IOStream;

public class GateFilterManage {

	private static GateFilterManage instance = null;
	private Map<String,GateFilter> filters = new ConcurrentHashMap<String, GateFilter>();
	
	private GateFilterManage(){
		
	}
	
	public void loadFilterFromFile(String path){
		InputStream input = IOStream.getResourceStream(path);
		
		if(input != null){
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
		}
		
	}
	
	public static GateFilterManage instance(){
		if(instance == null){
			synchronized (GateFilterManage.class) {
				if(instance == null){
					instance = new GateFilterManage();
				}
			}
		}
		return instance;
	}
}
