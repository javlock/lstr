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

public class DataBase {
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");

	private ConnectionSource connectionSource;

	private Dao<AppInfo, String> appInfoDao;
	private Dao<AppConfig, String> configDao;

	private void createDAOs() throws SQLException {
		appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		configDao = DaoManager.createDao(connectionSource, AppConfig.class);
	}

	private void createSource() throws SQLException {
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
		if (configDao.countOf() == 0) {// create
			configDao.create(new AppConfig());
		} else {// load
			for (AppConfig config : configDao) {
				if (config != null) {
					AppHeader.config = config;
					break;
				}
			}
		}
	}

	public void saveAppInfo(AppInfo appInfo) throws SQLException {
		String id = appInfo.getUuid();
		if (appInfoDao.idExists(id)) {// update
			AppInfo infoFromDB = appInfoDao.queryForId(id);
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
		configDao.update(AppHeader.config);
	}
}
