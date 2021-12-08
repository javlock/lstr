package com.github.javlock.lstr.data.configs;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "settings")
public class AppConfig {

	private @Getter @Setter @DatabaseField(id = true) String uuid = UUID.randomUUID().toString();

	private @Getter @Setter @DatabaseField int serverPort = 49000;

	private @Getter @Setter @DatabaseField int torSocksPort = 10359;
	private @Getter @Setter @DatabaseField String torDomain;

}
