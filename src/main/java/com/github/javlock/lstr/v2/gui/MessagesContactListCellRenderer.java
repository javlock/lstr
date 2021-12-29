package com.github.javlock.lstr.v2.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.data.AppInfo;

public class MessagesContactListCellRenderer implements ListCellRenderer<AppInfo> {
	protected static Border noFocusBorder = new EmptyBorder(15, 1, 1, 1);
	protected static TitledBorder focusBorder = new TitledBorder(LineBorder.createGrayLineBorder(), "title");
	private static final Logger LOGGER = LoggerFactory.getLogger("MessagesContactListCellRenderer");
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(JList<? extends AppInfo> list, AppInfo value, int index,
			boolean isSelected, boolean cellHasFocus) {

		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		JLabel label = new JLabel();

		if (value.getUsername() != null) {
			renderer.setText(value.getUsername());
			label.setText(value.getUsername());
		} else {
			renderer.setText(value.getHost());
			label.setText(value.getHost());
		}
		if (isSelected) {
			LOGGER.info("addListSelectionListener");
			AppHeader.GUI.setMessagesSelectedAppInfo(value);
		}

		// renderer.setBorder(cellHasFocus ? focusBorder : noFocusBorder);

		return renderer;
	}

}
