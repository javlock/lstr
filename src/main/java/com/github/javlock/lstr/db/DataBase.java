package com.github.javlock.lstr.db;

import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.EncryptKey;
import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBase extends Thread {
	private static final String CONTACT2 = "contact2";
	private static final String CONTACT1 = "contact1";
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");
	private ConnectionSource connectionSource;

	private Dao<AppInfo, String> appInfoDao;

	public Dao<AppConfig, String> configDao;
	private Dao<EncryptKey, Integer> keysDao;
	private Dao<Message, String> messageDao;

	private void createDAOs() throws SQLException {
		appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		configDao = DaoManager.createDao(connectionSource, AppConfig.class);
		keysDao = DaoManager.createDao(connectionSource, EncryptKey.class);
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
		TableUtils.createTableIfNotExists(connectionSource, EncryptKey.class);
		TableUtils.createTableIfNotExists(connectionSource, Message.class);
	}

	public EncryptKey getKeyFor(AppInfo contact, String hostContact2) throws SQLException {
		QueryBuilder<EncryptKey, Integer> queryBuilder = keysDao.queryBuilder();
		Where<EncryptKey, Integer> where = queryBuilder.where();
		// 1
		where.eq(CONTACT1, contact.getHost()).and().eq(CONTACT2, hostContact2)
				//
				.or()
				// 2
				.eq(CONTACT2, contact.getHost()).and().eq(CONTACT1, hostContact2);

		return queryBuilder.queryForFirst();
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

					try {
						message.repare(messageDao, myInfo, domainInfo);
					} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
							| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
							| NoSuchPaddingException | SQLException e) {
						e.printStackTrace();
					}

					domainInfo.getMessages().addIfAbsent(message);
					myInfo.getMessages().addIfAbsent(message);
					// GUI
					AppHeader.GUI.updateMessages();
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

			try {
				if (AppHeader.getConfig().getTorDomain() != null) {
					QueryBuilder<EncryptKey, Integer> queryBuilder = keysDao.queryBuilder();
					Where<EncryptKey, Integer> where = queryBuilder.where();
					where.eq(CONTACT1, AppHeader.getConfig().getTorDomain()).and().eq(CONTACT2,
							AppHeader.getConfig().getTorDomain());

					if (queryBuilder.countOf() == 0) {
						EncryptKey encryptKey = new EncryptKey();
						encryptKey.setContact1(AppHeader.getConfig().getTorDomain());
						encryptKey.setContact2(AppHeader.getConfig().getTorDomain());
						keysDao.create(encryptKey);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

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

		//

		if (AppHeader.getConfig().getTorDomain() != null) {
			QueryBuilder<EncryptKey, Integer> queryBuilder = keysDao.queryBuilder();
			Where<EncryptKey, Integer> where = queryBuilder.where();
			where.eq(CONTACT1, AppHeader.getConfig().getTorDomain()).and().eq(CONTACT2,
					AppHeader.getConfig().getTorDomain());
			List<EncryptKey> keys = queryBuilder.query();
			if (keys.isEmpty()) {
				EncryptKey encryptKey = new EncryptKey();
				encryptKey.setContact1(AppHeader.getConfig().getTorDomain());
				encryptKey.setContact2(AppHeader.getConfig().getTorDomain());
				keysDao.create(encryptKey);
			}
		}

	}
}
