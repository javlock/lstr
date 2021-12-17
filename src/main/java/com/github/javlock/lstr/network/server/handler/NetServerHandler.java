package com.github.javlock.lstr.network.server.handler;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.data.network.PingPacket;
import com.github.javlock.lstr.network.NetHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class NetServerHandler extends NetHandler {
	private PingPacket pingPacket = new PingPacket();
	private boolean pingActive = true;

	public NetServerHandler() {
		setType(HandlerType.SERVER);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		sendInitPacket(ctx);
		startPing(ctx);
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		pingActive = false;
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	private void sendInitPacket(ChannelHandlerContext ctx) {
		InitSessionPacket packet = new InitSessionPacket();
		packet.setFrom(FromT.SERVER);
		packet.setHost(AppHeader.getConfig().getTorDomain());
		packet.setPort(4001);
		ctx.channel().writeAndFlush(packet);
	}

	private void startPing(ChannelHandlerContext ctx) {
		pingPacket.setFrom(FromT.SERVER);
		new Thread((Runnable) () -> {
			while (pingActive) {
				try {
					ChannelFuture result = ctx.channel().writeAndFlush(pingPacket).await();
					if (!result.isSuccess()) {
						Throwable cause = result.cause();
						LOGGER.error("cause:", cause);
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}, Thread.currentThread().getName() + "-PING").start();

	}

}
