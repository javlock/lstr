package com.github.javlock.lstr.v2.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.Interfaces.DataBaseInterface;
import com.github.javlock.lstr.v2.data.AppInfo;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBase extends Thread {
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
		public ArrayList<Message> getMessageFor(AppInfo appInfo) throws SQLException {
			ArrayList<Message> answ = new ArrayList<>();
			QueryBuilder<Message, String> queryBuilder = messageDao.queryBuilder();
			Where<Message, String> where = queryBuilder.where();
			where.eq("from", appInfo.getHost()).or().eq("to", appInfo.getHost());

			answ.addAll(queryBuilder.query());

			return answ;
		}

		@Override
		public void init() throws SQLException {
			createSource();
			createDAOs();
			createTables();
			createDefaults();
			loadConfig();
			start();
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
		public void saveAppInfo(AppInfo appInfo) throws SQLException {
			appInfoDao.createOrUpdate(appInfo);
		}

		@Override
		public void saveMessage(Message message) throws SQLException {
			if (!messageDao.idExists(message.getId().toString())) {
				messageDao.create(message);
			}
		}

		@Override
		public void torDomain(String domain) throws SQLException {
			AppConfig config = configDao.queryForId(0);
			config.setTorDomain(domain);
			configDao.update(config);
			LOGGER.info("torDomain:[{}] updated", domain);
		}

		@Override
		public void updateSettings() throws SQLException {
			configDao.update(AppHeader.getConfig());
		}
	};

	@Override
	public void run() {
		do {
			for (AppInfo appInfo : appInfoDao) {
				AppHeader.dataInterface.appInfoFromDataBase(appInfo);
			}

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);// FIXME
	}

}
