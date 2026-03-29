package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/xettuyen2026?useSSL=false&serverTimezone=UTC";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PASSWORD = "";

	private DBConnection() {
	}

	public static Connection getConnection() throws SQLException {
		String url = getEnvOrDefault("MHPL_DB_URL", DEFAULT_URL);
		String user = getEnvOrDefault("MHPL_DB_USER", DEFAULT_USER);
		String password = getEnvOrDefault("MHPL_DB_PASSWORD", DEFAULT_PASSWORD);
		return DriverManager.getConnection(url, user, password);
	}

	private static String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return value;
	}
}
