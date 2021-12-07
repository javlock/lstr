package com.github.javlock.lstr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.services.TorWorker;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.Setter;

public class App extends Thread {
	public static class AppConfig {
		public static class TorConfig {
			public @Getter @Setter int socksPort = 10359;
			public String infohostname;
		}

		public static void readConfig(App app) throws IOException {
			if (CONFIGFILE.exists()) {
				app.config = app.objectMapper.readValue(CONFIGFILE, AppConfig.class);
			} else {
				if (!DIR.exists()) {
					Files.createDirectories(DIR.toPath());
				}
				app.objectMapper.writeValue(CONFIGFILE, app.config);
			}
		}

		private @Getter @Setter String uuid = UUID.randomUUID().toString();
		public @Getter @Setter int serverPort = 49000;

		public @Getter @Setter TorConfig torConfig = new TorConfig();

	}

	static class BootStrapRunner extends Thread {
		private static final Logger LOGGER = LoggerFactory.getLogger("BootStrapRunner");

		private static final String BOOTSTRAPURL = "https://raw.githubusercontent.com/javlock/lstr/main/infocon/bootstrap";
		private App app;

		public BootStrapRunner(App app) {
			this.app = app;
		}

		@Override
		public void run() {
			while (app.active) {
				try {
					ArrayList<String> lines = new ArrayList<>();
					URL url = new URL(BOOTSTRAPURL);
					URLConnection yc = url.openConnection();
					try (BufferedReader in = new BufferedReader(
							new InputStreamReader(yc.getInputStream(), StandardCharsets.UTF_8))) {
						String inputLine;
						while ((inputLine = in.readLine()) != null) {
							lines.add(inputLine);
						}
					}

					for (String string : lines) {
						app.torBootstrapDomain(string);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();

				}
			}
		}
	}

	class DataBase {
		ConnectionSource connectionSource;
		private Dao<AppInfo, ?> appInfoDao;// FIXME id Type

		private void createDAOs() throws SQLException {
			appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		}

		private void createSource() throws SQLException {
			connectionSource = new JdbcConnectionSource(
					"jdbc:sqlite:" + new File(DIR, "database.db").getAbsolutePath());
		}

		private void createTables() throws SQLException {
			TableUtils.createTableIfNotExists(connectionSource, AppInfo.class);
		}

		public void init() throws SQLException {
			createSource();
			createDAOs();
			createTables();
		}
	}

	public static class NetClientHandler extends NetHandler {

		public NetClientHandler(String uuid, String host, int port) {
			this.uuid = uuid;
			this.host = host;
			this.port = port;
			setType(HandlerType.CLIENT);
		}
	}

	public static class NetHandler extends ChannelDuplexHandler {
		enum HandlerType {
			NA, SERVER, CLIENT
		}

		protected static final Logger LOGGER = LoggerFactory.getLogger("NetHandler");
		protected String uuid;
		protected String host;
		protected int port;

		private @Getter @Setter HandlerType type = HandlerType.NA;

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			LOGGER.error("{}-channelActive", getType(), ctx.channel().remoteAddress());
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			LOGGER.error("{}-channelInactive", getType(), ctx.channel().remoteAddress());
			app.client.disconnect(ctx, uuid, host, port);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			LOGGER.error("{}-channelRead", getType(), msg);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			LOGGER.error("{}-exceptionCaught", getType(), cause);
		}
	}

	class NetServer {

		private static @Getter ServerBootstrap serverBootstrap = new ServerBootstrap();
		private static @Getter EventLoopGroup serverWorkgroup = new NioEventLoopGroup();
		private static final Logger LOGGER = LoggerFactory.getLogger("NetServer");
		private ChannelFuture bindChannelFuture;

		public void bind() {
			bindChannelFuture = serverBootstrap.bind();
		}

		public void init() {
			LOGGER.info("initNetwork-init");
			serverBootstrap.group(serverWorkgroup).channel(NioServerSocketChannel.class)
					.localAddress(new InetSocketAddress(config.serverPort));
			serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();

					p.addLast(new ObjectDecoder(Integer.MAX_VALUE,
							ClassResolvers.softCachingConcurrentResolver(Packet.class.getClassLoader())));
					p.addLast(new ObjectEncoder());
					p.addLast(new NetServerHandler());
				}
			});
			LOGGER.info("initNetwork-init-end");
		}

	}

	class NetServerHandler extends NetHandler {
		public NetServerHandler() {
			setType(HandlerType.SERVER);
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger("App");

	public static final File DIR = new File("App");

	private static final File CONFIGFILE = new File(DIR, "config.yaml");

	public static String jarPath;

	private static App app;

	public static void main(String[] args) {
		try {
			app = new App();
			System.out.println("App.main():" + jarPath);
			app.init();
			app.start();
		} catch (IOException | SQLException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private final BootStrapRunner bootStrapRunner = new BootStrapRunner(this);

	private TorWorker torWorker = new TorWorker(this);

	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	public AppConfig config = new AppConfig();

	DataBase dataBase = new DataBase();

	NetClient client = new NetClient(this);

	NetServer server = new NetServer();

	public boolean active = true;

	public App() throws URISyntaxException {
		jarPath = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
	}

	private void init() throws IOException, SQLException {
		// files
		AppConfig.readConfig(this);
		dataBase.init();
		server.init();
		torWorker.init(this);
	}

	@Override
	public void run() {
		server.bind();
		torWorker.start();
		bootStrapRunner.start();
		client.startConnector();
	}

	public void torBootstrapDomain(String onionDomain) {
		client.connector.appendDomain(onionDomain);
	}

	public void torServiceHost(String domain) throws IOException {
		LOGGER.info("TOR DOMAIN IS {}", domain);
		boolean needWrite = config.torConfig.infohostname == null || !config.torConfig.infohostname.equals(domain);
		config.torConfig.infohostname = domain;
		if (needWrite) {
			writeConfig();
		}
	}

	private void writeConfig() throws IOException {
		objectMapper.writeValue(CONFIGFILE, config);
	}
}
