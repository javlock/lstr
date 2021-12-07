package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "AppInfo")
public class AppInfo implements Serializable {
	private static final long serialVersionUID = -687125762693928112L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger("AppInfo");

	private @Getter @Setter @DatabaseField(id = true) String uuid;

	private @Getter CopyOnWriteArrayList<Addr> addrs = new CopyOnWriteArrayList<>();

	public boolean isConnected() {
		for (Addr addr : addrs) {
			if (addr.getContext() != null) {
				LOGGER.info("isActive {}", addr.getContext().channel().isActive());
				LOGGER.info("isOpen {}", addr.getContext().channel().isOpen());
				return true;
			}
		}
		return false;
	}
}
