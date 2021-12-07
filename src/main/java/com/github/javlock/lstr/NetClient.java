package com.github.javlock.lstr;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Addr;
import com.github.javlock.lstr.data.AppInfo;

public class NetClient {
	class NetClientConnector extends Thread {
		public void appendDomain(String onionDomain) {
			String[] ar = onionDomain.split(":");
			String uuid = ar[0];
			String host = ar[1];
			int port = 4001;
			if (ar.length == 3) {
				port = Integer.parseInt(ar[2]);
			}

			AppInfo info = connectionInfoMap.computeIfAbsent(uuid, v -> new AppInfo(uuid));

			Addr addr = new Addr();
			addr.setHost(host);
			addr.setPort(port);

			info.getAddrs().addIfAbsent(addr);
		}

		private boolean connect(Addr addr) {
			String host = addr.getHost();
			int port = addr.getPort();

			LOGGER.info("connect: host:{} port:{}", host, port);
			return false;// TODO REWRITE TO RESULT
		}

		@Override
		public void run() {
			Thread.currentThread().setName("NetClientConnector");

			while (app.active) { // loop

				for (Entry<String, AppInfo> entry : connectionInfoMap.entrySet()) {
					String key = entry.getKey();
					AppInfo appInfo = entry.getValue();

					if (appInfo.isConnected()) {
						LOGGER.info("isConnected");
						continue;
					}
					boolean connected = false;
					connectedLabel: {
						LOGGER.info("connectedLabel");
						if (!connected) {
							LOGGER.info("!connected");

							for (Addr addr : appInfo.getAddrs()) {
								if (connect(addr)) {
									connected = true;
									break connectedLabel;
								} // TODO NOT CONNECTED
							}
						} else {
							LOGGER.info("!connected ELSE");
						}
					}
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // loop
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("NetClient");
	private final ConcurrentHashMap<String, AppInfo> connectionInfoMap = new ConcurrentHashMap<>();

	final NetClientConnector connector = new NetClientConnector();

	private App app;

	public NetClient(App app) {
		this.app = app;
	}

	public void startConnector() {
		connector.start();
	}
}
