import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final Dotenv dotenv = Dotenv.load();

    private final String URL = dotenv.get("DB_URL") + dotenv.get("DB_NAME");
    private final String PASSWORD = dotenv.get("DB_PASSWORD");
    private final String USER = dotenv.get("DB_USER");

    public Connection getDBConnection() {
        try (
                Connection connection = DriverManager.getConnection(this.URL, this.USER, this.PASSWORD);
        ) {
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
