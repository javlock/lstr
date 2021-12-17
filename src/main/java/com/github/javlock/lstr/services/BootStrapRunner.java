package com.github.javlock.lstr.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.github.javlock.lstr.AppHeader;
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

				for (String host : lines) {
					try {
						AppInfo info = AppHeader.connectionInfoMap.computeIfAbsent(host, v -> new AppInfo(host));
						info.setHost(host);
						info.setPort(4001);
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