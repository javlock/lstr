package com.github.javlock.lstr.data.network;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class AppInfoMaili implements Serializable {

	private static final long serialVersionUID = 453459240807123224L;
	private @Getter @Setter String domain;

	public AppInfoMaili(String domain) {
		this.domain = domain;
	}

}
