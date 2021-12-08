package com.github.javlock.lstr.network.client.handler;

import com.github.javlock.lstr.network.NetHandler;

public class NetClientHandler extends NetHandler {
	public NetClientHandler() {
		setType(HandlerType.CLIENT);
	}

	public NetClientHandler(String uuid, String host, int port) {
		this.uuid = uuid;
		this.host = host;
		this.port = port;
		setType(HandlerType.CLIENT);
	}
}