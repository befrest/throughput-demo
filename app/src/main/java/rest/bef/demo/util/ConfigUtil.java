package rest.bef.demo.util;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static Properties properties;

    static {
        final InputStream is;

        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            LogManager.getLogger().error(e.getMessage(), e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static Integer getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static Long getLong(String key) {
        return Long.valueOf(get(key));
    }

    public static Boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static Properties getProperties() {
        return properties;
    }
}
