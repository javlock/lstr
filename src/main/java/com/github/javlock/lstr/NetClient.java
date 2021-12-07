package com.github.javlock.lstr;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.App.NetClientHandler;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.dummy.ChannelFutureDummy;

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

public class NetClient {
	class NetClientConnector extends Thread {
		public void appendDomain(String onionDomain) {
			String[] ar = onionDomain.split(":");
			String uuid = ar[0];
			String host = ar[1];
			int port = 4001;
			if (ar.length == 3) {
				port = Integer.parseInt(ar[2]);
			}

			AppInfo info = connectionInfoMap.computeIfAbsent(uuid, v -> new AppInfo(uuid));
			info.setHost(host);
			info.setPort(port);
		}

		private boolean connect(AppInfo appInfo) {
			String host = appInfo.getHost();
			int port = appInfo.getPort();

			ChannelFuture future = dummy;

			if (connected.containsKey(appInfo)) {
				return true;
			} else {
				connected.put(appInfo, future);
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
								int proxyPort = app.config.torConfig.socksPort;
								// proxy
								p.addLast(new Socks4ProxyHandler(new InetSocketAddress(proxyHost, proxyPort)));
								// objects
								p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
										ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
								p.addLast(new ObjectEncoder());
								// core
								p.addLast(new NetClientHandler(appInfo.getUuid(), host, port));

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

			future = b.connect(host, port).awaitUninterruptibly();
			boolean result = future.isSuccess();
			LOGGER.info("connection to {}:{} isSuccess?:{}", host, port, result);
			if (result) {
				connected.replace(appInfo, dummy, future);
			} else {
				System.err.println(future);
				connected.remove(appInfo);
				gr.shutdownGracefully();
			}

			return result;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("NetClientConnector");

			while (app.active) { // loop

				for (Entry<String, AppInfo> entry : connectionInfoMap.entrySet()) {
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
							} // TODO NOT CONNECTED
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
	private final ConcurrentHashMap<String, AppInfo> connectionInfoMap = new ConcurrentHashMap<>();;
	private final ConcurrentHashMap<AppInfo, ChannelFuture> connected = new ConcurrentHashMap<>();

	final NetClientConnector connector = new NetClientConnector();

	private App app;

	public NetClient(App app) {
		this.app = app;
	}

	public void disconnect(ChannelHandlerContext ctx, String uuid, String host, int port) {
		LOGGER.info("id:{} host:{} port:{}", uuid, host, port);
		LOGGER.info("connected:{}", connected.size());
		for (Entry<AppInfo, ChannelFuture> entry : connected.entrySet()) {
			AppInfo ent = entry.getKey();
			ChannelFuture val = entry.getValue();

			if (host != null) {
				if (ent.getHost().equals(host)) {
					connected.remove(ent, val);
				}
			}

		}
		LOGGER.info("connected after:{}", connected.size());
	}

	public void startConnector() {
		connector.start();
	}
}
