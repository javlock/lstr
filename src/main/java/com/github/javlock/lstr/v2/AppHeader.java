package com.github.javlock.lstr.v2;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.data.dummy.ChannelFutureDummy;
import com.github.javlock.lstr.v2.Interfaces.DataInterface;
import com.github.javlock.lstr.v2.data.AppInfo;
import com.github.javlock.lstr.v2.db.DataBase;
import com.github.javlock.lstr.v2.gui.AppGui;
import com.github.javlock.lstr.v2.gui.api.DefaultListModelApi;
import com.github.javlock.lstr.v2.network.NetworkWorker;
import com.github.javlock.lstr.v2.tor.Tor;
import com.github.javlock.lstr.v2.utils.FileUtils;

import lombok.Getter;
import lombok.Setter;

public abstract class AppHeader {

	public static final String osName = getOsName();
	public static final String arch = SystemUtils.OS_ARCH;

	public static final boolean activev2 = true;
	public static final App APP = new App();
	public static final File DIR = new File("LSTR_APP");
	public static File JARFILE;
	public static final ArrayList<String> obfs4List = new ArrayList<>();
	static {
		FileUtils.findJarFile(DIR);
		FileUtils.initObfs4List();
	}
	public static final DataBase DATABASE = new DataBase();
	public static final File DATABASFILE = new File(DIR, "database.db");

	public static final Tor TOR = new Tor();
	public static final NetworkWorker NETWORKWORKER = new NetworkWorker();

	private static @Getter @Setter AppConfig config = new AppConfig();

	public static final ChannelFutureDummy DUMMY = new ChannelFutureDummy();

	public static final DefaultListModelApi<AppInfo> messagesContactModel = new DefaultListModelApi<>();
	public static final ConcurrentHashMap<String, AppInfo> connectionInfoMap = new ConcurrentHashMap<>();

	public static AppGui GUI = new AppGui();

	public static final Integer objectForSendNetworkMaxLen = 47483647;

	public static final DataInterface dataInterface = new DataInterface() {

		@Override
		public void appInfoFromDataBase(AppInfo appInfo) {
			GUI.GUIINTERFACE.appInfoRecieve(appInfo);
		}

		@Override
		public void bootstrapAppInfo(AppInfo appInfo) throws SQLException {
			LoggerFactory.getLogger("bootstrapAppInfo").info("appInfo:{}", appInfo);
			connectionInfoMap.putIfAbsent(appInfo.getHost(), appInfo);
			AppHeader.DATABASE.DATABASEINTERFACE.saveAppInfo(appInfo);
		}

		@Override
		public void contactChanged(AppInfo contact) {
			AppHeader.GUI.GUIINTERFACE.contactChanged(contact);
		}

		@Override
		public void createdMessage(AppInfo messagesSelectedAppInfo, Message message) throws SQLException {
			AppHeader.DATABASE.DATABASEINTERFACE.saveMessage(message);
			messagesSelectedAppInfo.send(message);
		}

	};

	private static String getOsName() {
		if (SystemUtils.IS_OS_WINDOWS) {
			return "Windows";
		}
		if (SystemUtils.IS_OS_LINUX) {
			return "Linux";
		}
		throw new UnsupportedOperationException("OS not : WINDOWS OR LINUX");
	}

	private AppHeader() {
	}
}
