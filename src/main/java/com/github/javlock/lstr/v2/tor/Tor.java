package com.github.javlock.lstr.v2.tor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.v2.AppHeader;
import com.github.javlock.lstr.v2.Interfaces.TorInteface;
import com.github.javlock.lstr.v2.utils.FileUtils;
import com.github.javlock.ostools.executor.ExecutorMaster;
import com.github.javlock.ostools.executor.ExecutorMasterOutputListener;

public class Tor extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger("Tor");

	private static final String BRIDGE_OBFS4 = "Bridge obfs4 ";
	private static final String TORBINSTRING = "torbin/";
	private static final File TORDIR = new File(AppHeader.DIR, TORBINSTRING);
	private static final File TORRC = new File(TORDIR, "torrc");
	private static final File TORDATADIR = new File(TORDIR, "data");
	private static final File TORSERVICEDIR = new File(TORDATADIR, "service");
	private static final File TORSERVICEHOSTNAMFILE = new File(TORSERVICEDIR, "hostname");
	private static final String nameBinObfs4proxy = "obfs4proxy";
	private static final String nameBinSnowflake = "snowflake-client";
	private File obfs4proxyBin;
	private File snowflakeBin;
	private File TORBIN;
	private boolean torStarted = false;

	private String filesDirName = TORBINSTRING;
	public final TorInteface TORINTEFACE = new TorInteface() {

		private void createConfig() throws IOException {
			int torSocksPort = AppHeader.getConfig().getTorSocksPort();
			int serverPort = AppHeader.getConfig().getServerPort();
			StringBuilder builder = new StringBuilder();
			char n = '\n';

			builder.append("Log notice stdout").append(n);
			builder.append("DataDirectory ").append(TORDATADIR.getAbsolutePath()).append(n);

			builder.append("HiddenServiceDir ").append(TORSERVICEDIR.getAbsolutePath()).append(n);
			builder.append("HiddenServicePort 4001 127.0.0.1:" + serverPort).append(n);

			builder.append("SOCKSPort ").append(torSocksPort).append(n);
			if (obfs4proxyBin != null || snowflakeBin != null) {
				builder.append("UseBridges 1").append(n);

				// bridges
				if (obfs4proxyBin != null) {
					builder.append("ClientTransportPlugin meek_lite,obfs2,obfs3,obfs4,scramblesuit exec ")
							.append(obfs4proxyBin.getAbsolutePath()).append(n);
					for (String proxy : AppHeader.obfs4List) {
						builder.append(BRIDGE_OBFS4).append(proxy).append(n);
					}
				}
				if (snowflakeBin != null) {
					builder.append("ClientTransportPlugin snowflake exec ").append(snowflakeBin.getAbsolutePath())
							.append(" -url https://snowflake-broker.torproject.net.global.prod.fastly.net/ -front cdn.sstatic.net -ice stun:stun.l.google.com:19302,stun:stun.voip.blackberry.com:3478,stun:stun.altar.com.pl:3478,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.com:3478,stun:stun.sonetel.net:3478,stun:stun.stunprotocol.org:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478")
							.append(n);
				}

				// bridges
			}

			if (!TORRC.exists()) {
				if (TORRC.getParentFile().mkdirs()) {
					LOGGER.info("dir created:{}", TORRC.getParentFile());
				}
				Files.createFile(TORRC.toPath());
			}
			Files.writeString(TORRC.toPath(), builder, StandardOpenOption.TRUNCATE_EXISTING);
			List<String> lines = Files.readAllLines(TORRC.toPath(), StandardCharsets.UTF_8);
			for (String string : lines) {
				if (string.toLowerCase().startsWith("SOCKSPort".toLowerCase())) {
					LOGGER.info(string);
				}
				if (string.toLowerCase().startsWith("HiddenServicePort".toLowerCase())) {
					LOGGER.info(string);
				}
			}
		}

		@Override
		public void torInit() throws IOException {
			LOGGER.info("torInit");
			unpack();
			createConfig();
			torStart();
		}

		@Override
		public void torStart() {
			LOGGER.info("torStart");
			start();
			while (!torStarted) {
				LOGGER.info("wait");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			torStarted();
		}

		@Override
		public void torStarted() {
			LOGGER.info("torStarted");
			AppHeader.NETWORKWORKER.networkInterface.init();
			AppHeader.NETWORKWORKER.networkInterface.start();
		}

		private void unpack() throws IOException {
			filesDirName = filesDirName + AppHeader.osName + "." + AppHeader.arch;
			LOGGER.info("----------UNZIP-------------");
			try (ZipFile file = new ZipFile(AppHeader.JARFILE)) {
				// Get file entries
				Enumeration<? extends ZipEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();

					if (entryName.startsWith(TORBINSTRING + AppHeader.osName + "." + AppHeader.arch)) {
						File f = new File(AppHeader.DIR, entryName);

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

							// torbin
							if (f.getName().toLowerCase().startsWith("tor.exe")
									|| f.getName().toLowerCase().startsWith("tor")) {
								TORBIN = f;
								LOGGER.info("found tor bin: {}", f.getAbsolutePath());
								FileUtils.setPermissionBinFile(f);
							} // torbin

							if (f.getName().toLowerCase().startsWith(nameBinSnowflake)) {
								snowflakeBin = f;
								LOGGER.info("found snowflake: {}", f.getAbsolutePath());
								FileUtils.setPermissionBinFile(f);
							}
							if (f.getName().toLowerCase().startsWith(nameBinObfs4proxy)) {
								obfs4proxyBin = f;
								LOGGER.info("found obfs4proxy: {}", f.getAbsolutePath());
								FileUtils.setPermissionBinFile(f);
							}
						}
					}

				}
			}

			LOGGER.info("----------UNZIP-------------");
		}

	};

	@Override
	public void run() {
		try {
			int torstatus = new ExecutorMaster().setOutputListener(new ExecutorMasterOutputListener() {

				@Override
				public void appendInput(String line) {
					LOGGER.info("appendInput:{}", line);
				}

				@Override
				public void appendOutput(String line) throws Exception {
					LOGGER.info("appendOutput:{}", line);
					if (line.contains("Bootstrapped 100%")) {

						String domain = Files.readString(TORSERVICEHOSTNAMFILE.toPath(), StandardCharsets.UTF_8)
								.replaceAll("[\n]*", "");

						LOGGER.info("domain:[{}]", domain);
						LOGGER.info("domain 10:[{}]", domain.contains("\n"));
						AppHeader.DATABASE.DATABASEINTERFACE.torDomain(domain);
						torStarted = true;
					}
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
			}).parrentCommand(TORBIN.getAbsolutePath()).arg("-f " + TORRC.getAbsolutePath()).dir(TORDIR).call();
			LOGGER.info("tor stop with status:{}", torstatus);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
