package com.github.javlock.lstr.v1.gui.messages;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.github.javlock.lstr.data.Message;

public class MessagesMessagesListCellRenderer implements ListCellRenderer<Message> {
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		JLabel label = new JLabel();

		if (value.getRawMsg() != null) {
			renderer.setText(value.getRawMsg());
			label.setText(value.getRawMsg());
		}
		if (isSelected) {
			// AppHeader.GUI.setMessagesSelectedAppInfo(value);
		}

		return renderer;
	}

}
