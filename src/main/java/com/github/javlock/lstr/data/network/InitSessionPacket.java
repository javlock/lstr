package com.github.javlock.lstr.data.network;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class InitSessionPacket implements Serializable {
	public enum FromT {
		CLIENT, SERVER
	}

	private static final long serialVersionUID = -3222740463606912483L;
	private @Getter @Setter String uuid;
	private @Getter @Setter String host;
	private @Getter @Setter int port;
	private @Getter @Setter FromT from;

}
