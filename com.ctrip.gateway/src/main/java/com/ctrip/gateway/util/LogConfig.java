package com.ctrip.gateway.util;

import java.io.InputStream;

import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogConfig {

	private static final Logger logger = LoggerFactory.getLogger(LogConfig.class);

	private String environ;
	private String appName;
	
	public LogConfig(String appName,String environ){
		this.appName = appName;
		this.environ = environ;
	}
	
	public void config(){
		logger.info("To reconfig logback.");
	
		try{
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			try{
				JoranConfigurator joranConfig = new JoranConfigurator();
				joranConfig.setContext(lc);
				lc.reset();
				String configFile = appName + "-logback-"+environ+".xml";
				InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFile);
				if(inputStream == null){
					throw new Exception("Can't find logback config");
				}
				joranConfig.doConfigure(inputStream);
				logger.info("reconfig logback");
			}catch(JoranException e){
				e.printStackTrace();
			}
			
			StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
		}catch(Exception e){
			
		}
	
	}
	
}
