package com.github.javlock.lstr.v2.network;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.network.Packet;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.Interfaces.NetworkInterface;
import com.github.javlock.lstr.v2.network.server.handler.NetServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;

public class NetworkWorker {
	public class BootStrap extends Thread {
		public void appendUrl(String string) throws MalformedURLException {
			appendUrl(new URL(string));
		}

		public void appendUrl(URL uri) {
			// TODO Auto-generated method stub

		}

		@Override
		public void run() {
			do {

				try {
					Thread.sleep(45000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);// FIXME
		}
	}

	class Client extends Thread {
		private final Logger LOGGER = LoggerFactory.getLogger("Client");

		@Override
		public void run() {
			do {
				for (Entry<String, AppInfo> entry : AppHeader.connectionInfoMap.entrySet()) {
					AppInfo appInfo = entry.getValue();
					appInfo.connect();
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);// FIXME

		}
	}

	private @Getter BootStrap bootStrap = new BootStrap();

	private final Logger LOGGER = LoggerFactory.getLogger("NetworkWorker");

	private final Client client = new Client();

	public final NetworkInterface networkInterface = new NetworkInterface() {

		private ChannelFuture bindChannelFuture;
		private ServerBootstrap serverBootstrap = new ServerBootstrap();
		private EventLoopGroup serverWorkgroup = new NioEventLoopGroup();

		@Override
		public void init() {
			LOGGER.info("init-start");
			networkInterface.initServer();
			networkInterface.initBootStrap();
			LOGGER.info("init-end");
		}

		@Override
		public void initBootStrap() {
			LOGGER.info("initBootStrap");
		}

		@Override
		public void initServer() {
			LOGGER.info("initServer-start");
			serverBootstrap.group(serverWorkgroup).channel(NioServerSocketChannel.class)
					.localAddress(new InetSocketAddress(AppHeader.getConfig().getServerPort()));
			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();

					p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
							ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
					p.addLast(new ObjectEncoder());

					NetServerHandler handler = new NetServerHandler();
					p.addLast(handler);
				}
			});
			LOGGER.info("initServer-end");
		}

		@Override
		public void start() {
			LOGGER.info("start-start");
			networkInterface.startServer();
			networkInterface.startClient();
			networkInterface.startBootStrap();
			LOGGER.info("start-end");
		}

		@Override
		public void startBootStrap() {
			LOGGER.info("startBootStrap-start");
			bootStrap.start();
			LOGGER.info("startBootStrap-end");
		}

		@Override
		public void startClient() {
			LOGGER.info("startClient-start");
			client.start();
			LOGGER.info("startClient-end");
		}

		@Override
		public void startServer() {
			LOGGER.info("startServer-start");
			bindChannelFuture = serverBootstrap.bind().awaitUninterruptibly();
			LOGGER.info("startServer-end");
		}

	};
}
