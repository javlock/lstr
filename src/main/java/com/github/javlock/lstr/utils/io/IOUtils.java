package com.github.javlock.lstr.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger("IOUtils");

	public static ArrayList<String> readLinesFromResourceFile(String file) throws IOException {
		ArrayList<String> result = new ArrayList<>();
		ClassLoader classLoader = IOUtils.class.getClassLoader();
		try (BufferedReader listFile = new BufferedReader(
				new InputStreamReader(classLoader.getResourceAsStream(file), StandardCharsets.UTF_8))) {
			String assetResource = null;
			while ((assetResource = listFile.readLine()) != null) {
				LOGGER.info("2:{}", assetResource);
				result.add(assetResource);
				// Path assetFile = assetDir.resolve(assetResource);
				// Files.createDirectories(assetFile.getParent());
				// try (InputStream asset = getClass().getResourceAsStream(assetResource)) {
				// Files.copy(asset, assetFile);
				// }
			}
		}
		return result;
	}

}
