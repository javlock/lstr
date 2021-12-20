package com.github.javlock.lstr.v1.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.data.configs.AppConfig;
import com.github.javlock.lstr.v1.AppHeader;
import com.github.javlock.lstr.v1.gui.messages.MessagesContactListCellRenderer;
import com.github.javlock.lstr.v1.gui.messages.MessagesMessagesListCellRenderer;

import lombok.Setter;

public class AppGui extends JFrame {

	private static final long serialVersionUID = 8795345850667323095L;

	private static final DefaultListModel<AppInfo> messagesContactModel = new DefaultListModel<>();
	private static final DefaultListModel<Message> messagesMessageModel = new DefaultListModel<>();

	private static final Logger LOGGER = LoggerFactory.getLogger("AppGui");

	private static transient MessagesContactListCellRenderer messagesContactListCellRenderer = new MessagesContactListCellRenderer();
	private static transient MessagesMessagesListCellRenderer messagesMessagesListCellRenderer = new MessagesMessagesListCellRenderer();

	private @Setter AppInfo messagesSelectedAppInfo;

	private JTextField tfUsername;
	private JTextField tfServerPort;
	private JTextField tfTorPort;
	private JTextField tfProxyPortForTor;

	boolean confLoaded;
	private JTextField tfProxyHostForTor;
	private JCheckBox chckbxNewCheckBox;
	private JTextArea taMessage;

