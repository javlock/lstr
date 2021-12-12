package com.github.javlock.lstr.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "keys")
public class EncryptKey implements Serializable {
	private static final long serialVersionUID = 8807455117933933833L;

	private transient @DatabaseField(generatedId = true) int id;

	private @Getter @Setter @DatabaseField String salt = genSalt();
	private @Getter @Setter @DatabaseField String key = genPass();

	private @Getter @Setter @DatabaseField String contact1;
	private @Getter @Setter @DatabaseField String contact2;

	public EncryptKey() {

	}

	public void check() {
		if (key == null) {
			throw new NullPointerException("key == null");
		}
		if (contact1 == null) {
			throw new NullPointerException("contact1 == null");
		}
		if (contact2 == null) {
			throw new NullPointerException("contact2 == null");
		}
	}

	private String genPass() {
		// FIXME write real passwd, NO TIME
		return Long.toString(System.currentTimeMillis());
	}

	private String genSalt() {
		// FIXME write real salt, NO TIME
		StringBuilder builder = new StringBuilder();
		String aaaa = Long.toHexString(System.currentTimeMillis());
		while (builder.length() <= 32) {
			builder.append(aaaa);
		}
		String sss = builder.substring(0, 32);

		System.err.println("[" + sss + "]:" + sss.length());

		return sss;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EncryptKey [");
		if (key != null) {
			builder.append("key=");
			builder.append("exist");
			builder.append(", ");
		}
		if (contact1 != null) {
			builder.append("contact1=");
			builder.append(contact1);
			builder.append(", ");
		}
		if (contact2 != null) {
			builder.append("contact2=");
			builder.append(contact2);
		}
		builder.append("]");
		return builder.toString();
	}
}
