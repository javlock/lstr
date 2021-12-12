package com.github.javlock.lstr.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.github.javlock.lstr.AppHeader;

public class WindowCloseListener implements WindowListener {

	@Override
	public void windowActivated(WindowEvent e) {// IGNORE
	}

	@Override
	public void windowClosed(WindowEvent e) {
		AppHeader.app.active = false;
	}

	@Override
	public void windowClosing(WindowEvent e) {// IGNORE
	}

	@Override
	public void windowDeactivated(WindowEvent e) {// IGNORE
	}

	@Override
	public void windowDeiconified(WindowEvent e) {// IGNORE
	}

	@Override
	public void windowIconified(WindowEvent e) {// IGNORE
	}

	@Override
	public void windowOpened(WindowEvent e) {// IGNORE
	}

}
