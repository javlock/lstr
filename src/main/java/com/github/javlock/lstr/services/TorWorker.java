package com.github.javlock.lstr.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.executor.ExecutorMaster;
import com.github.javlock.lstr.executor.ExecutorMasterOutputListener;

public class TorWorker extends Thread {
	private static final String BRIDGE_OBFS4 = "Bridge obfs4 ";

	private static final Logger LOGGER = LoggerFactory.getLogger("TorWorker");

	private static final String TORBIN = "torbin/";

	private File TORBINDIR = new File(new File(AppHeader.DIR, TORBIN).getAbsolutePath());
	private File TORRC = new File(TORBINDIR, "torrc");
	private File TORDATADIR = new File(TORBINDIR, "data");
	private File TORSERVICEDIR = new File(TORDATADIR, "service");
	private File TORSERVICEHOSTNAMFILE = new File(TORSERVICEDIR, "hostname");

	private String filesDirName = TORBIN;
	private String osName = getOsName();

	private String arch = SystemUtils.OS_ARCH;

	private String nameBinObfs4proxy = "obfs4proxy";
	private String nameBinSnowflake = "snowflake-client";

	private File torBin;
	private File snowflakeBin;
	private File obfs4proxyBin;

	private void createConfig() throws IOException {
		int torSocksPort = AppHeader.getConfig().getTorSocksPort();
		int serverPort = AppHeader.getConfig().getServerPort();

		StringBuilder builder = new StringBuilder();

		builder.append("Log notice stdout").append('\n');
		builder.append("DataDirectory ").append(TORDATADIR.getAbsolutePath()).append('\n');

		builder.append("HiddenServiceDir ").append(TORSERVICEDIR.getAbsolutePath()).append('\n');
		builder.append("HiddenServicePort 4001 127.0.0.1:" + serverPort).append('\n');

		builder.append("SOCKSPort ").append(torSocksPort).append('\n');

		if (obfs4proxyBin != null || snowflakeBin != null) {
			builder.append("UseBridges 1").append('\n');
			// bridges
			if (obfs4proxyBin != null) {
				builder.append("ClientTransportPlugin meek_lite,obfs2,obfs3,obfs4,scramblesuit exec ")
						.append(obfs4proxyBin.getAbsolutePath()).append('\n');

				builder.append(BRIDGE_OBFS4).append(
						"107.189.14.228:2042 755B8D9967A8E9678C18822AB0C2622057A12AA3 cert=lY6L0qguLEylITkmpst6fzjDagpQLX/zKO4bW/WAlEbJaXLdfXq4Hr3leXpc+7oL7mWULA iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"129.159.251.88:1991 2BA7C7ED96AC40CD373DF721B2D597A64B1234B8 cert=H4Z/Gq779NGUT72BoZJhpgnSIdkLrspwI5TAVZaLLk+AF2Zr3hTOOM8A4bQlor4viePuOQ iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"172.93.54.60:8443 D5E9F7BED027D899F25E34440FEBF7B31107EB71 cert=/2fABoJN05CqJZI14iVMwxMAZEBHLfgTsuU5q1YApW/hc+vZ7WwCTW3fLXYwXiMIn2R8Pg iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"176.221.46.93:43609 8097F8B714216ED95B64C7CC77121FC95C9B10F0 cert=xksSGyfEzCKo4ThTsEie1lEMFGNwTBEjZ4FAJYtblsmUOg81ceK5MgUkXG6ons3vg4ELVA iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"185.177.207.98:12346 00C816688348E151A9DAC033C9F9D7F6E02548CF cert=p9L6+25s8bnfkye1ZxFeAE4mAGY7DH4Gaj7dxngIIzP9BtqrHHwZXdjMK0RVIQ34C7aqZw iat-mode=2")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"212.47.241.81:4443 949C2B99126D3D6FD61FF05D9F333B91B96DECA5 cert=ZM8W4zywbQPpDZix11RXRxrY3P2vawo3yMH/6mGuJzM3btewbdQ9ijFERdvCJBVMlyjpFQ iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"84.255.205.230:9002 6134167A91284AC6C8F913955532D36D8684EAD3 cert=9OoMvE6zKMg2leOguhM+9qiD4FWUJe6P/1eQQ5CkFIZQbpSPcapWgg/F8C2V9TdGWq2/Gw iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"94.242.249.2:52584 2B67FA9653964E35C3CB684CD3C0EADF7BD14D18 cert=Tb0DpYFQIZxk5BiAfthI+En3Q6Q/DFbyqoR5x0NghM8MvxOtyFZGl07PVOuPvqOeoJijKw iat-mode=0")
						.append('\n');
				builder.append(BRIDGE_OBFS4).append(
						"99.242.105.218:80 7BF9B5860BBE4E91E43F03673FE7AB43E72CE353 cert=2qY6IX82EvY2B6e5ahOJj4Unn7Bg0hssx6a+3iUhj2K5MXWT7i052DjqLyJKG7iD5dj/Ig iat-mode=0")
						.append('\n');

			}
			if (snowflakeBin != null) {
				builder.append("ClientTransportPlugin snowflake exec ").append(snowflakeBin.getAbsolutePath()).append(
						" -url https://snowflake-broker.torproject.net.global.prod.fastly.net/ -front cdn.sstatic.net -ice stun:stun.l.google.com:19302,stun:stun.voip.blackberry.com:3478,stun:stun.altar.com.pl:3478,stun:stun.antisip.com:3478,stun:stun.bluesip.net:3478,stun:stun.dus.net:3478,stun:stun.epygi.com:3478,stun:stun.sonetel.com:3478,stun:stun.sonetel.net:3478,stun:stun.stunprotocol.org:3478,stun:stun.uls.co.za:3478,stun:stun.voipgate.com:3478,stun:stun.voys.nl:3478")
						.append(torSocksPort).append('\n');
			}

			// bridges
		}

