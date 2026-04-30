package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.NguyenVongDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class NguyenVongDAO {
    public int countRows(String cccdKeyword, String truongKeyword) throws SQLException {
        String cccd = safe(cccdKeyword);
        String truong = safe(truongKeyword);

        String sql = """
                        select count(*)
                        from xt_nguyen_vong n
                        where (:cccd = '' or n.cccd like :cccdLike)
                            and (:truong = '' or n.tentruong like :truongLike)
            """;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Number result = (Number) session.createNativeQuery(sql)
                    .setParameter("cccd", cccd)
                    .setParameter("cccdLike", "%" + cccd + "%")
                    .setParameter("truong", truong)
                    .setParameter("truongLike", "%" + truong + "%")
                    .getSingleResult();
            return result == null ? 0 : result.intValue();
        } catch (Exception ex) {
            throw asSqlException("dem nguyen vong", ex);
        }
    }
    public NguyenVongDAO() {
        ensureTable();
    }

    public List<NguyenVongDTO> findRows(String cccdKeyword, String truongKeyword, int page, int pageSize) throws SQLException {
        String cccd = safe(cccdKeyword);
        String truong = safe(truongKeyword);
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

        String sql = """
                        select n.id, n.cccd, n.thutunv, n.matruong, n.tentruong, n.maxettuyen, n.tenmaxettuyen, n.nguyenvongtuyenthang
            from xt_nguyen_vong n
            where (:cccd = '' or n.cccd like :cccdLike)
              and (:truong = '' or n.tentruong like :truongLike)
            order by n.id asc
            limit :limitValue offset :offsetValue
            """;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session.createNativeQuery(sql)
                    .setParameter("cccd", cccd)
                    .setParameter("cccdLike", "%" + cccd + "%")
                    .setParameter("truong", truong)
                    .setParameter("truongLike", "%" + truong + "%")
                    .setParameter("limitValue", Math.max(pageSize, 1))
                    .setParameter("offsetValue", Math.max(offset, 0))
                    .list();

            List<NguyenVongDTO> results = new ArrayList<>();
            for (Object[] row : rows) {
                NguyenVongDTO dto = new NguyenVongDTO();
                dto.setId(toInt(row[0]));
                dto.setCccd(toStr(row[1]));
                dto.setThuTuNV(toInt(row[2]));
                dto.setMaTruong(toStr(row[3]));
                dto.setTenTruong(toStr(row[4]));
                dto.setMaXetTuyen(toStr(row[5]));
                dto.setTenMaXetTuyen(toStr(row[6]));
                dto.setNguyenVongThang(toStr(row[7]));
                results.add(dto);
            }
            return results;
        } catch (Exception ex) {
            throw asSqlException("tai danh sach nguyen vong", ex);
        }
    }

    public boolean create(NguyenVongDTO dto) throws SQLException {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeMutationQuery(
                    "insert into xt_nguyen_vong (cccd, thutunv, matruong, tentruong, maxettuyen, tenmaxettuyen, nguyenvongtuyenthang) values (:cccd, :thutunv, :matruong, :tentruong, :maxettuyen, :tenmaxettuyen, :nguyenvongtuyenthang)")
                    .setParameter("cccd", safeNullable(dto.getCccd()))
                    .setParameter("thutunv", dto.getThuTuNV())
                    .setParameter("matruong", safeNullable(dto.getMaTruong()))
                    .setParameter("tentruong", safeNullable(dto.getTenTruong()))
                    .setParameter("maxettuyen", safeNullable(dto.getMaXetTuyen()))
                    .setParameter("tenmaxettuyen", safeNullable(dto.getTenMaXetTuyen()))
                    .setParameter("nguyenvongtuyenthang", safeNullable(dto.getNguyenVongThang()))
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("them nguyen vong", ex);
        }
    }

    public boolean update(NguyenVongDTO dto) throws SQLException {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            return false;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeMutationQuery(
                    "update xt_nguyen_vong set cccd=:cccd, thutunv=:thutunv, matruong=:matruong, tentruong=:tentruong, maxettuyen=:maxettuyen, tenmaxettuyen=:tenmaxettuyen, nguyenvongtuyenthang=:nguyenvongtuyenthang where id=:id")
                    .setParameter("id", dto.getId())
                    .setParameter("cccd", safeNullable(dto.getCccd()))
                    .setParameter("thutunv", dto.getThuTuNV())
                    .setParameter("matruong", safeNullable(dto.getMaTruong()))
                    .setParameter("tentruong", safeNullable(dto.getTenTruong()))
                    .setParameter("maxettuyen", safeNullable(dto.getMaXetTuyen()))
                    .setParameter("tenmaxettuyen", safeNullable(dto.getTenMaXetTuyen()))
                    .setParameter("nguyenvongtuyenthang", safeNullable(dto.getNguyenVongThang()))
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("cap nhat nguyen vong", ex);
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return false;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeMutationQuery("delete from xt_nguyen_vong where id=:id")
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("xoa nguyen vong", ex);
        }
    }

    public List<NguyenVongDTO> importBulk(List<NguyenVongDTO> dtos) throws SQLException {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }
        List<NguyenVongDTO> failed = new ArrayList<>();
        for (NguyenVongDTO dto : dtos) {
            try {
                create(dto);
            } catch (SQLException ex) {
                failed.add(dto);
            }
        }
        return failed;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    private static String toStr(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static void rollbackQuietly(Transaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception ignored) {
        }
    }

    private static SQLException asSqlException(String context, Exception ex) {
        return new SQLException(context + ": " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()), ex);
    }

    private void ensureTable() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("""
                create table if not exists xt_nguyen_vong (
                    id bigint not null auto_increment,
                    cccd varchar(50),
                    thutunv int,
                    matruong varchar(50),
                    tentruong varchar(255),
                    maxettuyen varchar(50),
                    tenmaxettuyen varchar(255),
                    nguyenvongtuyenthang varchar(255),
                    primary key (id)
                )
                """).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception ex) {
            // If schema creation fails, let the normal CRUD path surface the real error.
        }
    }
}
