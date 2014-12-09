package com.ctrip.gateway.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

public class ServerInitializer extends ChannelInitializer<SocketChannel>{

	private static final Logger logger = LoggerFactory.getLogger(ServerInitializer.class);
	
	private DynamicIntProperty minRequestLen = DynamicPropertyFactory.getInstance().getIntProperty("request.minLen", 6);
	private DynamicIntProperty maxRequestLen = DynamicPropertyFactory.getInstance().getIntProperty("request.maxLen", 1024 * 8);
	
	
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		
	}

	
}
