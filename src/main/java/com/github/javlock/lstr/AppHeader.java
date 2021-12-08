package com.github.javlock.lstr;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.configs.AppConfig;

import io.netty.channel.ChannelFuture;

public class AppHeader {
	public static AppConfig config = new AppConfig();

	public static final File DIR = new File("App");
	private static final File CONFIGFILE = new File(DIR, "config.yaml");
	public static String jarPath;
	public static App app;

	static {
		try {
			jarPath = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	public final static ConcurrentHashMap<AppInfo, ChannelFuture> connected = new ConcurrentHashMap<>();;
	public final static ConcurrentHashMap<String, AppInfo> connectionInfoMap = new ConcurrentHashMap<>();

	/*
	 * public static String uuid; public static int socksPort; public static String
	 * domain; public static int serverPort;
	 */
}
