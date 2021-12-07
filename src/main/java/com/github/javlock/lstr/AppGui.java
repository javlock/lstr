package com.github.javlock.lstr;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class AppGui extends JFrame {
	private static final long serialVersionUID = 8795345850667323095L;

	public AppGui() {

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
		msgBtnAndField.add(btnSendMessage, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane();
		messagesSplitPanel.setLeftComponent(scrollPane);

		JList messagesContactList = new JList();
		scrollPane.setViewportView(messagesContactList);

		JScrollPane connectionScrollPanel = new JScrollPane();
		tabbedPane.addTab("Connections", null, connectionScrollPanel, null);

		JScrollPane logScrollPane = new JScrollPane();
		tabbedPane.addTab("LOGS", null, logScrollPane, null);
	}
}
