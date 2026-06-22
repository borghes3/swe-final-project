package it.polimi.ingsw.am23.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read-only configuration for the JDBC connection used by the leaderboard
 * repository. Values are resolved with the following precedence:
 * system properties (with the {@code mesos.db.} prefix), the
 * {@code db.properties} resource on the classpath, hard-coded defaults.
 */
public final class DatabaseConfig {

    // Path of the optional configuration resource
    private static final String RESOURCE_PATH = "/db.properties";

    private final String url;
    private final String user;
    private final String password;
    private final String driverClass;

    private DatabaseConfig(String url, String user, String password, String driverClass) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driverClass = driverClass;
    }

    /**
     * Loads the configuration applying the documented precedence rules.
     *
     * @return the resolved configuration; may be {@link #isValid() invalid}
     *         if the URL is missing
     */
    public static DatabaseConfig load() {
        Properties p = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ignored) {
            // No properties file found: fall back to defaults
        }
        String url = System.getProperty("mesos.db.url", p.getProperty("db.url", ""));
        String user = System.getProperty("mesos.db.user", p.getProperty("db.user", ""));
        String password = System.getProperty("mesos.db.password", p.getProperty("db.password", ""));
        String driver = System.getProperty("mesos.db.driver", p.getProperty("db.driver", "org.postgresql.Driver"));
        return new DatabaseConfig(url, user, password, driver);
    }

    /** @return the JDBC connection URL */
    public String url() { return url; }
    /** @return the database user */
    public String user() { return user; }
    /** @return the database password */
    public String password() { return password; }
    /** @return the fully-qualified JDBC driver class name */
    public String driverClass() { return driverClass; }

    /**
     * @return {@code true} if the configuration contains a non blank URL,
     *         which is the minimum required to attempt a connection
     */
    public boolean isValid() {
        return url != null && !url.isBlank();
    }
}
