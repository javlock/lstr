package com.github.javlock.lstr.v2.data;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.network.Packet;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.network.handler.client.NetClientHandler;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "AppInfo")
public class AppInfo extends Data {
	private static final long serialVersionUID = -687125762693928112L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger("AppInfo");

	private @Getter @Setter @DatabaseField String username;
	private @Getter @Setter @DatabaseField(id = true) String host;

	private @Getter @Setter @DatabaseField int port = 4001;
	private transient @Getter @Setter ChannelHandlerContext context;// from handler

	private transient @Getter @Setter ChannelFuture channelFuture;// for connect

	private transient @Getter CopyOnWriteArrayList<Message> messages = new CopyOnWriteArrayList<>();

	public AppInfo() {

	}

	public AppInfo(String domain) {
		host = domain;
	}

	public boolean connect() {
		if (itsMe(AppHeader.getConfig().getTorDomain())) {
			return true;
		}
		if (isConnected()) {
			return true;
		} else {
			setChannelFuture(AppHeader.DUMMY);
		}
		LOGGER.info("no me {}", this);
		final AppInfo ttt = this;

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
							p.addLast(new ObjectDecoder(AppHeader.objectForSendNetworkMaxLen,
									ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
							p.addLast(new ObjectEncoder());
							// core

							NetClientHandler handler = new NetClientHandler();
							handler.setInfo(ttt);
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
			setChannelFuture(future);
		} else {
			LOGGER.error("{}", future);
			try {
				disconnect();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gr.shutdownGracefully();
		}

		LOGGER.warn("SIZE:{}", AppHeader.connectionInfoMap.values().stream().filter(AppInfo::isConnected).count());

		return result;
	}

	public void disconnect() throws InterruptedException {
		if (channelFuture != null) {
			channelFuture.channel().closeFuture().await();
		}
		if (context != null) {
			context.close().await();
		}
		setChannelFuture(null);
		setContext(null);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AppInfo)) {
			return false;
		}
		AppInfo other = (AppInfo) obj;
		return Objects.equals(host, other.host) && port == other.port && Objects.equals(username, other.username);
	}

	@Override
	public Object getId() throws UnsupportedOperationException {
		return host;
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port, username);
	}

	public boolean isConnected() {
		return channelFuture != null || context != null;
	}

	public boolean itsMe(String torDomain) {
		return torDomain.equalsIgnoreCase(host);
	}

	public void send(Serializable msg) {
		try {
			if (channelFuture != null) {
				channelFuture.channel().writeAndFlush(msg);
			} else if (context != null) {
				context.channel().writeAndFlush(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AppInfo [");
		if (username != null) {
			builder.append("username=");
			builder.append(username);
			builder.append(", ");
		}
		if (host != null) {
			builder.append("host=");
			builder.append(host);
			builder.append(", ");
		}
		builder.append("port=");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}

}
