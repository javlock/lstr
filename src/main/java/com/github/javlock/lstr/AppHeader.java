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
	public interface AppInterface {
		void appStart();

		void guiClose();

		void guiClosed();

		void guiStart();

		void guiStarted();

	}

	interface TorInteface {
		void torStart();

		void torStarted();

	}

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

	public static final TorInteface TORINTEFACE = new TorInteface() {

		@Override
		public void torStart() {
			// TODO Auto-generated method stub

		}

		@Override
		public void torStarted() {
			// TODO Auto-generated method stub

		}
	};

	public static final AppInterface APPINTERFACE = new AppInterface() {

		@Override
		public void appStart() {
			// TODO Auto-generated method stub

		}

		@Override
		public void guiClose() {
			AppHeader.GUI.setVisible(false);
			AppHeader.GUI.dispose();
			guiClosed();
		}

		@Override
		public void guiClosed() {
			// TODO Auto-generated method stub

		}

		@Override
		public void guiStart() {
			AppHeader.GUI.setVisible(true);
			guiStarted();
		}

		@Override
		public void guiStarted() {

		}

	};

}
