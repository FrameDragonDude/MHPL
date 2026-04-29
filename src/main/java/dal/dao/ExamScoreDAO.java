package dal.dao;

import dal.entities.ExamScoreEntity;
import dal.hibernate.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import dto.ExamScoreDTO;
import dto.MethodStatDTO;
import dto.ScoreStatDTO;
import java.math.BigDecimal;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamScoreDAO {

    public int countExamScores(String keyword, String filter) throws SQLException {
        String key = safe(keyword);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                "select count(s.id) from ExamScoreEntity s " +
                "where (:key = '' or s.cccd like :keyLike or s.soBaoDanh like :keyLike) " +
                "and (:pt is null or s.dPhuongThuc = :pt)",
                Long.class
            )
            .setParameter("key", key)
            .setParameter("keyLike", "%" + key + "%")
            .setParameter("pt", filter)
            .uniqueResult();

            return total == null ? 0 : total.intValue();
        } catch (Exception ex) {
            throw asSqlException("đếm tổng số bản ghi điểm thi", ex);
        }
    }

    public List<ExamScoreDTO> findExamScores(String keyword, String filter, int page, int pageSize) throws SQLException {
         String key = safe(keyword);
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<ExamScoreEntity> entities = session.createQuery(
                    "from ExamScoreEntity s " +
                    "where (:key = '' or s.cccd like :keyLike or s.soBaoDanh like :keyLike) " +
                    "and (:pt is null or s.dPhuongThuc = :pt) " +
                    "order by s.id asc",
                    ExamScoreEntity.class
            )
            .setParameter("key", key)
            .setParameter("keyLike", "%" + key + "%")
            .setParameter("pt", filter)
            .setFirstResult(offset)
            .setMaxResults(pageSize)
            .list();

            List<ExamScoreDTO> results = new ArrayList<>();
            for (ExamScoreEntity entity : entities) {
                results.add(mapToDTO(entity));
            }

            return results;
        } catch (Exception ex) {
            throw asSqlException("tải danh sách điểm thi", ex);
        }
    }

    public ExamScoreDTO findExamScoreById(int id) throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ExamScoreEntity entity = session.find(ExamScoreEntity.class, id);
            
            if (entity == null) return null;
            
            return mapToDTO(entity);
        } catch (Exception ex) {
            throw asSqlException("tìm kiếm điểm thi theo ID", ex);
        }
    }

    public void insert(ExamScoreDTO dto) throws SQLException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ExamScoreEntity entity = mapToEntity(dto);
            session.persist(entity);
            transaction.commit();
        } catch (ConstraintViolationException cve) {
            if (transaction != null) transaction.rollback();
            throw new SQLException("CCCD đã tồn tại!", cve);

        } catch (Exception ex) {
            if (transaction != null) transaction.rollback();
            throw asSqlException("thêm mới điểm thi", ex);
        }
    }

    public void update(ExamScoreDTO dto) throws SQLException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ExamScoreEntity entity = session.find(ExamScoreEntity.class, dto.getIdDiemThi());
            if (entity != null) {
                entity.setCccd(dto.getCccd());
                entity.setSoBaoDanh(dto.getSoBaoDanh());
                entity.setDPhuongThuc(dto.getPhuongThuc());
                entity.setDiemTo(dto.getDiemTo());
                entity.setDiemVa(dto.getDiemVa());
                entity.setDiemLi(dto.getDiemLi());
                entity.setDiemHo(dto.getDiemHo());
                entity.setDiemSi(dto.getDiemSi());
                entity.setDiemSu(dto.getDiemSu());
                entity.setDiemDi(dto.getDiemDi());
                entity.setDiemN1Thi(dto.getDiemN1Thi());
                entity.setDiemN1Cc(dto.getDiemN1Cc());
                entity.setDiemTi(dto.getDiemTi());
                entity.setDiemKtpl(dto.getDiemKtpl());
                entity.setDiemCncn(dto.getDiemCncn());
                entity.setDiemCnnn(dto.getDiemCnnn());
                entity.setDiemNl1(dto.getDiemNl1());
                entity.setDiemNk1(dto.getDiemNk1());
                entity.setDiemNk2(dto.getDiemNk2());
            } else {
                transaction.rollback();
                throw new SQLException("Không tìm thấy điểm thi với ID: " + dto.getIdDiemThi());
            }
            transaction.commit();
        } catch (ConstraintViolationException cve) {
            if (transaction != null) transaction.rollback();
            throw new SQLException("CCCD đã tồn tại!", cve);

        } catch (Exception ex) {
            if (transaction != null) transaction.rollback();
            throw asSqlException("cập nhật điểm thi", ex);
        }
    }

    public void delete(int id) throws SQLException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ExamScoreEntity entity = session.find(ExamScoreEntity.class, id);
            if (entity != null) {
                session.remove(entity);
            } else {
                transaction.rollback();
                throw new SQLException("Không tìm thấy điểm thi với ID: " + id);
            }
            
            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) transaction.rollback();
            throw asSqlException("xóa điểm thi", ex);
        }
    }

    public List<MethodStatDTO> statByMethod() throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            List<Object[]> rows = session.createQuery(
                "select s.dPhuongThuc, count(s.id) " +
                "from ExamScoreEntity s " +
                "group by s.dPhuongThuc",
                Object[].class
            ).list();

            List<MethodStatDTO> result = new ArrayList<>();

            for (Object[] row : rows) {
                String method = row[0] == null ? "Khác" : (String) row[0];
                Long count = (Long) row[1];

                result.add(new MethodStatDTO(method, count));
            }

            return result;

        } catch (Exception ex) {
            throw asSqlException("thống kê theo phương thức", ex);
        }
    }

    public List<ScoreStatDTO> statBySubject(String field) throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            String hql = "select s." + field + ", count(s.id) " +
                        "from ExamScoreEntity s " +
                        "group by s." + field + " order by s." + field;

            List<Object[]> rows = session.createQuery(hql, Object[].class).list();

            List<ScoreStatDTO> result = new ArrayList<>();

            for (Object[] row : rows) {
                BigDecimal diemBD = (BigDecimal) row[0];
                Double diem = diemBD != null ? diemBD.doubleValue() : null;
                Long count = (Long) row[1];
                result.add(new ScoreStatDTO(diem, count));
            }

            return result;

        } catch (Exception ex) {
            throw asSqlException("thống kê theo môn", ex);
        }
    }

    private ExamScoreDTO mapToDTO(ExamScoreEntity entity) {
        if (entity == null) return null;
        
        ExamScoreDTO dto = new ExamScoreDTO();
        dto.setIdDiemThi(entity.getId() != null ? entity.getId() : 0);
        dto.setCccd(entity.getCccd());
        dto.setSoBaoDanh(entity.getSoBaoDanh());
        dto.setPhuongThuc(entity.getDPhuongThuc());
        dto.setDiemTo(entity.getDiemTo());
        dto.setDiemVa(entity.getDiemVa());
        dto.setDiemLi(entity.getDiemLi());
        dto.setDiemHo(entity.getDiemHo());
        dto.setDiemSi(entity.getDiemSi());
        dto.setDiemSu(entity.getDiemSu());
        dto.setDiemDi(entity.getDiemDi());
        dto.setDiemN1Thi(entity.getDiemN1Thi());
        dto.setDiemN1Cc(entity.getDiemN1Cc());
        dto.setDiemTi(entity.getDiemTi());
        dto.setDiemKtpl(entity.getDiemKtpl());
        dto.setDiemCncn(entity.getDiemCncn());
        dto.setDiemCnnn(entity.getDiemCnnn());
        dto.setDiemNl1(entity.getDiemNl1());
        dto.setDiemNk1(entity.getDiemNk1());
        dto.setDiemNk2(entity.getDiemNk2());
        
        return dto;
    }

    private ExamScoreEntity mapToEntity(ExamScoreDTO dto) {
        ExamScoreEntity entity = new ExamScoreEntity();
        entity.setCccd(dto.getCccd());
        entity.setSoBaoDanh(dto.getSoBaoDanh());
        entity.setDPhuongThuc(dto.getPhuongThuc());
        entity.setDiemTo(dto.getDiemTo());
        entity.setDiemVa(dto.getDiemVa());
        entity.setDiemLi(dto.getDiemLi());
        entity.setDiemHo(dto.getDiemHo());
        entity.setDiemSi(dto.getDiemSi());
        entity.setDiemSu(dto.getDiemSu());
        entity.setDiemDi(dto.getDiemDi());
        entity.setDiemN1Thi(dto.getDiemN1Thi());
        entity.setDiemN1Cc(dto.getDiemN1Cc());
        entity.setDiemTi(dto.getDiemTi());
        entity.setDiemKtpl(dto.getDiemKtpl());
        entity.setDiemCncn(dto.getDiemCncn());
        entity.setDiemCnnn(dto.getDiemCnnn());
        entity.setDiemNl1(dto.getDiemNl1());
        entity.setDiemNk1(dto.getDiemNk1());
        entity.setDiemNk2(dto.getDiemNk2());
        return entity;
    }

    private String safe(String input) {
        return input == null ? "" : input.trim();
    }

    private SQLException asSqlException(String action, Exception ex) {
        return new SQLException("Lỗi khi " + action + ": " + ex.getMessage(), ex);
    }
}