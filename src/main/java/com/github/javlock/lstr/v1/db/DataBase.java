package com.github.javlock.lstr.v1.db;

import java.io.File;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.data.network.AppInfoMaili;
import com.github.javlock.lstr.v1.AppHeader;
import com.github.javlock.lstr.v2.data.AppInfo;
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
	private Dao<Message, String> messageDao;

	private void createDAOs() throws SQLException {
		appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		configDao = DaoManager.createDao(connectionSource, AppConfig.class);
		messageDao = DaoManager.createDao(connectionSource, Message.class);

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
		TableUtils.createTableIfNotExists(connectionSource, Message.class);
	}

	private void getMessageForMeAnd(String domain) throws SQLException {
		String myDomain = AppHeader.getConfig().getTorDomain();
		if (myDomain == null) {
			return;
		}
		AppInfo domainInfo = AppHeader.connectionInfoMap.get(domain);
		if (domainInfo == null) {
			return;
		}
		AppInfo myInfo = AppHeader.connectionInfoMap.get(myDomain);
		if (myInfo == null) {
			return;
		}

		for (Message message : messageDao) {
			boolean forMe = message.getFrom().equals(myDomain) || message.getTo().equals(myDomain);
			if (forMe) {
				boolean fordomain = message.getFrom().equals(domain) || message.getTo().equals(domain);
				if (fordomain) {

					domainInfo.getMessages().addIfAbsent(message);
					myInfo.getMessages().addIfAbsent(message);
					// GUI
					AppHeader.GUI.updateMessages();
					domainInfo.send(message);
				}
			}

		}

	}

	public void init() throws SQLException {
		createSource();
		createDAOs();
		createTables();
	}

	public void loadConfig() {
		LOGGER.info("old config:{}", AppHeader.getConfig());
		for (AppConfig config : configDao) {
			if (config != null) {
				AppHeader.setConfig(config);
				break;
			}
		}
		LOGGER.info("new config:{}", AppHeader.getConfig());

	}

	@Override
	public void run() {
		while (AppHeader.app.active) {

			for (AppInfo appInfo : appInfoDao) {
				String domain = appInfo.getHost();
				try {
					getMessageForMeAnd(domain);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// gui
				AppHeader.GUI.receiveAppInfo(appInfo);
				// network
				AppHeader.connectionInfoMap.putIfAbsent(domain, appInfo);
				AppInfoMaili aim = new AppInfoMaili(domain);
				AppHeader.app.sendBroadCast(aim);
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

	public void saveMessage(Message message) throws SQLException {
		if (!messageDao.idExists(message.getId())) {
			messageDao.create(message);
		}
	}

	public void updateSettings() throws SQLException {
		configDao.update(AppHeader.getConfig());
	}
}
