package com.github.javlock.lstr.v2.network.handler;

import com.github.javlock.lstr.v2.data.AppInfo;

import io.netty.channel.ChannelDuplexHandler;
import lombok.Getter;
import lombok.Setter;

public class NHandler extends ChannelDuplexHandler {
	public enum NHandlerSide {
		SERVER, CLIENT, BOTH
	}

	private @Getter @Setter AppInfo info;
	protected @Getter NHandlerSide side;

	public NHandler() {
		side = NHandlerSide.BOTH;
	}
}
