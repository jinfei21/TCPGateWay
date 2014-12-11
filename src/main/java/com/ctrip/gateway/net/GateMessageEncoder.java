package com.ctrip.gateway.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.ctrip.gateway.common.GateMessage;

public class GateMessageEncoder extends MessageToByteEncoder<GateMessage>{

	@Override
	protected void encode(ChannelHandlerContext ctx, GateMessage msg,
			ByteBuf out) throws Exception {
		msg.encodeToBuf(out);
		
	}

	
}
