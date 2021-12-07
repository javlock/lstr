package com.github.javlock.lstr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.javlock.lstr.data.Addr;
import com.github.javlock.lstr.data.AppInfo;
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
		static class TorConfig {
			private @Getter @Setter int socksPort = 10359;
		}

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

		private @Getter @Setter TorConfig torConfig = new TorConfig();

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

		private static final String TORBIN = "torbin/";
		private static final File TORBINDIR = new File(new File(DIR, TORBIN).getAbsolutePath());

		String filesDirName = TORBIN;
		String osName = getOsName();
		String arch = SystemUtils.OS_ARCH;
		String nameBin = "tor";

		private File torBin;

		// private File torBin;
		// ->
		File torrc = new File(TORBINDIR, "torrc");

		private void createConfig(App app) throws IOException {
			StringBuilder builder = new StringBuilder();
			builder.append("Log notice stdout").append('\n');
			builder.append("DataDirectory data").append('\n');
			if (!torrc.exists()) {
				builder.append("SOCKSPort ").append(app.config.torConfig.socksPort).append('\n');
				Files.createFile(torrc.toPath());
			} else {
				List<String> lines = Files.readAllLines(app.torWorker.torrc.toPath(), StandardCharsets.UTF_8);
				for (String string : lines) {
					if (string.toLowerCase().startsWith("SOCKSPort".toLowerCase())) {
						LOGGER.info(string);
					}
				}
			}
			Files.writeString(app.torWorker.torrc.toPath(), builder, StandardOpenOption.TRUNCATE_EXISTING);
		}

		private String getOsName() {
			if (SystemUtils.IS_OS_WINDOWS) {
				return "Windows";
			}
			if (SystemUtils.IS_OS_LINUX) {
				return "Linux";
			}
			throw new UnsupportedOperationException("OS not : WINDOWS OR LINUX");
		}

		public void init(App app) throws IOException {
			unpack();
			createConfig(app);
		}

		@Override
		public void run() {
			Thread.currentThread().setName("TorWorker");

			try { // TODO check // start

				int statusTor = new ExecutorMaster().setOutputListener(new ExecutorMasterOutputListener() {

					@Override
					public void appendInput(String line) {
						LOGGER.info(line);
					}

					@Override
					public void appendOutput(String line) {
						LOGGER.info(line);
					}

					@Override
					public void startedProcess(Long pid) {
						LOGGER.info("add hook for tor with pid {}", pid);
						Runtime.getRuntime().addShutdownHook(new Thread((Runnable) () -> {
							Optional<ProcessHandle> processOptional = ProcessHandle.of(pid);
							if (processOptional.isPresent()) {
								ProcessHandle process = processOptional.get();
								process.destroyForcibly();
								LOGGER.info("process tor with pid {} .destroyForcibly()", pid);
							}
						}, "ShutdownHook-" + pid));
						LOGGER.info("add hook for tor with pid {} -- OK", pid);
					}
				}).parrentCommand(torBin.getAbsolutePath()).arg("-f " + torrc.getAbsolutePath()).dir(TORBINDIR).call();
				LOGGER.info("tor stop with status:{}", statusTor);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

		private void unpack() throws IOException {
			filesDirName = filesDirName + osName + "." + arch;
			LOGGER.info("----------UNZIP-------------");
			try (ZipFile file = new ZipFile(jarPath)) {
				// Get file entries
				Enumeration<? extends ZipEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();
					String entryNameLC = entryName.toLowerCase();

					if (entryName.startsWith(TORBIN + osName + "." + arch)) {
						File f = new File(DIR, entryName);

						LOGGER.info("unpack():{}", entryName);

						if (entry.isDirectory()) {
							f.mkdirs();
						} else {
							if (!f.exists()) {
								if (!f.getParentFile().exists()) {
									f.getParentFile().mkdirs();
								}
								Files.createFile(f.toPath());
							}
							InputStream is = file.getInputStream(entry);
							Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
							if (f.getName().toLowerCase().startsWith("tor.exe")
									|| f.getName().toLowerCase().startsWith("tor")) {
								torBin = f;

								if (!SystemUtils.IS_OS_WINDOWS) {
									Set<PosixFilePermission> perms = Files.getPosixFilePermissions(torBin.toPath(),
											LinkOption.NOFOLLOW_LINKS);
									perms.add(PosixFilePermission.OWNER_WRITE);
									perms.add(PosixFilePermission.OWNER_READ);
									perms.add(PosixFilePermission.OWNER_EXECUTE);
									perms.remove(PosixFilePermission.GROUP_WRITE);
									perms.remove(PosixFilePermission.GROUP_READ);
									perms.remove(PosixFilePermission.GROUP_EXECUTE);
									perms.remove(PosixFilePermission.OTHERS_EXECUTE);
									perms.remove(PosixFilePermission.OTHERS_READ);
									perms.remove(PosixFilePermission.OTHERS_WRITE);
									Files.setPosixFilePermissions(torBin.toPath(), perms);
									Files.setPosixFilePermissions(f.toPath(), perms);
								}
							}

							LOGGER.info("Written :{}", entryName);
						}

						// torbin+OS+arch
					}

					// If directory then create a new directory in uncompressed folder

				}
			}

			LOGGER.info("----------UNZIP-------------");
		}

	}

	private static final File DIR = new File("App");

	private static final File CONFIGFILE = new File(DIR, "config.yaml");

	private static String jarPath;

	public static void main(String[] args) {
		try {
			App app = new App();
			System.out.println("App.main():" + jarPath);
			app.init();
			app.start();
		} catch (IOException | SQLException | URISyntaxException e) {
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
		client.startConnector();
	}
}
