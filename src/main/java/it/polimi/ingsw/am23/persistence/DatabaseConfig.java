package it.polimi.ingsw.am23.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConfig {

    // DB env configuration
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

    public static DatabaseConfig load() {
        Properties p = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ignored) {
            // if no properties fallback to default
        }
        String url = System.getProperty("mesos.db.url", p.getProperty("db.url", ""));
        String user = System.getProperty("mesos.db.user", p.getProperty("db.user", ""));
        String password = System.getProperty("mesos.db.password", p.getProperty("db.password", ""));
        String driver = System.getProperty("mesos.db.driver", p.getProperty("db.driver", "org.postgresql.Driver"));
        return new DatabaseConfig(url, user, password, driver);
    }

    public String url() { return url; }
    public String user() { return user; }
    public String password() { return password; }
    public String driverClass() { return driverClass; }

    public boolean isValid() {
        return url != null && !url.isBlank();
    }
}
