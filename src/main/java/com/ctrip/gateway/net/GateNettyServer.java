package com.ctrip.gateway.net;

import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class GateNettyServer {

	private static final Logger logger = LoggerFactory.getLogger(GateNettyServer.class);
	
	private final NioEventLoopGroup bossGroup;
	private final NioEventLoopGroup workGroup;
	private final List<Channel> channels = new CopyOnWriteArrayList<Channel>();
	
	private DynamicStringProperty port = DynamicPropertyFactory.getInstance().getStringProperty("server.port", "8080");
	
	
	private GateNettyServer(){
		this.bossGroup = new NioEventLoopGroup(1);
		this.workGroup = new NioEventLoopGroup();
	}
	
}
