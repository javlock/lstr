package com.github.javlock.lstr.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "addrs")
public class Addr implements Serializable {
	private static final long serialVersionUID = -5609871221752761498L;
	private transient @DatabaseField(id = true) long id;
	private @Getter @Setter @DatabaseField String host;
	private @Getter @Setter @DatabaseField int port;
	private transient @Getter @Setter ChannelHandlerContext context;
}
