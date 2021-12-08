package com.github.javlock.lstr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.github.javlock.lstr.data.AppInfo;

public class BootStrapRunner extends Thread {

	private static final String BOOTSTRAPURL = "https://raw.githubusercontent.com/javlock/lstr/main/infocon/bootstrap";

	@Override
	public void run() {
		while (AppHeader.app.active) {
			try {
				ArrayList<String> lines = new ArrayList<>();
				URL url = new URL(BOOTSTRAPURL);
				URLConnection yc = url.openConnection();
				try (BufferedReader in = new BufferedReader(
						new InputStreamReader(yc.getInputStream(), StandardCharsets.UTF_8))) {
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						lines.add(inputLine);
					}
				}

				for (String string : lines) {
					String[] ar = string.split(":");
					String uuid = ar[0];
					String host = ar[1];
					int port = 4001;
					if (ar.length == 3) {
						port = Integer.parseInt(ar[2]);
					}

					try {
						AppInfo info = AppHeader.connectionInfoMap.computeIfAbsent(uuid, v -> new AppInfo(uuid));
						info.setHost(host);
						info.setPort(port);
						AppHeader.app.dataBase.saveAppInfo(info);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
		}
	}
}