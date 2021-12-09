package com.github.javlock.lstr.db;

import java.io.File;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBase extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");
	private ConnectionSource connectionSource;

	private Dao<AppInfo, String> appInfoDao;

	public Dao<AppConfig, String> configDao;

	private void createDAOs() throws SQLException {
		appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		configDao = DaoManager.createDao(connectionSource, AppConfig.class);
	}

	private void createSource() throws SQLException {
		if (!AppHeader.DIR.exists()) {
			AppHeader.DIR.mkdirs();
		}
		connectionSource = new JdbcConnectionSource(
				"jdbc:sqlite:" + new File(AppHeader.DIR, "database.db").getAbsolutePath());
	}

	private void createTables() throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, AppConfig.class);
		TableUtils.createTableIfNotExists(connectionSource, AppInfo.class);
	}

	public void init() throws SQLException {
		createSource();
		createDAOs();
		createTables();
	}

	@Override
	public void run() {
		while (AppHeader.app.active) {
			for (AppInfo appInfo : appInfoDao) {
				// gui
				AppHeader.GUI.receiveAppInfo(appInfo);
				// network
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveAppInfo(AppInfo appInfo) throws SQLException {
		String host = appInfo.getHost();// id

		if (appInfoDao.idExists(host)) {// update
			AppInfo infoFromDB = appInfoDao.queryForId(host);

			boolean needUpdate = false;

			if (infoFromDB.getPort() != appInfo.getPort()) {
				infoFromDB.setPort(appInfo.getPort());
				needUpdate = true;
			}

			if (appInfo.getUsername() != null
					&& (infoFromDB.getUsername() == null || infoFromDB.getUsername().equals(appInfo.getUsername()))) {
				infoFromDB.setUsername(appInfo.getUsername());
				needUpdate = true;
			}

			if (needUpdate) {
				appInfoDao.update(infoFromDB);
			}
		} else {// create
			appInfoDao.create(appInfo);
		}
	}

	public void updateSettings() throws SQLException {
		configDao.update(AppHeader.getConfig());
	}
}
