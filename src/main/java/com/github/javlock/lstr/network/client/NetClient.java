package com.github.javlock.lstr.network.client;

import java.net.InetSocketAddress;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.App;
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
			String uuid = appInfo.getUuid();
			String host = appInfo.getHost();
			int port = appInfo.getPort();

			if (AppHeader.connected.containsKey(appInfo)) {
				return true;
			} else {
				AppHeader.connected.put(appInfo, dummy);
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
								int proxyPort = AppHeader.config.getTorSocksPort();
								// proxy
								p.addLast(new Socks4ProxyHandler(new InetSocketAddress(proxyHost, proxyPort)));
								// objects
								p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
										ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
								p.addLast(new ObjectEncoder());
								// core

								NetClientHandler handler = new NetClientHandler();
								handler.setUuid(uuid);
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

				AppHeader.connected.replace(appInfo, dummy, future);
				InitSessionPacket packet = new InitSessionPacket();
				packet.setFrom(FromT.CLIENT);
				packet.setUuid(AppHeader.config.getUuid());
				packet.setHost(AppHeader.config.getTorDomain());
				packet.setPort(4001);
				future.channel().writeAndFlush(packet);
			} else {
				LOGGER.error("{}", future);
				AppHeader.connected.remove(appInfo);
				gr.shutdownGracefully();
			}

			LOGGER.warn("SIZE:{}", AppHeader.connected.size());
			for (Entry<AppInfo, ChannelFuture> entry : AppHeader.connected.entrySet()) {
				AppInfo key = entry.getKey();
				ChannelFuture val = entry.getValue();
				LOGGER.warn("[{}]:[{}]", key, val);
			}

			return result;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("NetClientConnector");

			while (app.active) { // loop

				for (Entry<String, AppInfo> entry : AppHeader.connectionInfoMap.entrySet()) {
					String uuid = entry.getKey();
					AppInfo appInfo = entry.getValue();

					if (appInfo.isConnected()) {
						LOGGER.info("isConnected");
						continue;
					}
					boolean connected = false;
					connectedLabel: {
						if (!connected) {
							if (connect(appInfo)) {
								connected = true;
								break connectedLabel;
							}
						} else {
							LOGGER.info("!connected ELSE");
						}
					}
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

	private App app;

	public NetClient(App app) {
		this.app = app;
	}

	public void disconnect(ChannelHandlerContext ctx, String uuid, String host, int port) {
		LOGGER.info("id:{} host:{} port:{}", uuid, host, port);
		LOGGER.info("connected:{}", AppHeader.connected.size());
		for (Entry<AppInfo, ChannelFuture> entry : AppHeader.connected.entrySet()) {
			AppInfo ent = entry.getKey();
			ChannelFuture val = entry.getValue();

			if (host != null) {
				if (ent.getHost().equals(host)) {
					AppHeader.connected.remove(ent, val);
				}
			}

		}
		LOGGER.info("connected after:{}", AppHeader.connected.size());
	}

	@Override
	public void run() {
		Thread.currentThread().setName("NetClient");

	}

	public void startConnector() {
		connector.start();
	}
}
