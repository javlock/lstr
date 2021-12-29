package com.github.javlock.lstr.v2.data;

import java.io.Serializable;

public class Data implements Serializable {

	private static final long serialVersionUID = -1135300627113405425L;

	public Object getId() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(String.format("impl getId() for class %s", getClass().getSimpleName()));
	}
}
