package com.github.javlock.lstr.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.Message;

public class AppGui extends JFrame {

	private static final long serialVersionUID = 8795345850667323095L;

	private static final DefaultListModel<AppInfo> messagesContactModel = new DefaultListModel<>();

	ActionListener btnSendMessageListener = a -> {
		System.out.println("AppGui.enclosing_method()");
	};

	public AppGui() {
		setSize(1300, 760);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JSplitPane messagesSplitPanel = new JSplitPane();
		tabbedPane.addTab("messages", null, messagesSplitPanel, null);

		JPanel panel = new JPanel();
		messagesSplitPanel.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel msgBtnAndField = new JPanel();
		panel.add(msgBtnAndField, BorderLayout.SOUTH);
		msgBtnAndField.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();

		msgBtnAndField.add(textArea, BorderLayout.CENTER);

		JButton btnSendMessage = new JButton("Send");
		btnSendMessage.addActionListener(btnSendMessageListener);
		msgBtnAndField.add(btnSendMessage, BorderLayout.EAST);

		JScrollPane scrollPane1 = new JScrollPane();
		panel.add(scrollPane1, BorderLayout.CENTER);

		JList<Message> messagesList = new JList<>();
		scrollPane1.setViewportView(messagesList);

		JScrollPane scrollPane = new JScrollPane();
		messagesSplitPanel.setLeftComponent(scrollPane);

		FocusedTitleListCellRenderer renderer = new FocusedTitleListCellRenderer();
		JList<AppInfo> messagesContactList = new JList<>(messagesContactModel);
		messagesContactList.setCellRenderer(renderer);

		scrollPane.setViewportView(messagesContactList);

		JScrollPane connectionScrollPanel = new JScrollPane();
		tabbedPane.addTab("Connections", null, connectionScrollPanel, null);

		JScrollPane logScrollPane = new JScrollPane();
		tabbedPane.addTab("LOGS", null, logScrollPane, null);
	}

}
