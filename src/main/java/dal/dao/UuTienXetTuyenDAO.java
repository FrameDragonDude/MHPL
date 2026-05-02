package dal.dao;

import dal.entities.UuTienXetTuyenEntity;
import dal.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class UuTienXetTuyenDAO {
    private static final int PAGE_SIZE = 20;

    public boolean create(UuTienXetTuyenEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi tạo ưu tiên xét tuyển: " + ex.getMessage());
            return false;
        }
    }

    public List<UuTienXetTuyenEntity> findByPage(String searchCccd, String searchGiai, int page) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM UuTienXetTuyenEntity WHERE 1=1");
            
            if (searchCccd != null && !searchCccd.isEmpty()) {
                hql.append(" AND tsCccd LIKE :cccd");
            }
            if (searchGiai != null && !searchGiai.isEmpty()) {
                hql.append(" AND loaiGiai LIKE :giai");
            }
            hql.append(" ORDER BY createdAt DESC");
            
            Query<UuTienXetTuyenEntity> query = session.createQuery(hql.toString(), UuTienXetTuyenEntity.class);
            if (searchCccd != null && !searchCccd.isEmpty()) {
                query.setParameter("cccd", "%" + searchCccd + "%");
            }
            if (searchGiai != null && !searchGiai.isEmpty()) {
                query.setParameter("giai", "%" + searchGiai + "%");
            }
            query.setFirstResult((page - 1) * PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            return query.list();
        } catch (Exception ex) {
            System.err.println("Lỗi tìm ưu tiên xét tuyển: " + ex.getMessage());
            return List.of();
        }
    }

    public int countRows(String searchCccd, String searchGiai) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(*) FROM UuTienXetTuyenEntity WHERE 1=1");
            
            if (searchCccd != null && !searchCccd.isEmpty()) {
                hql.append(" AND tsCccd LIKE :cccd");
            }
            if (searchGiai != null && !searchGiai.isEmpty()) {
                hql.append(" AND loaiGiai LIKE :giai");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            if (searchCccd != null && !searchCccd.isEmpty()) {
                query.setParameter("cccd", "%" + searchCccd + "%");
            }
            if (searchGiai != null && !searchGiai.isEmpty()) {
                query.setParameter("giai", "%" + searchGiai + "%");
            }
            return query.getSingleResult().intValue();
        } catch (Exception ex) {
            System.err.println("Lỗi đếm ưu tiên xét tuyển: " + ex.getMessage());
            return 0;
        }
    }

    public boolean update(UuTienXetTuyenEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(entity);
            session.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi cập nhật ưu tiên xét tuyển: " + ex.getMessage());
            return false;
        }
    }

    public boolean delete(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            UuTienXetTuyenEntity entity = session.get(UuTienXetTuyenEntity.class, id);
            if (entity != null) {
                session.delete(entity);
                session.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception ex) {
            System.err.println("Lỗi xóa ưu tiên xét tuyển: " + ex.getMessage());
            return false;
        }
    }

    public boolean upsertByKey(UuTienXetTuyenEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            session.beginTransaction();
            String hql = "FROM UuTienXetTuyenEntity WHERE utxtKeys = :key";
            Query<UuTienXetTuyenEntity> query = session.createQuery(hql, UuTienXetTuyenEntity.class);
            query.setParameter("key", entity.getUtxtKeys());
            UuTienXetTuyenEntity existing = query.uniqueResult();
            
            if (existing != null) {
                existing.setCapQuocGia(entity.getCapQuocGia());
                existing.setDoiTuyen(entity.getDoiTuyen());
                existing.setMaMon(entity.getMaMon());
                existing.setLoaiGiai(entity.getLoaiGiai());
                existing.setDiemCongMonDatMc(entity.getDiemCongMonDatMc());
                existing.setDiemCongKhongMonDatMc(entity.getDiemCongKhongMonDatMc());
                existing.setCoChungChi(entity.getCoChungChi());
                existing.setGhiChu(entity.getGhiChu());
                session.merge(existing);
            } else {
                session.save(entity);
            }
            session.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi upsert ưu tiên xét tuyển: " + ex.getMessage());
            return false;
        }
    }
}