	public AppGui() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 550);

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JSplitPane messagesSplitPanel = new JSplitPane();
		tabbedPane.addTab("messages", null, messagesSplitPanel, null);

		JPanel panel = new JPanel();
		messagesSplitPanel.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel msgBtnAndField = new JPanel();
		panel.add(msgBtnAndField, BorderLayout.SOUTH);
		msgBtnAndField.setLayout(new BorderLayout(0, 0));

		taMessage = new JTextArea();

		msgBtnAndField.add(taMessage, BorderLayout.CENTER);

		JButton btnSendMessage = new JButton("Send");
		btnSendMessage.addActionListener(e -> sendMessage());
		msgBtnAndField.add(btnSendMessage, BorderLayout.EAST);

		JScrollPane messagesMessageScrollPane = new JScrollPane();
		panel.add(messagesMessageScrollPane, BorderLayout.CENTER);

		JList<Message> messagesList = new JList<>(messagesMessageModel);
		messagesList.setCellRenderer(messagesMessagesListCellRenderer);
		messagesMessageScrollPane.setViewportView(messagesList);

		JScrollPane messagesContactScrollPane = new JScrollPane();
		messagesSplitPanel.setLeftComponent(messagesContactScrollPane);

		JList<AppInfo> messagesContactList = new JList<>(messagesContactModel);
		messagesContactList.addListSelectionListener(e -> {
			if (messagesSelectedAppInfo != null) {
				for (Message message : messagesSelectedAppInfo.getMessages()) {
					messagesMessageModel.addElement(message);
				}
			}
		});
		messagesContactList.setCellRenderer(messagesContactListCellRenderer);
		messagesContactScrollPane.setViewportView(messagesContactList);

		JScrollPane connectionScrollPanel = new JScrollPane();
		tabbedPane.addTab("Connections", null, connectionScrollPanel, null);

		JPanel settingsPanel = new JPanel();
		tabbedPane.addTab("Settings", null, settingsPanel, null);
		GridBagLayout gblsettingsPanel = new GridBagLayout();
		gblsettingsPanel.columnWidths = new int[] { 0, 0, 0 };
		gblsettingsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblsettingsPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gblsettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		settingsPanel.setLayout(gblsettingsPanel);

		JLabel lblUsername = new JLabel("username");
		GridBagConstraints gbclblUsername = new GridBagConstraints();
		gbclblUsername.insets = new Insets(0, 0, 5, 5);
		gbclblUsername.anchor = GridBagConstraints.EAST;
		gbclblUsername.gridx = 0;
		gbclblUsername.gridy = 0;
		settingsPanel.add(lblUsername, gbclblUsername);

		tfUsername = new JTextField();
		GridBagConstraints gbctfUsername = new GridBagConstraints();
		gbctfUsername.insets = new Insets(0, 0, 5, 0);
		gbctfUsername.fill = GridBagConstraints.HORIZONTAL;
		gbctfUsername.gridx = 1;
		gbctfUsername.gridy = 0;
		settingsPanel.add(tfUsername, gbctfUsername);
		tfUsername.setColumns(10);

		JLabel lblServerPort = new JLabel("serverPort");
		GridBagConstraints gbclblServerPort = new GridBagConstraints();
		gbclblServerPort.anchor = GridBagConstraints.EAST;
		gbclblServerPort.insets = new Insets(0, 0, 5, 5);
		gbclblServerPort.gridx = 0;
		gbclblServerPort.gridy = 1;
		settingsPanel.add(lblServerPort, gbclblServerPort);

		tfServerPort = new JTextField();
		GridBagConstraints gbctfServerPort = new GridBagConstraints();
		gbctfServerPort.insets = new Insets(0, 0, 5, 0);
		gbctfServerPort.fill = GridBagConstraints.HORIZONTAL;
		gbctfServerPort.gridx = 1;
		gbctfServerPort.gridy = 1;
		settingsPanel.add(tfServerPort, gbctfServerPort);
		tfServerPort.setColumns(10);

		JLabel lblTorPort = new JLabel("torPort");
		GridBagConstraints gbclblTorPort = new GridBagConstraints();
		gbclblTorPort.anchor = GridBagConstraints.EAST;
		gbclblTorPort.insets = new Insets(0, 0, 5, 5);
		gbclblTorPort.gridx = 0;
		gbclblTorPort.gridy = 2;
		settingsPanel.add(lblTorPort, gbclblTorPort);

		tfTorPort = new JTextField();
		GridBagConstraints gbctfTorPort = new GridBagConstraints();
		gbctfTorPort.insets = new Insets(0, 0, 5, 0);
		gbctfTorPort.fill = GridBagConstraints.HORIZONTAL;
		gbctfTorPort.gridx = 1;
		gbctfTorPort.gridy = 2;
		settingsPanel.add(tfTorPort, gbctfTorPort);
		tfTorPort.setColumns(10);

		chckbxNewCheckBox = new JCheckBox("need proxy for tor");
		GridBagConstraints gbcchckbxNewCheckBox = new GridBagConstraints();
		gbcchckbxNewCheckBox.insets = new Insets(0, 0, 5, 0);
		gbcchckbxNewCheckBox.gridx = 1;
		gbcchckbxNewCheckBox.gridy = 3;
		settingsPanel.add(chckbxNewCheckBox, gbcchckbxNewCheckBox);

		JButton btnSaveSettings = new JButton("save");
		btnSaveSettings.addActionListener(e -> settingsSave());

		JButton btnLoadSettings = new JButton("load");
		btnLoadSettings.addActionListener(e -> settingsLoad());

		JLabel lblProxyhostfortor = new JLabel("proxyHostForTor");
		GridBagConstraints gbclblProxyhostfortor = new GridBagConstraints();
		gbclblProxyhostfortor.anchor = GridBagConstraints.EAST;
		gbclblProxyhostfortor.insets = new Insets(0, 0, 5, 5);
		gbclblProxyhostfortor.gridx = 0;
		gbclblProxyhostfortor.gridy = 4;
		settingsPanel.add(lblProxyhostfortor, gbclblProxyhostfortor);

		tfProxyHostForTor = new JTextField();
		GridBagConstraints gbctfProxyHostForTor = new GridBagConstraints();
		gbctfProxyHostForTor.insets = new Insets(0, 0, 5, 0);
		gbctfProxyHostForTor.fill = GridBagConstraints.HORIZONTAL;
		gbctfProxyHostForTor.gridx = 1;
		gbctfProxyHostForTor.gridy = 4;
		settingsPanel.add(tfProxyHostForTor, gbctfProxyHostForTor);
		tfProxyHostForTor.setColumns(10);

		JLabel lblProxyPortForTor = new JLabel("proxyPortForTor");
		GridBagConstraints gbclblProxyPortForTor = new GridBagConstraints();
		gbclblProxyPortForTor.anchor = GridBagConstraints.EAST;
		gbclblProxyPortForTor.insets = new Insets(0, 0, 5, 5);
		gbclblProxyPortForTor.gridx = 0;
		gbclblProxyPortForTor.gridy = 5;
		settingsPanel.add(lblProxyPortForTor, gbclblProxyPortForTor);

		tfProxyPortForTor = new JTextField();
		GridBagConstraints gbctfProxyPortForTor = new GridBagConstraints();
		gbctfProxyPortForTor.insets = new Insets(0, 0, 5, 0);
		gbctfProxyPortForTor.fill = GridBagConstraints.HORIZONTAL;
		gbctfProxyPortForTor.gridx = 1;
		gbctfProxyPortForTor.gridy = 5;
		settingsPanel.add(tfProxyPortForTor, gbctfProxyPortForTor);
		tfProxyPortForTor.setColumns(10);
		GridBagConstraints gbcbtnLoadSettings = new GridBagConstraints();
		gbcbtnLoadSettings.insets = new Insets(0, 0, 5, 5);
		gbcbtnLoadSettings.gridx = 0;
		gbcbtnLoadSettings.gridy = 6;
		settingsPanel.add(btnLoadSettings, gbcbtnLoadSettings);
		GridBagConstraints gbcbtnSaveSettings = new GridBagConstraints();
		gbcbtnSaveSettings.insets = new Insets(0, 0, 0, 5);
		gbcbtnSaveSettings.gridx = 0;
		gbcbtnSaveSettings.gridy = 7;
		settingsPanel.add(btnSaveSettings, gbcbtnSaveSettings);

		JScrollPane logScrollPane = new JScrollPane();
		tabbedPane.addTab("LOGS", null, logScrollPane, null);

		addWindowListener(new WindowCloseListener());
	}

	public void receiveAppInfo(AppInfo appInfo) {
		if (!messagesContactModel.contains(appInfo)) {
			messagesContactModel.addElement(appInfo);
		}
	}

	private void sendMessage() {
		try {
			if (messagesSelectedAppInfo == null) {
				LOGGER.warn("empty for");
				return;
			}
			if (taMessage.getText().isEmpty()) {
				LOGGER.warn("Message empty");
				return;
			}
			Message message = new Message();
			message.setRawMsg(taMessage.getText());
			message.setFrom(AppHeader.getConfig().getTorDomain());
			message.setTo(messagesSelectedAppInfo.getHost());

			AppHeader.app.dataBase.saveMessage(message);
			messagesSelectedAppInfo.send(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void settingsLoad() {
		try {
			AppConfig conf = AppHeader.getConfig();
			tfUsername.setText(conf.getUsername());
			tfServerPort.setText(Integer.toString(conf.getServerPort()));
			tfTorPort.setText(Integer.toString(conf.getTorSocksPort()));

			chckbxNewCheckBox.setSelected(conf.isTorNeedProxy());
			tfProxyHostForTor.setText(conf.getTorProxyHost());
			tfProxyPortForTor.setText(Integer.toString(conf.getTorProxyPort()));
			confLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void settingsSave() {
		if (confLoaded) {
			try {
				AppConfig conf = AppHeader.getConfig();
				conf.setUsername(tfUsername.getText());
				conf.setServerPort(Integer.parseInt(tfServerPort.getText()));
				conf.setTorSocksPort(Integer.parseInt(tfTorPort.getText()));

				conf.setTorNeedProxy(chckbxNewCheckBox.isSelected());
				conf.setTorProxyHost(tfProxyHostForTor.getText());
				conf.setTorProxyPort(Integer.parseInt(tfProxyPortForTor.getText()));

				AppHeader.app.dataBase.updateSettings();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateMessages() {
		try {
			messagesMessageModel.clear();
			if (messagesSelectedAppInfo == null) {
				return;
			}
			String mydomain = AppHeader.getConfig().getTorDomain();

			CopyOnWriteArrayList<Message> myMessages = AppHeader.connectionInfoMap.get(mydomain).getMessages();

			for (Message message : myMessages) {
				if ((message.getFrom().equals(messagesSelectedAppInfo.getHost())
						|| message.getTo().equals(messagesSelectedAppInfo.getHost()))
						&& !messagesMessageModel.contains(message)) {
					messagesMessageModel.addElement(message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
