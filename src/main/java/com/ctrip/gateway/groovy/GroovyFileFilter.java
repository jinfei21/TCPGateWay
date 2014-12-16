package com.ctrip.gateway.groovy;

import java.io.File;
import java.io.FilenameFilter;

public class GroovyFileFilter implements FilenameFilter{

	public boolean accept(File dir, String name) {
		
		return name.endsWith(".groovy");
	}

}
