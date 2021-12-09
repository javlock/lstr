package com.github.javlock.lstr.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.PingPacket;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.proxy.ProxyConnectException;
import lombok.Getter;
import lombok.Setter;

public class NetHandler extends ChannelDuplexHandler {
	public enum HandlerType {
		NA, SERVER, CLIENT
	}

	protected static final Logger LOGGER = LoggerFactory.getLogger("NetHandler");

	private @Getter @Setter HandlerType type = HandlerType.NA;

	protected @Getter @Setter String host;
	protected @Getter @Setter int port;

	PingPacket pingPacket = new PingPacket();
	boolean pingActive = true;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("{}-channelActive", getType(), ctx.channel().remoteAddress());
		if (getType().equals(HandlerType.SERVER)) {
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

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("{}-channelInactive", getType(), ctx.channel().remoteAddress());
		AppHeader.app.client.disconnect(ctx, host, port);
		pingActive = false;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof InitSessionPacket) {
			InitSessionPacket initSessionPacket = (InitSessionPacket) msg;
			host = initSessionPacket.getHost();
			port = initSessionPacket.getPort();

			System.err.println(1);
			if (initSessionPacket.getFrom().equals(FromT.CLIENT)) {
				InitSessionPacket packet = new InitSessionPacket();
				packet.setFrom(FromT.SERVER);
				packet.setHost(AppHeader.getConfig().getTorDomain());
				packet.setPort(4001);
				ctx.writeAndFlush(packet);
				System.err.println(2);
			}

			AppInfo info = AppHeader.connectionInfoMap.computeIfAbsent(host, v -> new AppInfo(host));
			info.setHost(host);
			info.setPort(port);
			AppHeader.app.dataBase.saveAppInfo(info);
			return;
		}
		if (msg instanceof PingPacket) {
			PingPacket ping = (PingPacket) msg;
			if (ping.getFrom().equals(FromT.CLIENT)) {
				ping.setFrom(FromT.SERVER);
				ctx.writeAndFlush(ping);
			}
			return;
		}

		LOGGER.info("{}-channelRead msg:[{}]", getType(), msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof ProxyConnectException) {
			ProxyConnectException proxyConnectException = (ProxyConnectException) cause;
			LOGGER.error(proxyConnectException.getClass().getSimpleName());
			return;
		}

		LOGGER.error("{}-exceptionCaught", getType(), cause);
	}
}
