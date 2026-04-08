package dal.hibernate;

import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.Nganh;
import dal.entities.ToHopMon;
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
                
                // tạo entity mới thì phải add vào đây trước thì mới sử dụng được
                configuration.addAnnotatedClass(CandidateEntity.class);
                configuration.addAnnotatedClass(ExamScoreEntity.class);
                configuration.addAnnotatedClass(ToHopMon.class);
                configuration.addAnnotatedClass(Nganh.class);

                
                registry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(registry);
            } catch (Exception ex) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                throw new RuntimeException("Khoi tao SessionFactory that bai", ex);
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
