package com.github.javlock.lstr.data.network;

import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;

import lombok.Getter;
import lombok.Setter;

public class PingPacket extends Packet {
	private static final long serialVersionUID = 7555206210304766573L;
	private @Getter @Setter FromT from;

}
