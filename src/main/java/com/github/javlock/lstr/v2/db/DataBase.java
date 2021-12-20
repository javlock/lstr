package com.github.javlock.lstr.v2.db;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.Interfaces.DataBaseInterface;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBase {
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");
	private ConnectionSource connectionSource;

	private Dao<AppInfo, String> appInfoDao;
	public Dao<AppConfig, Integer> configDao;
	private Dao<Message, String> messageDao;

	public final DataBaseInterface DATABASEINTERFACE = new DataBaseInterface() {
		private void createDAOs() throws SQLException {
			appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
			configDao = DaoManager.createDao(connectionSource, AppConfig.class);
			messageDao = DaoManager.createDao(connectionSource, Message.class);
		}

		@Override
		public void createDefaults() throws SQLException {
			if (configDao.countOf() == 0) {
				configDao.create(AppHeader.getConfig());
			}

		}

		private void createSource() throws SQLException {
			if (!AppHeader.DIR.exists()) {
				AppHeader.DIR.mkdirs();
			}
			connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + AppHeader.DATABASFILE.getAbsolutePath());
		}

		private void createTables() throws SQLException {
			TableUtils.createTableIfNotExists(connectionSource, AppConfig.class);
			TableUtils.createTableIfNotExists(connectionSource, AppInfo.class);
			TableUtils.createTableIfNotExists(connectionSource, Message.class);
		}

		@Override
		public void init() throws SQLException {
			createSource();
			createDAOs();
			createTables();
			createDefaults();
			loadConfig();
		}

		private void loadConfig() {
			for (AppConfig config : configDao) {
				if (config != null) {
					AppHeader.setConfig(config);
					break;
				}
			}
		}

		@Override
		public void saveMessage(Message message) throws SQLException {
			if (!messageDao.idExists(message.getId())) {
				messageDao.create(message);
			}
		}

		@Override
		public void torDomain(String domain) throws SQLException {
			AppConfig config = configDao.queryForId(0);
			config.setTorDomain(domain);
			configDao.update(config);
			LOGGER.info("torDomain:[{}] saved", domain);
		}

		@Override
		public void updateSettings() throws SQLException {
			configDao.update(AppHeader.getConfig());
		}
	};

}
