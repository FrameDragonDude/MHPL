package dal.dao;

import dal.entities.ExamScoreEntity;
import dal.hibernate.HibernateUtil;
import dto.ExamScoreDTO;
import org.hibernate.Session;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamScoreDAO {

    public int countExamScores(String keyword) throws SQLException {
        String filter = safe(keyword);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                "select count(s.id) from ExamScoreEntity s " +
                "where (:key = '' or s.cccd like :keyLike or s.soBaoDanh like :keyLike)", 
                Long.class
            )
            .setParameter("key", filter)
            .setParameter("keyLike", "%" + filter + "%")
            .uniqueResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception ex) {
            throw asSqlException("đếm tổng số bản ghi điểm thi", ex);
        }
    }

    public List<ExamScoreDTO> findExamScores(String keyword, int page, int pageSize) throws SQLException {
        String filter = safe(keyword);
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<ExamScoreEntity> entities = session.createQuery(
                    "from ExamScoreEntity s "
                            + "where (:key = '' or s.cccd like :keyLike or s.soBaoDanh like :keyLike) "
                            + "order by s.id asc",
                    ExamScoreEntity.class
            )
                    .setParameter("key", filter)
                    .setParameter("keyLike", "%" + filter + "%")
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

    private String safe(String input) {
        return input == null ? "" : input.trim();
    }

    private SQLException asSqlException(String action, Exception ex) {
        return new SQLException("Lỗi khi " + action + ": " + ex.getMessage(), ex);
    }
}