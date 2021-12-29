package com.github.javlock.lstr.v2.network.handler.server;

import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.data.AppInfo;
import com.github.javlock.lstr.v2.network.handler.NHandler;

import io.netty.channel.ChannelHandlerContext;

public class NetServerHandler extends NHandler {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		getLOGGER().info("channelActive");

		InitSessionPacket initSessionPacket = new InitSessionPacket();
		initSessionPacket.setFrom(FromT.SERVER);
		initSessionPacket.setHost(AppHeader.getConfig().getTorDomain());
		initSessionPacket.setPort(4001);
		ctx.channel().writeAndFlush(initSessionPacket);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof InitSessionPacket) {
			InitSessionPacket initSessionPacket = (InitSessionPacket) msg;
			String host = initSessionPacket.getHost();
			int port = initSessionPacket.getPort();

			info = AppHeader.CONTACTMODELAPI.addElementIfAbsent(host, i -> new AppInfo());
			info.setPort(port);
			AppHeader.dataInterface.appInfoFromNetServerHandler(info);
			return;
		}

		getLOGGER().info("{} msg:{}", ctx.channel().remoteAddress(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		getLOGGER().info("exceptionCaught");
		// super.exceptionCaught(ctx, cause);

	}

	@Override
	public NHandlerSide getSide() {
		return NHandlerSide.SERVER;
	}

}
