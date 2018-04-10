package rest.bef.demo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static String getFileContent(String filename) {
        StringBuilder content = new StringBuilder();

        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String line;

            while ((line = reader.readLine()) != null)
                content.append(line).append(System.getProperty("line.separator"));

            return content.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public static void writeOnDisk(InputStream stream, Path location, Path destDir) {
        try {
            Files.createDirectories(destDir);
            Files.deleteIfExists(location);
            Files.copy(stream, location);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}