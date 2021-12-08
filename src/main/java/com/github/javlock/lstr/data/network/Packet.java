package com.github.javlock.lstr.data.network;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Packet implements Serializable {
	private static final long serialVersionUID = 2061414776477868289L;
	private @Getter @Setter byte[] data;
}
