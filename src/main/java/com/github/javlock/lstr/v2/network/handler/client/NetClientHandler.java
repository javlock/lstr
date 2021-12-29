package com.github.javlock.lstr.v2.network.handler.client;

import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.network.handler.NHandler;

import io.netty.channel.ChannelHandlerContext;

public class NetClientHandler extends NHandler {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof InitSessionPacket) {// no parse (info already exist)
			InitSessionPacket initSessionPacket = new InitSessionPacket();
			initSessionPacket.setFrom(FromT.CLIENT);
			initSessionPacket.setHost(AppHeader.getConfig().getTorDomain());
			initSessionPacket.setPort(4001);
			ctx.channel().writeAndFlush(initSessionPacket);
			return;
		}

		getLOGGER().info("{} msg:{}", ctx.channel().remoteAddress(), msg);
	}

	@Override
	public NHandlerSide getSide() {
		return NHandlerSide.CLIENT;
	}
}
