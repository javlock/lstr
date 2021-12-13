package com.github.javlock.lstr.network;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.data.network.PingPacket;

import io.netty.channel.ChannelDuplexHandler;
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

	protected @Getter @Setter AppInfo info;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("{}-channelActive", getType(), ctx.channel().remoteAddress());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("{}-channelInactive", getType(), ctx.channel().remoteAddress());
		disconnect(ctx, info.getHost(), info.getPort());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof InitSessionPacket) {
			InitSessionPacket initSessionPacket = (InitSessionPacket) msg;
			String host = initSessionPacket.getHost();
			int port = initSessionPacket.getPort();

			info = AppHeader.connectionInfoMap.computeIfAbsent(host, v -> new AppInfo(host));
			info.setHost(host);
			info.setPort(port);
			info.setContext(ctx);

			AppHeader.app.dataBase.saveAppInfo(info);
			return;
		}

		if (msg instanceof PingPacket) {
			PingPacket ping = (PingPacket) msg;
			if (ping.getFrom().equals(FromT.SERVER)) {
				ping.setFrom(FromT.CLIENT);
				ctx.writeAndFlush(ping);
			}
			return;
		}

		LOGGER.info("{}-channelRead msg:[{}]", getType(), msg);
	}

	private void disconnect(ChannelHandlerContext ctx, String host2, int port2) throws InterruptedException {

		LOGGER.info("info != null: info:{}", info);
		LOGGER.info("info != null: info:HP:{} {}", host2, port2);

		// ctx.close().await();
		if (info != null) {
			info.setContext(null);
			if (info.getChannelFuture() != null) {
				info.setChannelFuture(null);
			}
		} else {
			if (host2 != null) {
				AppInfo infoHost = AppHeader.connectionInfoMap.get(host2);
				infoHost.disconnect();
			}

			for (Entry<String, AppInfo> entry : AppHeader.connectionInfoMap.entrySet()) {
				/*
				 * String entryUUID = entry.getKey(); AppInfo entryVal = entry.getValue();
				 * String entryHost = entryVal.getHost(); int entryPort = entryVal.getPort();
				 *
				 * ChannelHandlerContext context = entryVal.getContext(); ChannelFuture
				 * channelFuture = entryVal.getChannelFuture();
				 *
				 * if (context != null) { LOGGER.info("disconnect:{}",
				 * ctx.channel().remoteAddress() == context.channel().remoteAddress());
				 * LOGGER.info("disconnect 2:{}",
				 * ctx.channel().remoteAddress().equals(context.channel().remoteAddress())); }
				 *
				 * if (host2 != null && entryHost.equals(host2)) {
				 * System.out.println("NetHandler.disconnect():" + entryVal);
				 * entryVal.setContext(null); entryVal.setChannelFuture(null); }
				 */

			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof ProxyConnectException) {
			LOGGER.error("proxyError");
			return;
		}

		LOGGER.error("{}-exceptionCaught", getType(), cause);
	}

}
