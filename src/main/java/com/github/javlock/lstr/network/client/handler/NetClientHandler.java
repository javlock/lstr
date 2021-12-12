package com.github.javlock.lstr.network.client.handler;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.network.NetHandler;

import io.netty.channel.ChannelHandlerContext;

public class NetClientHandler extends NetHandler {
	public NetClientHandler() {
		setType(HandlerType.CLIENT);
	}

	public NetClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		setType(HandlerType.CLIENT);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);

		if (msg instanceof InitSessionPacket) {
			InitSessionPacket initSessionPacket = (InitSessionPacket) msg;
			if (initSessionPacket.getFrom().equals(FromT.SERVER)) {
				LOGGER.info("InitSessionPacket from SERVER to client");
				InitSessionPacket packet = new InitSessionPacket();
				packet.setFrom(FromT.CLIENT);
				packet.setHost(AppHeader.getConfig().getTorDomain());
				packet.setPort(4001);
				ctx.writeAndFlush(packet);
				LOGGER.info("InitSessionPacket from client to SERVER");
			}
		}
		//
	}

}