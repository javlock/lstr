package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "AppInfo")
public class AppInfo implements Serializable {
	private static final long serialVersionUID = -687125762693928112L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger("AppInfo");

	private @Getter @Setter @DatabaseField(id = true) String uuid;

	private @Getter @Setter @DatabaseField String username;
	private @Getter @Setter @DatabaseField String host;
	private @Getter @Setter @DatabaseField int port;
	private transient @Getter @Setter ChannelHandlerContext context;

	public AppInfo() {
	}

	public AppInfo(String id) {
		uuid = id;
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
		return Objects.equals(host, other.host) && port == other.port && Objects.equals(uuid, other.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port, uuid);
	}

	public boolean isConnected() {
		if (context != null) {
			LOGGER.info("isActive {}", context.channel().isActive());
			LOGGER.info("isOpen {}", context.channel().isOpen());
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AppInfo [");
		if (uuid != null) {
			builder.append("uuid=");
			builder.append(uuid);
			builder.append(", ");
		}
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
		builder.append(", ");
		if (context != null) {
			builder.append("context=");
			builder.append(context);
		}
		builder.append("]");
		return builder.toString();
	}
}
