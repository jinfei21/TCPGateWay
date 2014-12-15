package com.ctrip.gateway.client;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GateConnectionPool {

	private volatile int minCount;
	private volatile int maxCount;

	private GateConnectionFactory connectFactory;
	private Set<GateConnection> lease;
	private LinkedList<GateConnection> free;
	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	public GateConnectionPool(int minCount,int maxCount,GateConnectionFactory connectFactory){
		this.free = new LinkedList<GateConnection>();
		this.lease = new HashSet<GateConnection>();
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.connectFactory = connectFactory;
	}
	
	public GateConnection lease(){
		return lease(2000);
	}
	
	public GateConnection lease(long timeout) {
		
		try{
			lock.lock();
			
			Date deadline = new Date(System.currentTimeMillis()+timeout);
			while(true){
				if(connectCount()<=minCount){
					return createToLease();
				}
				GateConnection connection = null;
				if((connection=free.pollFirst())!=null){
					lease.add(connection);
					return connection;
				}
				
				if(connectCount()<=maxCount){
					return createToLease();
				}
				try {
					condition.awaitUntil(deadline);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}finally{
			lock.unlock();
		}
	}
	
	public void release(GateConnection connection){
		try{
			lock.lock();
			lease.remove(connection);
			free.add(connection);			
			condition.signalAll();
		}finally{
			lock.unlock();
		}	
	}
	
	public void close(){

		while(lease.size() != 0){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		for(GateConnection connection:free){
				connection.close();
		}
		free.clear();
	}
	
	private int connectCount(){
		return lease.size()+free.size();
	}
	
	private GateConnection createToLease(){
		GateConnection connection = connectFactory.createConnection();
		lease.add(connection);
		return connection;
	}
}
