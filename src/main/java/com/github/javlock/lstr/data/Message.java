package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "messages")
public class Message implements Serializable {
	private static final long serialVersionUID = 313854771560527060L;

	private @Getter @DatabaseField(id = true) String id = UUID.randomUUID().toString();
	private @Getter long timeCreated = System.currentTimeMillis() / 1000;

	private @Getter @Setter @DatabaseField String encMsg;
	private transient @Getter @Setter String rawMsg;// no send

}