package com.github.javlock.lstr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
					AppHeader.app.torBootstrapDomain(string);
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