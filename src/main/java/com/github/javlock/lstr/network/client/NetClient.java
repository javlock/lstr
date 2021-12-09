package com.github.javlock.lstr.network.client;

import java.net.InetSocketAddress;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.dummy.ChannelFutureDummy;
import com.github.javlock.lstr.data.network.InitSessionPacket;
import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.data.network.Packet;
import com.github.javlock.lstr.network.client.handler.NetClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;

public class NetClient extends Thread {
	public class NetClientConnector extends Thread {

		private boolean connect(AppInfo appInfo) {
			String host = appInfo.getHost();
			int port = appInfo.getPort();

			if (appInfo.isConnected()) {
				return true;
			} else {
				appInfo.setChannelFuture(dummy);
			}

			Bootstrap b = new Bootstrap();
			b.resolver(NoopAddressResolverGroup.INSTANCE);

			NioEventLoopGroup gr = new NioEventLoopGroup();
			b.group(gr).channel(NioSocketChannel.class)

					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							try {
								ChannelPipeline p = ch.pipeline();

								String proxyHost = "127.0.0.1";
								int proxyPort = AppHeader.getConfig().getTorSocksPort();
								// proxy
								p.addLast(new Socks4ProxyHandler(new InetSocketAddress(proxyHost, proxyPort)));
								// objects
								p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
										ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
								p.addLast(new ObjectEncoder());
								// core

								NetClientHandler handler = new NetClientHandler();
								handler.setHost(host);
								handler.setPort(port);
								p.addLast(handler);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

			ChannelFuture future = b.connect(host, port).awaitUninterruptibly();
			boolean result = future.isSuccess();
			LOGGER.info("connection to {}:{} isSuccess?:{}", host, port, result);

			if (result) {
				appInfo.setChannelFuture(future);

				InitSessionPacket packet = new InitSessionPacket();
				packet.setFrom(FromT.CLIENT);
				packet.setHost(AppHeader.getConfig().getTorDomain());
				packet.setPort(4001);
				future.channel().writeAndFlush(packet);
			} else {
				LOGGER.error("{}", future);
				appInfo.setChannelFuture(null);
				gr.shutdownGracefully();
			}

			LOGGER.warn("SIZE:{}", AppHeader.connectionInfoMap.values().stream().filter(AppInfo::isConnected).count());

			return result;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("NetClientConnector");

			while (AppHeader.app.active) { // loop

				for (Entry<String, AppInfo> entry : AppHeader.connectionInfoMap.entrySet()) {
					String uuid = entry.getKey();
					AppInfo appInfo = entry.getValue();

					if (appInfo.isConnected()) {
						LOGGER.info("isConnected");
						continue;
					}
					connect(appInfo);
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // loop
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("NetClient");

	ChannelFutureDummy dummy = new ChannelFutureDummy();

	public final NetClientConnector connector = new NetClientConnector();

	public void disconnect(ChannelHandlerContext ctx, String host, int port) {
		LOGGER.info("host:{} port:{}", host, port);

		for (Entry<String, AppInfo> entry : AppHeader.connectionInfoMap.entrySet()) {
			String entryUUID = entry.getKey();
			AppInfo entryVal = entry.getValue();

			ChannelHandlerContext context = entryVal.getContext();
			ChannelFuture channelFuture = entryVal.getChannelFuture();

			String entryHost = entryVal.getHost();
			int entryPort = entryVal.getPort();

			LOGGER.info("disconnect:{}", ctx == context);
			// FIXME

			/*
			 * if (host != null && ent.getHost().equals(host) &&
			 * val.channel().remoteAddress().equals(ctx.channel().remoteAddress())) {
			 * AppHeader.connected.remove(ent, val); break; }
			 */
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("NetClient");

	}

	public void startConnector() {
		connector.start();
	}
}
