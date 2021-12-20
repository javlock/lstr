package com.github.javlock.lstr.v1;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.v1.db.DataBase;
import com.github.javlock.lstr.v1.network.client.NetClient;
import com.github.javlock.lstr.v1.network.server.NetServer;
import com.github.javlock.lstr.v1.services.BootStrapRunner;
import com.github.javlock.lstr.v1.services.TorWorker;

public class App extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger("App");

	public static void main(String[] args) {
		try {
			AppHeader.app = new App();
			System.out.println("App.main():" + AppHeader.jarPath);
			AppHeader.app.init();
			AppHeader.app.start();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	public final DataBase dataBase = new DataBase();

	private final BootStrapRunner bootStrapRunner = new BootStrapRunner();
	private final TorWorker torWorker = new TorWorker();
	public final NetClient client = new NetClient();
	private final NetServer server = new NetServer();

	public boolean active = true;

	public boolean torStarted;

	private void init() throws IOException, SQLException {
		dataBase.init();
		server.init();
		torWorker.init();
	}

	@Override
	public void run() {
		dataBase.start();
		server.bind();
		torWorker.start();
		while (!torStarted) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("tor started:->continue");
		bootStrapRunner.start();
		client.startConnector();
		client.start();
		AppHeader.GUI.setVisible(true);
	}

	public void sendBroadCast(Serializable message) {
		AppHeader.connectionInfoMap.values().parallelStream().forEach((var a) -> a.send(message));
	}

	public void torServiceHost(String domain) throws SQLException {
		LOGGER.info("TOR DOMAIN IS {}", domain);
		boolean needWrite = AppHeader.getConfig().getTorDomain() == null
				|| !AppHeader.getConfig().getTorDomain().equals(domain);
		AppHeader.getConfig().setTorDomain(domain);

		if (needWrite) {
			if (dataBase.configDao.countOf() == 0) {// create
				AppConfig newConfig = new AppConfig();
				newConfig.setTorDomain(domain);
				dataBase.configDao.create(newConfig);
			} else {// load
				dataBase.loadConfig();
			}
			dataBase.updateSettings();

		}
	}

}
