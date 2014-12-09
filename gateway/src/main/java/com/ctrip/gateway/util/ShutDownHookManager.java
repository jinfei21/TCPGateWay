package com.ctrip.gateway.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutDownHookManager {

	private static final Logger logger = LoggerFactory.getLogger(ShutDownHookManager.class);
	private static final ShutDownHookManager MGR = new ShutDownHookManager();
	private Set<HookEntry> hooks = Collections.synchronizedSet(new HashSet<HookEntry>());
	private AtomicBoolean shutDownRun = new AtomicBoolean();
	static{
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				MGR.shutDownRun.set(true);
				for(Runnable hook:MGR.getShutDownHookByPriority()){
					try{
						hook.run();
					}catch(Throwable t){
						logger.warn("Shutdown fail!",t);
					}
				}
			}
		});
	}
	
	private ShutDownHookManager(){
		
	}
	
	private List<Runnable> getShutDownHookByPriority(){
		List<HookEntry> list;
		synchronized (MGR.hooks) {
			list = new ArrayList<ShutDownHookManager.HookEntry>(MGR.hooks);
		}
		
		Collections.sort(list, new Comparator<HookEntry>() {

			public int compare(HookEntry o1, HookEntry o2) {
				
				return o2.prior - o1.prior;
			}
		
		});
		List<Runnable> orders = new ArrayList<Runnable>();
		for(HookEntry entry:list){
			orders.add(entry.hook);
		}
		return orders;
	}
	
	private static class HookEntry{
		private Runnable hook;
		private int prior;
		
		public HookEntry(Runnable hook,int prior){
			this.hook = hook;
			this.prior = prior;
		}
		
		public int hashCode(){
			return hook.hashCode();
		}
		
		public boolean equals(Object obj){
			boolean res = false;
			if(obj != null){
				if(obj instanceof HookEntry){
					res = (hook == ((HookEntry)obj).hook);
				}
			}
			return res;
		}
	}
	
	public boolean removeShutDown(Runnable shutDown){
		if(shutDownRun.get()){
			throw new IllegalStateException("Shutdown is running!");
		}
		return hooks.remove(new HookEntry(shutDown,0));
	}
	
	public void addShutDown(Runnable shutDown,int prior){
		if(shutDown == null){
			throw new IllegalArgumentException("Shutdown is null!");
		}
		
		if(shutDownRun.get()){
			throw new IllegalStateException("Shutdown is running!");
		}
		
		hooks.add(new HookEntry(shutDown,prior));
	}
	
	public boolean isShutDownRunning(){
		return shutDownRun.get();
	}
	
	public boolean hasShutDownHook(Runnable shutDown){
		return hooks.contains(new HookEntry(shutDown,0));
	}
}
