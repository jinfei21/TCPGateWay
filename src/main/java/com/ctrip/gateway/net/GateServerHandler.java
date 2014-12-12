package com.ctrip.gateway.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static com.ctrip.gateway.common.Constant.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.common.BusinessResponse;
import com.ctrip.gateway.common.GateCallback;
import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.GateResponse;
import com.ctrip.gateway.common.HeartBeatResponse;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class GateServerHandler extends SimpleChannelInboundHandler<GateRequest> {

	private static final Logger logger = LoggerFactory.getLogger(GateServerHandler.class);

	DynamicIntProperty poolCoreSize = DynamicPropertyFactory.getInstance()
			.getIntProperty("server.pool.coresize", 200);
	DynamicIntProperty poolMaxSize = DynamicPropertyFactory.getInstance()
			.getIntProperty("server.pool.maxsize", 2000);
	DynamicLongProperty poolActiveTime = DynamicPropertyFactory.getInstance()
			.getLongProperty("server.pool.alivetime", 1000 * 60 * 5);
	DynamicStringProperty appName = DynamicPropertyFactory.getInstance()
			.getStringProperty("archaius.deployment.applicationId",
					"GateServer");

	private final ThreadPoolExecutor poolExecutor;

	public GateServerHandler() {
		this.poolExecutor = new ThreadPoolExecutor(poolCoreSize.get(),
				poolMaxSize.get(), poolActiveTime.get(), TimeUnit.MILLISECONDS,
				new SynchronousQueue<Runnable>());

		Runnable callback = new Runnable() {

			public void run() {
				poolExecutor.setCorePoolSize(poolCoreSize.get());
				poolExecutor.setMaximumPoolSize(poolMaxSize.get());
				poolExecutor.setKeepAliveTime(poolActiveTime.get(),
						TimeUnit.MILLISECONDS);
			}
		};

		poolCoreSize.addCallback(callback);
		poolMaxSize.addCallback(callback);
		poolActiveTime.addCallback(callback);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx,GateRequest request) throws Exception {
		GateResponse response = null;
		switch (request.getRequestType()) {
			case HEARTBEAT:
				response = new HeartBeatResponse();
			case BUSINESS:
				if (response == null) {
					response = new BusinessResponse();
				}
				response.setChannelHandlerContext(ctx);
				try{
					poolExecutor.submit(new GateCallback(request, response));
				}catch(Throwable t){
					ctx.close();
					logger.error("reject request!"+ctx.channel().remoteAddress().toString(), t);
				}
				break;
			case UNKNOW:
				ctx.close();
				break;
		}

	}
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        channel.attr(AttributeKey.valueOf(CHANNEL_CREATE_TIME)).set(System.currentTimeMillis());

        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
  
    }
}
