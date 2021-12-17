package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "AppInfo")
public class AppInfo implements Serializable {
	private static final long serialVersionUID = -687125762693928112L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger("AppInfo");

	private @Getter @Setter @DatabaseField String username;
	private @Getter @Setter @DatabaseField(id = true) String host;
	private @Getter @Setter @DatabaseField int port;

	private transient @Getter @Setter ChannelHandlerContext context;// from handler
	private transient @Getter @Setter ChannelFuture channelFuture;// for connect

	private transient @Getter CopyOnWriteArrayList<Message> messages = new CopyOnWriteArrayList<>();

	public AppInfo() {
	}

	public AppInfo(String domain) {
		host = domain;
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
