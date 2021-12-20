package com.github.javlock.lstr.v1.network.server;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.network.Packet;
import com.github.javlock.lstr.v1.AppHeader;
import com.github.javlock.lstr.v1.network.server.handler.NetServerHandler;

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

public class NetServer {
	private static @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
	private static @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();
	private static final Logger LOGGER = LoggerFactory.getLogger("NetServer");
	private ChannelFuture bindChannelFuture;

	public void bind() {
		bindChannelFuture = serverBootstrap.bind();
	}

	public void init() {
		LOGGER.info("initNetwork-init");

		while (AppHeader.getConfig() == null) {
			System.out.println("NetServer.init(sleep)");
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

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
		LOGGER.info("initNetwork-init-end");
	}

}