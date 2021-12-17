package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "messages")
public class Message implements Serializable {

	private static final long serialVersionUID = 313854771560527060L;

	private @Getter @DatabaseField(id = true) String id = UUID.randomUUID().toString();

	private @Getter @DatabaseField long timeCreated = System.currentTimeMillis() / 1000;
	private @Getter @DatabaseField boolean delivered;
	private @Getter @DatabaseField long timeDelivery;

	private @Getter @Setter @DatabaseField String rawMsg;
	private @Getter @Setter @DatabaseField String from;
	private @Getter @Setter @DatabaseField String to;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Message)) {
			return false;
		}
		Message other = (Message) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}