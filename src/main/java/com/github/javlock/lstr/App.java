package com.github.javlock.lstr;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.db.DataBase;
import com.github.javlock.lstr.network.client.NetClient;
import com.github.javlock.lstr.network.server.NetServer;
import com.github.javlock.lstr.services.TorWorker;

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

	private final DataBase dataBase = new DataBase();

	private final BootStrapRunner bootStrapRunner = new BootStrapRunner();
	private final TorWorker torWorker = new TorWorker();
	public final NetClient client = new NetClient(this);
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
	}

	public void torBootstrapDomain(String onionDomain) {
		client.connector.appendDomain(onionDomain);
	}

	public void torServiceHost(String domain) throws SQLException {
		LOGGER.info("TOR DOMAIN IS {}", domain);
		boolean needWrite = AppHeader.config.getTorDomain() == null || !AppHeader.config.getTorDomain().equals(domain);
		AppHeader.config.setTorDomain(domain);
		if (needWrite) {
			dataBase.updateSettings();
		}
	}

}
