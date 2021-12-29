package com.github.javlock.lstr.v2;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.v2.data.AppInfo;

public class Interfaces {
	public interface AppInteface {
		void start() throws SQLException, IOException, InterruptedException;
	}

	public interface DataBaseInterface {
		void createDefaults() throws SQLException;

		ArrayList<Message> getMessageFor(AppInfo messagesSelectedAppInfo) throws SQLException;

		void init() throws SQLException;

		void saveAppInfo(AppInfo appInfo) throws SQLException;

		void saveMessage(Message message) throws SQLException;

		void torDomain(String domain) throws SQLException;

		void updateSettings() throws SQLException;
	}

	public interface DataInterface {

		void appInfoFromDataBase(AppInfo appInfo);

		void bootstrapAppInfo(AppInfo appInfo) throws SQLException;

		void contactChanged(AppInfo contact);

		void createdMessage(AppInfo forApp, Message message) throws SQLException;
	}

	public interface GuiInterface {

		void appInfoRecieve(AppInfo appInfo);

		void contactChanged(AppInfo contact);

		void start();
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
