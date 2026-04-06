package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/xettuyen2026?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PASSWORD = "12345";

//	private DBConnection() {
//	}

	public static Connection getConnection() throws SQLException {
		ensureJdbcDriverLoaded();
		String url = getEnvOrDefault("MHPL_DB_URL", DEFAULT_URL);
		String user = getEnvOrDefault("MHPL_DB_USER", DEFAULT_USER);
		String password = getEnvOrDefault("MHPL_DB_PASSWORD", DEFAULT_PASSWORD);
		return DriverManager.getConnection(url, user, password);
	}

	private static void ensureJdbcDriverLoaded() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return;
		} catch (ClassNotFoundException ignored) {
			// Fallback for older connector naming.
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException ex) {
			throw new SQLException(
				"MySQL JDBC driver not found. Add mysql-connector-j*.jar into libs/ and reload Java project.",
				ex
			);
		}
	}

	private static String getEnvOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return value;
	}
}
