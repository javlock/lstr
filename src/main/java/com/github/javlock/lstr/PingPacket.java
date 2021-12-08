package com.github.javlock.lstr;

import com.github.javlock.lstr.data.network.InitSessionPacket.FromT;
import com.github.javlock.lstr.data.network.Packet;

import lombok.Getter;
import lombok.Setter;

public class PingPacket extends Packet {
	private static final long serialVersionUID = 7555206210304766573L;
	private @Getter @Setter FromT from;

}
