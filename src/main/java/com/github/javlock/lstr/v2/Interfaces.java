package com.github.javlock.lstr.v2;

import java.io.IOException;
import java.sql.SQLException;

import com.github.javlock.lstr.data.Message;

public class Interfaces {
	public interface AppInteface {
		void start() throws SQLException, IOException;
	}

	public interface DataBaseInterface {
		void createDefaults() throws SQLException;

		void init() throws SQLException;

		void saveMessage(Message message) throws SQLException;

		void torDomain(String domain) throws SQLException;

		void updateSettings() throws SQLException;
	}

	public interface NetworkInterface {

		void init();

		void initBootStrap();

		void initServer();

		void start();

		void startBootStrap();

		void startClient();

		void startServer();

	}

	public interface TorInteface {
		void torInit() throws IOException;

		void torStart();

		void torStarted();
	}

}
