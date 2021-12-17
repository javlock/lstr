package com.github.javlock.lstr.data.configs;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "settings")
public class AppConfig {

	private @Getter @Setter @DatabaseField String username;

	private @Getter @Setter @DatabaseField int serverPort = 49000;

	private @Getter @Setter @DatabaseField(id = true) String torDomain;
	private @Getter @Setter @DatabaseField int torSocksPort = 10359;

	private @Getter @Setter @DatabaseField boolean torNeedProxy = false;
	private @Getter @Setter @DatabaseField String torProxyHost = "proxy.example.com";
	private @Getter @Setter @DatabaseField int torProxyPort = 1080;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AppConfig [");
		if (username != null) {
			builder.append("username=");
			builder.append(username);
			builder.append(", ");
		}
		builder.append("serverPort=");
		builder.append(serverPort);
		builder.append(", ");
		if (torDomain != null) {
			builder.append("torDomain=");
			builder.append(torDomain);
			builder.append(", ");
		}
		builder.append("torSocksPort=");
		builder.append(torSocksPort);
		builder.append(", torNeedProxy=");
		builder.append(torNeedProxy);
		builder.append(", ");
		if (torProxyHost != null) {
			builder.append("torProxyHost=");
			builder.append(torProxyHost);
			builder.append(", ");
		}
		builder.append("torProxyPort=");
		builder.append(torProxyPort);
		builder.append("]");
		return builder.toString();
	}

}
