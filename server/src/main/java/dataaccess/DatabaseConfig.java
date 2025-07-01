package dataaccess;

import java.util.Properties;

public class DatabaseConfig {
    public String name;
    public String user;
    public String password;
    public String url;


    public DatabaseConfig() {
        loadPropertiesFromResources();
    }

    public DatabaseConfig(String name) {
        this();
        this.name = name;
    }


    private void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private void loadProperties(Properties props) {
        name = props.getProperty("db.name");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        url = String.format("jdbc:mysql://%s:%d", host, port);
    }

    public String toString() {
        return String.format("%s@%s", user, name);
    }
}
