package com.github.javlock.lstr.v2;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import com.github.javlock.lstr.v2.Interfaces.AppInteface;

public class App {

	public static void main(String[] args) throws Exception {
		// System.err.println(AppHeader.messagesContactModel);

		AppHeader.NETWORKWORKER.getBootStrap()
				.appendUrl(new URL("https://raw.githubusercontent.com/javlock/lstr/main/infocon/bootstrap"));
		AppHeader.APP.AppInteface.start();
	}

	public final AppInteface AppInteface = new AppInteface() {
		@Override
		public void start() throws SQLException, IOException, InterruptedException {
			AppHeader.GUI.setVisible(true);
			AppHeader.GUI.GUIINTERFACE.start();
			AppHeader.DATABASE.DATABASEINTERFACE.init();
			AppHeader.TOR.TORINTEFACE.torInit();
		}

	};
}
