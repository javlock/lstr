package com.github.javlock.lstr.v1.network.client;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.dummy.ChannelFutureDummy;
import com.github.javlock.lstr.data.network.Packet;
import com.github.javlock.lstr.v1.AppHeader;
import com.github.javlock.lstr.v1.network.client.handler.NetClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;

public class NetClient extends Thread {
	public class NetClientConnector extends Thread {

		private boolean connect(AppInfo appInfo) {
			String host = appInfo.getHost();
			int port = appInfo.getPort();

			if (appInfo.itsMe(AppHeader.getConfig().getTorDomain())) {
				return true;
			}

			if (appInfo.isConnected()) {
				return true;
			} else {
				appInfo.setChannelFuture(dummy);
			}
			LOGGER.info("no me {}", appInfo);

			Bootstrap b = new Bootstrap();
			b.resolver(NoopAddressResolverGroup.INSTANCE);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000);
			b.option(ChannelOption.SO_TIMEOUT, 30000);

			NioEventLoopGroup gr = new NioEventLoopGroup();

			b.group(gr).channel(NioSocketChannel.class)

					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							try {
								ChannelPipeline p = ch.pipeline();

								// proxy
								String proxyHost = "127.0.0.1";
								int proxyPort = AppHeader.getConfig().getTorSocksPort();

								InetSocketAddress proxyAdress = new InetSocketAddress(proxyHost, proxyPort);
								Socks5ProxyHandler proxyHandler1 = new Socks5ProxyHandler(proxyAdress);
								// Socks4ProxyHandler proxyHandler2 = new Socks4ProxyHandler(proxyAdress);
								proxyHandler1.setConnectTimeoutMillis(TimeUnit.SECONDS.toMillis(30));
								p.addLast(proxyHandler1);

								// objects
								p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
										ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
								p.addLast(new ObjectEncoder());
								// core

								NetClientHandler handler = new NetClientHandler();
								handler.setInfo(appInfo);
								p.addLast(handler);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

			ChannelFuture future = b.connect(host, port);
			boolean aaa = future.awaitUninterruptibly(2, TimeUnit.MINUTES);
			System.out.println("NetClient.NetClientConnector.connect(AAA):" + aaa);
			boolean result = future.isSuccess();
			if (result) {
				appInfo.setChannelFuture(future);
			} else {
				LOGGER.error("{}", future);
				try {
					appInfo.disconnect();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
					AppInfo appInfo = entry.getValue();
					connect(appInfo);
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					AppHeader.app.active = false;
					Thread.currentThread().interrupt();
				}
			} // loop
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("NetClient");

	ChannelFutureDummy dummy = new ChannelFutureDummy();

	public final NetClientConnector connector = new NetClientConnector();

	@Override
	public void run() {
		Thread.currentThread().setName("NetClient");

	}

	public void startConnector() {
		connector.start();
	}
}
