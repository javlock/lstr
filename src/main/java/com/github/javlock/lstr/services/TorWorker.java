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
	private static final Logger LOGGER = LoggerFactory.getLogger("TorWorker");

	private static final String TORBIN = "torbin/";

	private File TORBINDIR = new File(new File(AppHeader.DIR, TORBIN).getAbsolutePath());
	private File TORRC = new File(TORBINDIR, "torrc");
	private File TORDATADIR = new File(TORBINDIR, "data");
	private File TORSERVICEDIR = new File(TORDATADIR, "service");
	private File TORSERVICEHOSTNAMFILE = new File(TORSERVICEDIR, "hostname");

	String filesDirName = TORBIN;
	String osName = getOsName();

	String arch = SystemUtils.OS_ARCH;

	String nameBin = "tor";
	private File torBin;

	private void createConfig() throws IOException {
		int torSocksPort = AppHeader.getConfig().getTorSocksPort();
		int serverPort = AppHeader.getConfig().getServerPort();

		StringBuilder builder = new StringBuilder();

		builder.append("Log notice stdout").append('\n');
		builder.append("DataDirectory ").append(TORDATADIR.getAbsolutePath()).append('\n');

		builder.append("HiddenServiceDir ").append(TORSERVICEDIR.getAbsolutePath()).append('\n');
		builder.append("HiddenServicePort 4001 127.0.0.1:" + serverPort).append('\n');

		builder.append("SOCKSPort ").append(torSocksPort).append('\n');
		if (!TORRC.exists()) {
			Files.createFile(TORRC.toPath());
		} else {
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
		Files.writeString(TORRC.toPath(), builder, StandardOpenOption.TRUNCATE_EXISTING);
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
