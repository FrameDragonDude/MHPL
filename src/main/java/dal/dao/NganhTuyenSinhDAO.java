package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.NganhTuyenSinhDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class NganhTuyenSinhDAO {
    public int countRows(String codeKeyword, String nameKeyword) throws SQLException {
        String code = safe(codeKeyword);
        String name = safe(nameKeyword);
        String sql = """
            select count(*) from xt_nganh n
            where (:code = '' or n.manganh like :codeLike)
              and (:name = '' or n.tennganh like :nameLike)
            """;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Number result = (Number) session.createNativeQuery(sql)
                    .setParameter("code", code)
                    .setParameter("codeLike", "%" + code + "%")
                    .setParameter("name", name)
                    .setParameter("nameLike", "%" + name + "%")
                    .getSingleResult();
            return result == null ? 0 : result.intValue();
        } catch (Exception ex) {
            throw asSqlException("dem nganh", ex);
        }
    }

    public List<NganhTuyenSinhDTO> findRows(String codeKeyword, String nameKeyword, int page, int pageSize) throws SQLException {
        String code = safe(codeKeyword);
        String name = safe(nameKeyword);
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);
                                                                String sql = """
                                                                                                                                                                                                select n.idnganh,
                                                                                                                                                                                                                                                 n.manganh,
                                                                                                                                                                                                                                                 n.tennganh,
                                                                                                                                                                                                                                                 n.n_tuyenthang,
                                                                                                                                                                                                                                                 n.n_diemsan,
                                                                                                                                                                                                                                                 n.n_chitieu,
                                                                                                                                                                                                                                                         n.n_diemtrungtuyen,
                                                                                                                                                                                                                                                         n.n_dgnl,
                                                                                                                                                                                                                                                         n.n_thpt,
                                                                                                                                                                                                                                                         n.n_vsat,
                                                                                                                                                                                                                                                             count(distinct nullif(trim(v.cccd), '')) as so_thi_sinh_dangky
                                                                                                                                                                                                                                from xt_nganh n
                                                                                                                                                                                                                                left join xt_nguyen_vong v on lower(trim(v.maxettuyen)) = lower(trim(n.manganh))
                                                where (:code = '' or n.manganh like :codeLike)
                                                        and (:name = '' or n.tennganh like :nameLike)
                                                group by n.idnganh, n.manganh, n.tennganh, n.n_tuyenthang, n.n_diemsan, n.n_chitieu, n.n_diemtrungtuyen
                                                order by n.idnganh asc
                                                limit :limitValue offset :offsetValue
                                                """;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = session.createNativeQuery(sql)
                    .setParameter("code", code)
                    .setParameter("codeLike", "%" + code + "%")
                    .setParameter("name", name)
                    .setParameter("nameLike", "%" + name + "%")
                    .setParameter("limitValue", Math.max(pageSize, 1))
                    .setParameter("offsetValue", Math.max(offset, 0))
                    .list();

            List<NganhTuyenSinhDTO> results = new ArrayList<>();
            for (Object[] row : rows) {
                NganhTuyenSinhDTO dto = new NganhTuyenSinhDTO();
                dto.setId(toInt(row[0]));
                dto.setMaXetTuyen(toStr(row[1]));
                dto.setTenNganh(toStr(row[2]));
                dto.setChuongTrinh(toStr(row[3]));
                dto.setNguongDauVao(toStr(row[4]));
                dto.setChiTieuChot(toInt(row[5]));
                dto.setDiemTrungTuyen(toStr(row[6]));
                // build phương thức text from flags
                String dgnl = toStr(row[7]);
                String thpt = toStr(row[8]);
                String vsat = toStr(row[9]);
                StringBuilder method = new StringBuilder();
                if (!dgnl.isEmpty()) { if (method.length() > 0) method.append(", "); method.append("DGNL"); }
                if (!thpt.isEmpty()) { if (method.length() > 0) method.append(", "); method.append("THPT"); }
                if (!vsat.isEmpty()) { if (method.length() > 0) method.append(", "); method.append("VSAT"); }
                dto.setPhuongThuc(method.toString());
                dto.setSoThiSinhDangKy(toInt(row[10]));
                results.add(dto);
            }
            return results;
        } catch (Exception ex) {
            throw asSqlException("tai danh sach nganh", ex);
        }
    }

    public boolean create(NganhTuyenSinhDTO dto) throws SQLException {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
                session.createNativeMutationQuery(
                    "insert into xt_nganh (manganh, tennganh, n_tuyenthang, n_diemsan, n_chitieu, n_dgnl, n_thpt, n_vsat) values (:manganh, :tennganh, :ntuyenthang, :ndiemsan, :nchitieu, :ndgnl, :nthpt, :nvsat)")
                    .setParameter("manganh", safeNullable(dto.getMaXetTuyen()))
                    .setParameter("tennganh", safeNullable(dto.getTenNganh()))
                    .setParameter("ntuyenthang", safeNullable(dto.getnTuyenthang()))
                    .setParameter("ndiemsan", safeNullable(dto.getNguongDauVao()))
                    .setParameter("nchitieu", dto.getChiTieuChot())
                    .setParameter("ndgnl", safeNullable(dto.getnDgnl()))
                    .setParameter("nthpt", safeNullable(dto.getnThpt()))
                    .setParameter("nvsat", safeNullable(dto.getnVsat()))
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("them nganh", ex);
        }
    }

    public boolean update(NganhTuyenSinhDTO dto) throws SQLException {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            return false;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
                int affected = session.createNativeMutationQuery(
                    "update xt_nganh set manganh = :manganh, tennganh = :tennganh, n_tuyenthang = :ntuyenthang, n_diemsan = :ndiemsan, n_chitieu = :nchitieu, n_dgnl = :ndgnl, n_thpt = :nthpt, n_vsat = :nvsat where idnganh = :id")
                    .setParameter("manganh", safeNullable(dto.getMaXetTuyen()))
                    .setParameter("tennganh", safeNullable(dto.getTenNganh()))
                    .setParameter("ntuyenthang", safeNullable(dto.getnTuyenthang()))
                    .setParameter("ndiemsan", safeNullable(dto.getNguongDauVao()))
                    .setParameter("nchitieu", dto.getChiTieuChot())
                    .setParameter("ndgnl", safeNullable(dto.getnDgnl()))
                    .setParameter("nthpt", safeNullable(dto.getnThpt()))
                    .setParameter("nvsat", safeNullable(dto.getnVsat()))
                    .setParameter("id", dto.getId())
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("cap nhat nganh", ex);
        }
    }

    public boolean deleteById(int id) throws SQLException {
        if (id <= 0) return false;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createNativeMutationQuery("delete from xt_nganh where idnganh = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("xoa nganh", ex);
        }
    }

    public boolean upsertByCode(NganhTuyenSinhDTO dto) throws SQLException {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String code = safeNullable(dto.getMaXetTuyen());
            if (code == null) { tx.rollback(); return false; }
            Number foundId = (Number) session.createNativeQuery("select idnganh from xt_nganh where manganh = :code limit 1")
                    .setParameter("code", code)
                    .uniqueResult();
            if (foundId == null) {
                session.createNativeMutationQuery(
                    "insert into xt_nganh (manganh, tennganh, n_tuyenthang, n_diemsan, n_chitieu, n_dgnl, n_thpt, n_vsat) values (:manganh, :tennganh, :ntuyenthang, :ndiemsan, :nchitieu, :ndgnl, :nthpt, :nvsat)")
                    .setParameter("manganh", code)
                    .setParameter("tennganh", safeNullable(dto.getTenNganh()))
                    .setParameter("ntuyenthang", safeNullable(dto.getnTuyenthang()))
                    .setParameter("ndiemsan", safeNullable(dto.getNguongDauVao()))
                    .setParameter("nchitieu", dto.getChiTieuChot())
                    .setParameter("ndgnl", safeNullable(dto.getnDgnl()))
                    .setParameter("nthpt", safeNullable(dto.getnThpt()))
                    .setParameter("nvsat", safeNullable(dto.getnVsat()))
                    .executeUpdate();
            } else {
                session.createNativeMutationQuery(
                    "update xt_nganh set tennganh = :tennganh, n_tuyenthang = :ntuyenthang, n_diemsan = :ndiemsan, n_chitieu = :nchitieu, n_dgnl = :ndgnl, n_thpt = :nthpt, n_vsat = :nvsat where idnganh = :id")
                    .setParameter("tennganh", safeNullable(dto.getTenNganh()))
                    .setParameter("ntuyenthang", safeNullable(dto.getnTuyenthang()))
                    .setParameter("ndiemsan", safeNullable(dto.getNguongDauVao()))
                    .setParameter("nchitieu", dto.getChiTieuChot())
                    .setParameter("ndgnl", safeNullable(dto.getnDgnl()))
                    .setParameter("nthpt", safeNullable(dto.getnThpt()))
                    .setParameter("nvsat", safeNullable(dto.getnVsat()))
                    .setParameter("id", foundId.intValue())
                    .executeUpdate();
            }
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("upsert nganh", ex);
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return null; }
    }

    private String toStr(Object value) { return value == null ? "" : value.toString(); }

    private String safe(String input) { return input == null ? "" : input.trim(); }

    private String safeNullable(String input) { if (input == null) return null; String t = input.trim(); return t.isEmpty() ? null : t; }

    private SQLException asSqlException(String action, Exception ex) {
        if (ex instanceof SQLException) return (SQLException) ex;
        return new SQLException("Loi Hibernate khi " + action + ": " + ex.getMessage(), ex);
    }

    private void rollbackQuietly(Transaction tx) {
        if (tx != null) {
            try { if (tx.isActive()) tx.rollback(); } catch (Exception ignored) {}
        }
    }
}
