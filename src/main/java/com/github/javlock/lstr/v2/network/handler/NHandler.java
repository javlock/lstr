package com.github.javlock.lstr.v2.network.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.v2.data.AppInfo;

import io.netty.channel.ChannelDuplexHandler;
import lombok.Getter;
import lombok.Setter;

public class NHandler extends ChannelDuplexHandler {
	public enum NHandlerSide {
		SERVER, CLIENT, BOTH
	}

	protected @Getter @Setter AppInfo info;
	protected @Getter NHandlerSide side;

	public NHandler() {
		getLOGGER();
		side = NHandlerSide.BOTH;
	}

	public Logger getLOGGER() {
		return LoggerFactory.getLogger(getSide().name());
	}
}
