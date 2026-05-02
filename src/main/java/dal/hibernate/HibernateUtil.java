package dal.hibernate;

import dal.entities.CandidateEntity;
import dal.entities.AspirationEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.NganhEntity;
import dal.entities.ToHopMon;
import dal.entities.UserEntity;
import dal.entities.RoleEntity;
import dal.entities.PermissionEntity;
import dal.entities.AuditLogEntity;
import dal.entities.UuTienXetTuyenEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static StandardServiceRegistry registry;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");

                // Các entity của hệ thống
                configuration.addAnnotatedClass(UserEntity.class);
                configuration.addAnnotatedClass(CandidateEntity.class);
                configuration.addAnnotatedClass(AspirationEntity.class);
                configuration.addAnnotatedClass(ExamScoreEntity.class);
                configuration.addAnnotatedClass(ToHopMon.class);
                configuration.addAnnotatedClass(NganhEntity.class);
                configuration.addAnnotatedClass(RoleEntity.class);
                configuration.addAnnotatedClass(PermissionEntity.class);
                configuration.addAnnotatedClass(AuditLogEntity.class);
                configuration.addAnnotatedClass(UuTienXetTuyenEntity.class);

                registry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(registry);
            } catch (Exception ex) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                String detail = ex.getMessage() == null ? "khong ro nguyen nhan" : ex.getMessage();
                throw new RuntimeException("Khoi tao SessionFactory that bai: " + detail, ex);
            }
        }

        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
