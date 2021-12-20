package com.github.javlock.lstr.v2;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import com.github.javlock.lstr.v2.Interfaces.AppInteface;

public class App {

	public static void main(String[] args) throws Exception {
		AppHeader.NETWORKWORKER.getBootStrap().appendUrl(new URL(""));
		AppHeader.APP.AppInteface.start();
	}

	public final AppInteface AppInteface = new AppInteface() {
		@Override
		public void start() throws SQLException, IOException {
			AppHeader.DATABASE.DATABASEINTERFACE.init();
			AppHeader.TOR.TORINTEFACE.torInit();
		}

	};
}
