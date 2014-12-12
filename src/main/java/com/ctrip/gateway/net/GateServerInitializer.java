package com.ctrip.gateway.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.common.Constant;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

public class GateServerInitializer extends ChannelInitializer<SocketChannel>{

	private static final Logger logger = LoggerFactory.getLogger(GateServerInitializer.class);
	
	private DynamicIntProperty minRequestLen = DynamicPropertyFactory.getInstance().getIntProperty("request.minLen", 6);
	private DynamicIntProperty maxRequestLen = DynamicPropertyFactory.getInstance().getIntProperty("request.maxLen", 1024 * 8);
	private DynamicIntProperty readTimeout = DynamicPropertyFactory.getInstance().getIntProperty("read.timeout", 1000*60);
	private DynamicIntProperty writeTimeout = DynamicPropertyFactory.getInstance().getIntProperty("write.timeout", 1000*60);
	private DynamicIntProperty idleTimeout = DynamicPropertyFactory.getInstance().getIntProperty("idle.timeout", 1000*60);
	
	private final GateMessageEncoder gateMessageEncoder;
	private final GateServerHandler serverHandler;
	
	public GateServerInitializer(){
		this.gateMessageEncoder = new GateMessageEncoder();
		this.serverHandler = new GateServerHandler();
		Runnable callback = new Runnable(){

			public void run() {
				Constant.MIN_LENGTH = minRequestLen.get();
				Constant.MAX_LENGTH = maxRequestLen.get();
				
			}			
		};
		
		minRequestLen.addCallback(callback);
		maxRequestLen.addCallback(callback);
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new IdleStateHandler(readTimeout.get(), writeTimeout.get(), idleTimeout.get()));
		pipeline.addLast(new GateRequestDecoder());
		pipeline.addLast(gateMessageEncoder);
		pipeline.addLast(serverHandler);
	}

	
}
