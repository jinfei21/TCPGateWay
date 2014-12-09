package com.ctrip.gateway;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.util.LogConfig;
import com.ctrip.gateway.util.NetUtil;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.PropertiesInstanceConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;

public abstract class AbstractServer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected String appName;
	protected LogConfig logConfig;
	
	public AbstractServer() throws Exception{
		appName = ConfigurationManager.getDeploymentContext().getApplicationId();
		logger.info("Initialize the {}...",appName);
		try{
			loadConfig();
			configLog();
            registerEureka();
			init();
		      logger.info("Has initialized the {}.", appName);
        } catch (Exception e) {
            logger.error("Failed to initialize the " + appName + ".", e);
            throw e;
        }
	}
	
    protected abstract void init() throws Exception;
    protected abstract void doStart() throws Exception;
    protected abstract void doClose() throws Exception;
    
    public void start() throws Exception{
    	logger.info("starting the {}...",appName);
    	
    	try{
    		doStart();
    		try{
    			 ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    		}catch(Exception e){}
    	}catch(Exception e){
    		logger.error("fail to start "+appName, e);
    		throw e;
    	}
    	logger.info("started the {}...", appName);
    }
    
    private void configLog(){
    	logConfig = new LogConfig(appName,ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
    	logConfig.config();
    }
    
    private void loadConfig(){
    	System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
    	
    	if(null != appName){
    		try{
    			logger.info(String.format("load config with appid:%s and environment:%s", appName,ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
    			ConfigurationManager.loadCascadedPropertiesFromResources(appName);
    		}catch(IOException e){
    			logger.error(String.format("fail to load config with appid:%s and environment:%s",appName,ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
    		}
    	}else{
    		logger.warn("appname is null.");
    	}
    }
    
    public void registerEureka(){
    	DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("eureka.enabled", false);
    	if(!enable.get()) return;
    	
    	EurekaInstanceConfig config = new PropertiesInstanceConfig() {
		};
		
		DiscoveryManager.getInstance().initComponent(config, new DefaultEurekaClientConfig());
		
		final DynamicStringProperty serverStatus = DynamicPropertyFactory.getInstance().getStringProperty("server."+NetUtil.getIP()+".status", "up");
    
		DiscoveryManager.getInstance().getDiscoveryClient().registerHealthCheckCallback(new HealthCheckCallback(){

			public boolean isHealthy() {
				
				return serverStatus.get().toLowerCase().equals("up");
			}
			
		});
		
		
        String version = String.valueOf(System.currentTimeMillis());
        String group = ConfigurationManager.getConfigInstance().getString("server.group", "default");
        String dataCenter = ConfigurationManager.getConfigInstance().getString("server.data-center", "default");

        final Map<String, String> metadata = new HashMap<String,String>();
        metadata.put("version", version);
        metadata.put("group", group);
        metadata.put("dataCenter", dataCenter);

        ApplicationInfoManager.getInstance().registerAppMetadata(metadata);
		
    }
}
