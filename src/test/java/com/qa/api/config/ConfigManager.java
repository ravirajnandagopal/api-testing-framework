package com.qa.api.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration from src/test/resources/config.properties and allows
 * any value to be overridden by a system property or environment variable,
 * which is handy in CI (e.g. -Dbase.uri=... or BASE_URI=...).
 */
public final class ConfigManager {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on the classpath");
            }
            PROPS.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConfigManager() {
    }

    public static String get(String key) {
        // Precedence: system property -> environment variable -> properties file.
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String envKey = key.toUpperCase().replace('.', '_');
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(key);
    }
}
