package com.ctrip.gateway.common;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.groovy.GroovyCompiler;
import com.ctrip.gateway.groovy.GroovyFileFilter;


public class GateFilterManage {

	private static final Logger logger = LoggerFactory.getLogger(GateFilterManage.class);
	
	private static GateFilterManage instance = null;
	
	private Map<String,GateFilter> filters = new ConcurrentHashMap<String, GateFilter>();
    private final ConcurrentHashMap<String, Long> filterClassLastModified = new ConcurrentHashMap<String, Long>();
		
	private String filterDir[];
	private FilenameFilter filenameFilter;
	private static Thread flusher;
	private static int refreshInterval;
	private static volatile boolean runing = true; 
    private static GroovyCompiler compiler = new GroovyCompiler();
    private static FilterFactory filterFactory = new DefaultFilterFactory();
	
    private ConcurrentHashMap<String,List<GateFilter>> filtersByType = new ConcurrentHashMap<String, List<GateFilter>>();
    
	private GateFilterManage(){
		this.filenameFilter = new GroovyFileFilter();
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
	
	
	public void init(int refreshInterval,String... dir){
		this.refreshInterval = refreshInterval;
		this.filterDir = dir;
		startFlush();
	}
	
	public static void setRefreshInterval(int refreshInterval){
		instance.refreshInterval = refreshInterval;
	}
	
	public List<GateFilter> getGateFilterByType(String type){
	       List<GateFilter> list = filtersByType.get(type);
	        if (list != null) return list;

	        list = new ArrayList<GateFilter>();

	        Collection<GateFilter> filterList = filters.values();
	        for (Iterator<GateFilter> iterator = filterList.iterator(); iterator.hasNext(); ) {
	            GateFilter filter = iterator.next();
	            if (filter.filterType().equals(type)) {
	                list.add(filter);
	            }
	        }
	        Collections.sort(list); // sort by priority

	         filtersByType.putIfAbsent(type, list);
	        return list;
	}
	
	private void startFlush(){
		flusher = new Thread("GroovyFilterFileManagerPoller") {
	            public void run() {
	                while (runing) {
	                    try {
	                        sleep(refreshInterval * 1000);
	                        manageFiles();
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        };
	        flusher.setDaemon(true);
	        flusher.start();
	}
	
	private void stopFlush(){
		runing = false;
	}
	
	public static void shutdown(){
		instance.stopFlush();
	}
	
	private List<File> getFiles() {
        List<File> list = new ArrayList<File>();
        for (String dir : filterDir) {
            if (dir != null) {
                File directory = getDirectory(dir);
                File[] aFiles = directory.listFiles(filenameFilter);
                if (aFiles != null) {
                    list.addAll(Arrays.asList(aFiles));
                }
            }
        }
        return list;
    }

    private File getDirectory(String path) {
        File  file = new File(path);
        if (!file.isDirectory()) {
            URL resource = GateFilterManage.class.getClassLoader().getResource(path);
            try {
                file = new File(resource.toURI());
            } catch (Exception e) {
                logger.error("Error accessing directory in classloader. path=" + path, e);
            }
            if (!file.isDirectory()) {
                throw new RuntimeException(file.getAbsolutePath() + " is not a valid directory");
            }
        }
        return file;
    }
    
    private void processGroovyFiles(List<File> files) throws Exception,InstantiationException, IllegalAccessException {

		for (File file : files) {
			putFilter(file);
		}
	}
    
    
   private boolean putFilter(File file) throws Exception {
        String sName = file.getAbsolutePath() + file.getName();
        if (filterClassLastModified.get(sName) != null && (file.lastModified() != filterClassLastModified.get(sName))) {
            logger.debug("reloading filter " + sName);
            filters.remove(sName);
        }
        GateFilter filter = filters.get(sName);
        if (filter == null) {
            Class clazz = compiler.compile(file);
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = (GateFilter) filterFactory.newInstance(clazz);
                filters.put(file.getAbsolutePath() + file.getName(), filter);
                filterClassLastModified.put(sName, file.lastModified());
                List<GateFilter> list = filtersByType.get(filter.filterType());
                if (list != null) {
                	filtersByType.remove(filter.filterType()); //rebuild this list
                }
                return true;
            }
        }

        return false;
    }

   private void manageFiles() throws Exception, IllegalAccessException, InstantiationException {
        List<File> aFiles = getFiles();
        processGroovyFiles(aFiles);
    }
	
}