		if (!TORRC.exists()) {
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

	private String getOsName() {
		if (SystemUtils.IS_OS_WINDOWS) {
			return "Windows";
		}
		if (SystemUtils.IS_OS_LINUX) {
			return "Linux";
		}
		throw new UnsupportedOperationException("OS not : WINDOWS OR LINUX");
	}

	public void init() throws IOException {
		AppHeader.app.dataBase.loadConfig();
		unpack();
		createConfig();
	}

	@Override
	public void run() {
		Thread.currentThread().setName("TorWorker");

		try {
			int statusTor = new ExecutorMaster().setOutputListener(new ExecutorMasterOutputListener() {
				@Override
				public void appendInput(String line) {
					LOGGER.info(line);
				}

				@Override
				public void appendOutput(String line) throws IOException, SQLException {
					LOGGER.info(line);
					if (line.contains("Bootstrapped 100%")) {

						String domain = Files.readString(TORSERVICEHOSTNAMFILE.toPath(), StandardCharsets.UTF_8)
								.replaceAll("[\n]*", "");

						LOGGER.info("domain:[{}]", domain);
						LOGGER.info("domain 10:[{}]", domain.contains("\n"));

						AppHeader.app.torServiceHost(domain);
						AppHeader.app.torStarted = true;
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
			}).parrentCommand(torBin.getAbsolutePath()).arg("-f " + TORRC.getAbsolutePath()).dir(TORBINDIR).call();
			LOGGER.info("tor stop with status:{}", statusTor);
		} catch (Exception e) {
			e.printStackTrace();
			AppHeader.app.active = false;
		}

	}

	private void setPermissionBinFile(File f) throws IOException {
		if (!SystemUtils.IS_OS_WINDOWS) {
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(f.toPath(), LinkOption.NOFOLLOW_LINKS);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.remove(PosixFilePermission.GROUP_WRITE);
			perms.remove(PosixFilePermission.GROUP_READ);
			perms.remove(PosixFilePermission.GROUP_EXECUTE);
			perms.remove(PosixFilePermission.OTHERS_EXECUTE);
			perms.remove(PosixFilePermission.OTHERS_READ);
			perms.remove(PosixFilePermission.OTHERS_WRITE);
			Files.setPosixFilePermissions(f.toPath(), perms);
		}
	}

	private void unpack() throws IOException {
		filesDirName = filesDirName + osName + "." + arch;
		LOGGER.info("----------UNZIP-------------");
		try (ZipFile file = new ZipFile(AppHeader.jarPath)) {
			// Get file entries
			Enumeration<? extends ZipEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();

				if (entryName.startsWith(TORBIN + osName + "." + arch)) {
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
							torBin = f;
							setPermissionBinFile(f);
						} // torbin

						if (f.getName().toLowerCase().startsWith(nameBinSnowflake)) {
							snowflakeBin = f;
							LOGGER.info("found snowflake: {}", f.getAbsolutePath());
							setPermissionBinFile(f);
						}
						if (f.getName().toLowerCase().startsWith(nameBinObfs4proxy)) {
							obfs4proxyBin = f;
							LOGGER.info("found obfs4proxy: {}", f.getAbsolutePath());
							setPermissionBinFile(f);
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
