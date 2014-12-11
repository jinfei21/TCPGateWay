package com.ctrip.gateway.net;

import static com.ctrip.gateway.common.Constant.MAX_LENGTH;
import static com.ctrip.gateway.common.Constant.MIN_LENGTH;
import static com.ctrip.gateway.common.Constant.UNKNOW_REQUEST;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.common.BusinessRequest;
import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.HeartBeatRequest;
import com.ctrip.gateway.common.State;

public class GateRequestDecoder extends ReplayingDecoder<State>{

	private static final Logger logger = LoggerFactory.getLogger(GateRequestDecoder.class);
	private long startTime;
	private int len = 0;
	private byte[] lenBytes=null;
	
	public GateRequestDecoder(){
		super(State.CHECK_LENGTH);
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,List<Object> out) throws Exception {
		
		switch(state()){
			case CHECK_LENGTH:
				startTime = System.currentTimeMillis();
				lenBytes = new byte[8];
				in.readBytes(lenBytes);
				
				try{
					len = Integer.valueOf(new String(lenBytes).trim());
					if(len < MIN_LENGTH || len > MAX_LENGTH){
						out.add(UNKNOW_REQUEST);
						break;
					}
				}catch(Exception e){
					logger.warn("parse len bytes error!", e);
					out.add(UNKNOW_REQUEST);
					break;
				}
				checkpoint(State.GET_BODY);
			case GET_BODY:
				byte[] dataBytes = new byte[len];
				in.readBytes(dataBytes);
				
				GateRequest request = null;
				if(len == 6){
					request = new HeartBeatRequest();
				}else{
					request = new BusinessRequest();
				}
				InetSocketAddress local = (InetSocketAddress)ctx.channel().localAddress();
				InetSocketAddress remote = (InetSocketAddress)ctx.channel().remoteAddress();
				request.setCreateTime(startTime);
				request.setCompletedReceiveTime(System.currentTimeMillis());
				request.setLen(len);
				request.setLenBytes(lenBytes);
				request.setDataBytes(dataBytes);
				request.setLocalAddress(local.getAddress());
				request.setRemoteAddress(remote.getAddress());
				out.add(request);
				checkpoint(State.CHECK_LENGTH);
		}
		
	}
	

}
