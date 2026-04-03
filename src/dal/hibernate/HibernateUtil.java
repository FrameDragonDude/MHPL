package dal.hibernate;

import dal.entities.AspirationEntity;
import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.PermissionEntity;
import dal.entities.RoleEntity;
import dal.entities.RolePermissionEntity;
import dal.entities.UserEntity;
import dal.entities.UserRoleEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

public final class HibernateUtil {

	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/xettuyen2026?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PASSWORD = "12345";

	private static SessionFactory sessionFactory;

	private HibernateUtil() {
	}

	public static synchronized SessionFactory getSessionFactory() {
		if (sessionFactory != null) {
			return sessionFactory;
		}

		Map<String, Object> settings = new HashMap<>();
		settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
		settings.put(Environment.URL, getEnvOrDefault("MHPL_DB_URL", DEFAULT_URL));
		settings.put(Environment.USER, getEnvOrDefault("MHPL_DB_USER", DEFAULT_USER));
		settings.put(Environment.PASS, getEnvOrDefault("MHPL_DB_PASSWORD", DEFAULT_PASSWORD));
		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
		settings.put(Environment.SHOW_SQL, getEnvOrDefault("MHPL_HIBERNATE_SHOW_SQL", "false"));
		settings.put(Environment.FORMAT_SQL, getEnvOrDefault("MHPL_HIBERNATE_FORMAT_SQL", "false"));
		settings.put(Environment.HBM2DDL_AUTO, "none");
		settings.put(Environment.POOL_SIZE, getEnvOrDefault("MHPL_HIBERNATE_POOL_SIZE", "10"));
		settings.put(Environment.STATEMENT_BATCH_SIZE, getEnvOrDefault("MHPL_HIBERNATE_BATCH_SIZE", "30"));
		settings.put(Environment.ORDER_INSERTS, "true");
		settings.put(Environment.ORDER_UPDATES, "true");
		settings.put(Environment.JDBC_TIME_ZONE, "UTC");

		StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySettings(settings)
				.build();

		Metadata metadata = new MetadataSources(registry)
				.addAnnotatedClass(CandidateEntity.class)
				.addAnnotatedClass(ExamScoreEntity.class)
				.addAnnotatedClass(AspirationEntity.class)
				.addAnnotatedClass(UserEntity.class)
				.addAnnotatedClass(RoleEntity.class)
				.addAnnotatedClass(PermissionEntity.class)
				.addAnnotatedClass(UserRoleEntity.class)
				.addAnnotatedClass(RolePermissionEntity.class)
				.buildMetadata();

		sessionFactory = metadata.buildSessionFactory();
		return sessionFactory;
	}

	public static synchronized void shutdown() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
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
