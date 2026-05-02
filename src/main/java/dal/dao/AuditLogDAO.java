package dal.dao;

import dal.entities.AuditLogEntity;
import dal.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class AuditLogDAO {
    private static final int PAGE_SIZE = 50;

    public boolean logAction(AuditLogEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi ghi audit log: " + ex.getMessage());
            return false;
        }
    }

    public List<AuditLogEntity> findByPage(String username, String action, String module, int page) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM AuditLogEntity WHERE 1=1");
            
            if (username != null && !username.isEmpty()) {
                hql.append(" AND username LIKE :username");
            }
            if (action != null && !action.isEmpty()) {
                hql.append(" AND action LIKE :action");
            }
            if (module != null && !module.isEmpty()) {
                hql.append(" AND module LIKE :module");
            }
            hql.append(" ORDER BY createdAt DESC");
            
            Query<AuditLogEntity> query = session.createQuery(hql.toString(), AuditLogEntity.class);
            if (username != null && !username.isEmpty()) {
                query.setParameter("username", "%" + username + "%");
            }
            if (action != null && !action.isEmpty()) {
                query.setParameter("action", "%" + action + "%");
            }
            if (module != null && !module.isEmpty()) {
                query.setParameter("module", "%" + module + "%");
            }
            query.setFirstResult((page - 1) * PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            return query.list();
        } catch (Exception ex) {
            System.err.println("Lỗi tìm audit log: " + ex.getMessage());
            return List.of();
        }
    }

    public List<AuditLogEntity> findByDateRange(LocalDateTime start, LocalDateTime end, int page) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity WHERE createdAt >= :start AND createdAt <= :end ORDER BY createdAt DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            query.setFirstResult((page - 1) * PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            return query.list();
        } catch (Exception ex) {
            System.err.println("Lỗi tìm audit log theo khoảng thời gian: " + ex.getMessage());
            return List.of();
        }
    }

    public int countRows(String username, String action, String module) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM AuditLogEntity WHERE 1=1");
            
            if (username != null && !username.isEmpty()) {
                hql.append(" AND username LIKE :username");
            }
            if (action != null && !action.isEmpty()) {
                hql.append(" AND action LIKE :action");
            }
            if (module != null && !module.isEmpty()) {
                hql.append(" AND module LIKE :module");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            if (username != null && !username.isEmpty()) {
                query.setParameter("username", "%" + username + "%");
            }
            if (action != null && !action.isEmpty()) {
                query.setParameter("action", "%" + action + "%");
            }
            if (module != null && !module.isEmpty()) {
                query.setParameter("module", "%" + module + "%");
            }
            return query.getSingleResult().intValue();
        } catch (Exception ex) {
            System.err.println("Lỗi đếm audit log: " + ex.getMessage());
            return 0;
        }
    }

    public List<AuditLogEntity> getAllLogs() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity ORDER BY createdAt DESC";
            return session.createQuery(hql, AuditLogEntity.class).list();
        } catch (Exception ex) {
            System.err.println("Lỗi lấy tất cả audit log: " + ex.getMessage());
            return List.of();
        }
    }
}