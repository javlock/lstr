package com.github.javlock.lstr.v2.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.v2.AppHeader;

public class FileUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger("FileUtils");

	public static void findJarFile(File input) throws URISyntaxException {
		input = new File(input.getAbsolutePath());
		input = input.getParentFile();
		try (Stream<Path> stream = Files.find(input.toPath(), 100, (path, basicFileAttributes) -> {
			File file = path.toFile();
			if (!file.canRead()) {
				LOGGER.warn("cant read: {}", file);
				return false;
			}
			return !file.isDirectory() && file.getName().endsWith("-jar-with-dependencies.jar");
		})) {
			Optional<Path> optional = stream.parallel().findFirst();
			if (optional.isPresent()) {
				File file = optional.get().toFile();
				LOGGER.info("exe jar FOUND: {}", file);
				AppHeader.JARFILE = file;
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		LOGGER.info("try search via ProtectionDomain");
		File file = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		LOGGER.info("{}", file);
		if (file.isFile()) {
			AppHeader.JARFILE = file;
			return;
		}

		LOGGER.error("exe jar not found");
		Runtime.getRuntime().exit(2);
	}

	public static void initObfs4List() {
		AppHeader.obfs4List.add(
				"107.189.14.228:2042 755B8D9967A8E9678C18822AB0C2622057A12AA3 cert=lY6L0qguLEylITkmpst6fzjDagpQLX/zKO4bW/WAlEbJaXLdfXq4Hr3leXpc+7oL7mWULA iat-mode=0");
		AppHeader.obfs4List.add(
				"129.159.251.88:1991 2BA7C7ED96AC40CD373DF721B2D597A64B1234B8 cert=H4Z/Gq779NGUT72BoZJhpgnSIdkLrspwI5TAVZaLLk+AF2Zr3hTOOM8A4bQlor4viePuOQ iat-mode=0");
		AppHeader.obfs4List.add(
				"172.93.54.60:8443 D5E9F7BED027D899F25E34440FEBF7B31107EB71 cert=/2fABoJN05CqJZI14iVMwxMAZEBHLfgTsuU5q1YApW/hc+vZ7WwCTW3fLXYwXiMIn2R8Pg iat-mode=0");
		AppHeader.obfs4List.add(
				"176.221.46.93:43609 8097F8B714216ED95B64C7CC77121FC95C9B10F0 cert=xksSGyfEzCKo4ThTsEie1lEMFGNwTBEjZ4FAJYtblsmUOg81ceK5MgUkXG6ons3vg4ELVA iat-mode=0");
		AppHeader.obfs4List.add(
				"185.141.27.108:31337 E315A7F8A5E2C1B4819D09D79C5A627D65C182AB cert=p2GkSqmSJk9pdARY3cqiQEYCbhkWl7eZ1XpYi2EA/vw09FVtRz1VzzsSHFeUTM1TwE7afQ iat-mode=0");
		AppHeader.obfs4List.add(
				"185.177.207.98:12346 00C816688348E151A9DAC033C9F9D7F6E02548CF cert=p9L6+25s8bnfkye1ZxFeAE4mAGY7DH4Gaj7dxngIIzP9BtqrHHwZXdjMK0RVIQ34C7aqZw iat-mode=2");
		AppHeader.obfs4List.add(
				"185.178.93.141:9060 05012FE073A4B522580E6FE77F3FD92E1ABF6FD1 cert=377UalzVX10eI5raeIVmJFFvq0FpKEF12igXoKe2rMJWGJxScKzMm9dph9m4v8hz0y3hDg iat-mode=0");
		AppHeader.obfs4List.add(
				"192.241.152.208:444 05277333D6A14B6E706CF5A189C57C97C471189B cert=2j8mktwt8pA4LGmqBUFeDfkGbVE0U0MqYBIbt6/ePMRrlrk9UhHQeeyw8WqgGPa1xYzyaQ iat-mode=0");
		AppHeader.obfs4List.add(
				"212.47.241.81:4443 949C2B99126D3D6FD61FF05D9F333B91B96DECA5 cert=ZM8W4zywbQPpDZix11RXRxrY3P2vawo3yMH/6mGuJzM3btewbdQ9ijFERdvCJBVMlyjpFQ iat-mode=0");
		AppHeader.obfs4List.add(
				"24.252.46.193:12346 BBA0AA74D556D2E8FD70F69E50FB90B3188BA578 cert=Hnhac7+nXnLJIFrTapvnmkSibMvzbb4yP32EsCesmyqCZW8s6B6zRIsZS9M5MkUSPO2fdA iat-mode=0");
		AppHeader.obfs4List.add(
				"64.86.168.59:5223 4F53709C4A798A66646E4F5BBDD9D1612F098274 cert=caoiyXyLS0KkIpxxUgeouRVTRHlENTB5/hFOrhT+mIjskEqAem2zQ0au6eaOceR51isJOA iat-mode=0");
		AppHeader.obfs4List.add(
				"82.64.20.253:7800 B27592A9DA08DCB871F14F9247299F9FBA72D05F cert=g+x8k6Fn0V2zIESkJ05jWOANrEfj5T6HofdDcSfqqUGJGxqzjCZmt6R51yXbpz90JPS2OA iat-mode=0");
		AppHeader.obfs4List.add(
				"84.255.205.230:9002 6134167A91284AC6C8F913955532D36D8684EAD3 cert=9OoMvE6zKMg2leOguhM+9qiD4FWUJe6P/1eQQ5CkFIZQbpSPcapWgg/F8C2V9TdGWq2/Gw iat-mode=0");
		AppHeader.obfs4List.add(
				"94.242.249.2:52584 2B67FA9653964E35C3CB684CD3C0EADF7BD14D18 cert=Tb0DpYFQIZxk5BiAfthI+En3Q6Q/DFbyqoR5x0NghM8MvxOtyFZGl07PVOuPvqOeoJijKw iat-mode=0");
		AppHeader.obfs4List.add(
				"99.242.105.218:80 7BF9B5860BBE4E91E43F03673FE7AB43E72CE353 cert=2qY6IX82EvY2B6e5ahOJj4Unn7Bg0hssx6a+3iUhj2K5MXWT7i052DjqLyJKG7iD5dj/Ig iat-mode=0");

	}

	public static void setPermissionBinFile(File f) throws IOException {
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

	private FileUtils() {
	}
}
