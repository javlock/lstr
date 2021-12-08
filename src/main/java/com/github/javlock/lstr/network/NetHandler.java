package com.github.javlock.lstr.network;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.App;
import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.PingPacket;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.dummy.ChannelFutureDummy;
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

	private @Getter @Setter App app;
	private @Getter @Setter HandlerType type = HandlerType.NA;

	protected @Getter @Setter String uuid;
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
					for (Entry<AppInfo, ChannelFuture> entry : AppHeader.connected.entrySet()) {
						AppInfo ent = entry.getKey();
						ChannelFuture val = entry.getValue();

						if (!(val instanceof ChannelFutureDummy)) {
							LOGGER.info("{}", val.channel().writeAndFlush(pingPacket));
						}
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
		app.client.disconnect(ctx, uuid, host, port);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof InitSessionPacket) {
			InitSessionPacket initSessionPacket = (InitSessionPacket) msg;
			uuid = initSessionPacket.getUuid();
			host = initSessionPacket.getHost();
			port = initSessionPacket.getPort();

			System.err.println(1);
			if (initSessionPacket.getFrom().equals(FromT.CLIENT)) {
				InitSessionPacket packet = new InitSessionPacket();
				packet.setFrom(FromT.SERVER);
				packet.setUuid(AppHeader.config.getUuid());
				packet.setHost(AppHeader.config.getTorDomain());
				packet.setPort(4001);
				ctx.writeAndFlush(packet);
				System.err.println(2);
			}

			app.torBootstrapDomain(uuid + ":" + host + ":" + port);
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
