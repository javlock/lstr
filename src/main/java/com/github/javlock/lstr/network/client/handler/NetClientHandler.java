package com.github.javlock.lstr.network.client.handler;

import com.github.javlock.lstr.network.NetHandler;

import io.netty.channel.ChannelHandlerContext;

public class NetClientHandler extends NetHandler {
	public NetClientHandler() {
		setType(HandlerType.CLIENT);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

}