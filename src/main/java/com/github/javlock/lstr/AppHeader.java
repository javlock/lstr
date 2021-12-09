package com.github.javlock.lstr;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.gui.AppGui;

import lombok.Getter;
import lombok.Setter;

public class AppHeader {
	public static App app;

	public static final AppGui GUI = new AppGui();
	private static @Getter @Setter AppConfig config = new AppConfig();

	public static final File DIR = new File("App");
	public static String jarPath;

	static {
		try {
			jarPath = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	public static final ConcurrentHashMap<String, AppInfo> connectionInfoMap = new ConcurrentHashMap<>();

}
