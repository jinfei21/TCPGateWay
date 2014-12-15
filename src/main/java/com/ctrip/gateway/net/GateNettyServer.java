package com.ctrip.gateway.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

public class GateNettyServer {

	private static final Logger logger = LoggerFactory
			.getLogger(GateNettyServer.class);

	private final NioEventLoopGroup bossGroup;
	private final NioEventLoopGroup workGroup;
	private final List<Channel> channels = new CopyOnWriteArrayList<Channel>();

	private DynamicIntProperty port = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8080);

	private GateServerInitializer serverInitializer;

	public GateNettyServer() throws InterruptedException {
		this.bossGroup = new NioEventLoopGroup(1);
		this.workGroup = new NioEventLoopGroup();
		this.serverInitializer = new GateServerInitializer();

		if (port.get() <= 0 || port.get() > 65535) {
			logger.error("The server port [{}] is illegal.", port.get());

		}
		ServerBootstrap bootStrap = new ServerBootstrap();
		bootStrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootStrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootStrap.group(bossGroup, workGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(serverInitializer);
		channels.add(bootStrap.bind(port.get()).sync().channel());

	}

	public void close() throws InterruptedException {
		try {

			for (Channel channel : channels) {
				channel.close().sync();
			}
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

}
