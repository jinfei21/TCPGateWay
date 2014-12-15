package com.ctrip.gateway.client;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GateConnectionPool {

	private volatile int minCount;
	private volatile int maxCount;
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	
	
	public GateConnectionPool(int minCount,int maxCount){
		
	}
	
	
}
