package com.github.javlock.lstr;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.lstr.data.Addr;
import com.github.javlock.lstr.data.AppInfo;
import com.github.javlock.lstr.utils.io.IOUtils;
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
	static class AppConfig {
		public static void readConfig(App app) throws IOException {
			if (CONFIGFILE.exists()) {
				app.objectMapper.readValue(CONFIGFILE, AppConfig.class);
			} else {
				if (!DIR.exists()) {
					Files.createDirectories(DIR.toPath());
				}
				app.objectMapper.writeValue(CONFIGFILE, app.config);
			}
		}

		private @Getter @Setter String uuid = UUID.randomUUID().toString();
		private @Getter @Setter int serverPort = 49000;

	}

	class DataBase {
		ConnectionSource connectionSource;
		private Dao<Addr, Long> addrDao;
		private Dao<AppInfo, ?> appInfoDao;// FIXME id Type

		private void createDAOs() throws SQLException {
			addrDao = DaoManager.createDao(connectionSource, Addr.class);
			appInfoDao = DaoManager.createDao(connectionSource, AppInfo.class);
		}

		private void createSource() throws SQLException {
			connectionSource = new JdbcConnectionSource(
					"jdbc:sqlite:" + new File(DIR, "database.db").getAbsolutePath());
		}

		private void createTables() throws SQLException {
			TableUtils.createTableIfNotExists(connectionSource, Addr.class);
			TableUtils.createTableIfNotExists(connectionSource, AppInfo.class);
		}

		public void init() throws SQLException {
			createSource();
			createDAOs();
			createTables();
		}
	}

	class NetClient {
		class NetClientConnector extends Thread {
			private boolean connect(Addr addr) {
				String host = addr.getHost();
				int port = addr.getPort();

				return false;// TODO REWRITE TO RESULT
			}

			@Override
			public void run() {
				Thread.currentThread().setName("NetClientConnector");

				while (active) { // loop
					for (AppInfo appInfo : connectionInfos) {
						if (appInfo.isConnected()) {
							LOGGER.info("isConnected");
							continue;
						}
						boolean connected = false;
						connectedLabel: {
							LOGGER.info("connectedLabel");
							if (!connected) {
								LOGGER.info("!connected");

								for (Addr addr : appInfo.getAddrs()) {
									if (connect(addr)) {
										connected = true;
										break connectedLabel;
									} // TODO NOT CONNECTED
								}
							} else {
								LOGGER.info("!connected ELSE");
							}
						}
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} // loop
			}
		}

		private static final Logger LOGGER = LoggerFactory.getLogger("NetClient");

		private final CopyOnWriteArrayList<AppInfo> connectionInfos = new CopyOnWriteArrayList<>();
		private final NetClientConnector connector = new NetClientConnector();

		public void startConnector() {
			connector.start();
		}
	}

	class NetClientHandler extends NetHandler {
		public NetClientHandler() {
			setType(HandlerType.CLIENT);
		}
	}

	class NetHandler extends ChannelDuplexHandler {
		enum HandlerType {
			NA, SERVER, CLIENT
		}

		protected static final Logger LOGGER = LoggerFactory.getLogger("NetHandler");

		private @Getter @Setter HandlerType type = HandlerType.NA;

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			LOGGER.error("{}-channelActive", getType(), ctx.channel().remoteAddress());
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			LOGGER.error("{}-channelInactive", getType(), ctx.channel().remoteAddress());
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

	static class TorWorker extends Thread {
		private static final Logger LOGGER = LoggerFactory.getLogger("TorWorker");
		private File torBin = getTorBin();

		private File getTorBin() {
			String name = "tor";
			if (SystemUtils.IS_OS_WINDOWS) {
				name = name + ".exe";
			}
			return new File(new File(new File(TORBINDIR, SystemUtils.OS_NAME), SystemUtils.OS_ARCH), name);
		}

		public void init() throws IOException {
			LOGGER.info("{}", torBin.getAbsolutePath());
			unpack();
		}

		@Override
		public void run() {
			Thread.currentThread().setName("TorWorker");
			try {
				// TODO check
				// start
				int statusTor = new ExecutorMaster().setOutputListener(new ExecutorMasterOutputListener() {
					@Override
					public void appendInput(String line) {
						LOGGER.info(line);
					}

					@Override
					public void appendOutput(String line) {
						LOGGER.info(line);
					}
				}).parrentCommand(torBin.getAbsolutePath()).dir(TORBINDIR)
						// .command(maven + " clean install && exit 0 ")
						.call();
				LOGGER.info("tor stop with status:{}", statusTor);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

		private void unpack() throws IOException {
			// TODO Auto-generated method stub

			String filesListName = "torbin/";
			String arch = SystemUtils.OS_ARCH;

			if (SystemUtils.IS_OS_WINDOWS) {
				filesListName = filesListName + "WWW." + arch + ".List";
			} else if (SystemUtils.IS_OS_LINUX) {
				filesListName = filesListName + "Linux." + arch + ".List";
			}
			LOGGER.info("unpack from {}", filesListName);
			ArrayList<String> files = IOUtils.readLinesFromResourceFile(filesListName);
			for (String file : files) {
				LOGGER.info("unpack {} to {}", file, null);
			}
		}

	}

	private static final File DIR = new File("App");
	private static final File CONFIGFILE = new File(DIR, "config.yaml");
	private static final File TORBINDIR = new File(DIR, "torBins");

	public static void main(String[] args) {
		try {
			App app = new App();
			app.init();
			app.start();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	private TorWorker torWorker = new TorWorker();

	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	AppConfig config = new AppConfig();

	DataBase dataBase = new DataBase();
	NetClient client = new NetClient();
	NetServer server = new NetServer();

	boolean active = true;

	private void init() throws IOException, SQLException {
		// files
		AppConfig.readConfig(this);
		dataBase.init();
		server.init();
		torWorker.init();
	}

	@Override
	public void run() {
		server.bind();
		torWorker.start();
		client.startConnector();
	}
}
