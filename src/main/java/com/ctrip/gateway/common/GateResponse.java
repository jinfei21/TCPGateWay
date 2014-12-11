package com.ctrip.gateway.common;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class GateResponse extends GateMessage{

	private MessageType responseType;
	private ChannelHandlerContext channelHandlerContext;
	
	public void setResponseType(MessageType reponseType){
		this.responseType = responseType;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	public MessageType getResponseType() {
		return responseType;
	}
	
	public ChannelFuture write(GateResponse response){
		setLen(response.getLen());
		return channelHandlerContext.writeAndFlush(response);
	}
	
	public ChannelFuture close(){
		return channelHandlerContext.close();
	}
}
