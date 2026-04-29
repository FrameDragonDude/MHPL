package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.SubjectCombinationDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SubjectCombinationDAO {
    public int countRows(String codeKeyword, String nameKeyword) throws SQLException {
        String code = safe(codeKeyword);
        String name = safe(nameKeyword);

        String sql = """
            select count(*)
            from xt_tohop_monthi t
            where (:code = '' or t.matohop like :codeLike)
              and (:name = '' or t.tentohop like :nameLike)
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
            throw asSqlException("dem to hop mon", ex);
        }
    }

    public List<SubjectCombinationDTO> findRows(String codeKeyword, String nameKeyword, int page, int pageSize) throws SQLException {
        String code = safe(codeKeyword);
        String name = safe(nameKeyword);
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

        String sql = """
            select t.idtohop, t.matohop, t.mon1, t.mon2, t.mon3, t.tentohop
            from xt_tohop_monthi t
            where (:code = '' or t.matohop like :codeLike)
              and (:name = '' or t.tentohop like :nameLike)
            order by t.idtohop asc
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

            List<SubjectCombinationDTO> results = new ArrayList<>();
            for (Object[] row : rows) {
                SubjectCombinationDTO dto = new SubjectCombinationDTO();
                dto.setId(toInt(row[0]));
                dto.setMaToHop(toStr(row[1]));
                dto.setMon1(toStr(row[2]));
                dto.setMon2(toStr(row[3]));
                dto.setMon3(toStr(row[4]));
                dto.setTenToHop(toStr(row[5]));
                results.add(dto);
            }
            return results;
        } catch (Exception ex) {
            throw asSqlException("tai danh sach to hop mon", ex);
        }
    }

    public boolean create(SubjectCombinationDTO dto) throws SQLException {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeMutationQuery(
                    "insert into xt_tohop_monthi (matohop, mon1, mon2, mon3, tentohop) values (:matohop, :mon1, :mon2, :mon3, :tentohop)")
                    .setParameter("matohop", safeNullable(dto.getMaToHop()))
                    .setParameter("mon1", safeNullable(dto.getMon1()))
                    .setParameter("mon2", safeNullable(dto.getMon2()))
                    .setParameter("mon3", safeNullable(dto.getMon3()))
                    .setParameter("tentohop", safeNullable(dto.getTenToHop()))
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("them to hop mon", ex);
        }
    }

    public boolean update(SubjectCombinationDTO dto) throws SQLException {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            return false;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createNativeMutationQuery(
                    """
                    update xt_tohop_monthi
                    set matohop = :matohop,
                        mon1 = :mon1,
                        mon2 = :mon2,
                        mon3 = :mon3,
                        tentohop = :tentohop
                    where idtohop = :id
                    """)
                    .setParameter("matohop", safeNullable(dto.getMaToHop()))
                    .setParameter("mon1", safeNullable(dto.getMon1()))
                    .setParameter("mon2", safeNullable(dto.getMon2()))
                    .setParameter("mon3", safeNullable(dto.getMon3()))
                    .setParameter("tentohop", safeNullable(dto.getTenToHop()))
                    .setParameter("id", dto.getId())
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("cap nhat to hop mon", ex);
        }
    }

    public boolean deleteById(int id) throws SQLException {
        if (id <= 0) {
            return false;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createNativeMutationQuery("delete from xt_tohop_monthi where idtohop = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("xoa to hop mon", ex);
        }
    }

    public boolean upsertByCode(SubjectCombinationDTO dto) throws SQLException {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            String code = safeNullable(dto.getMaToHop());
            if (code == null) {
                tx.rollback();
                return false;
            }

            Number foundId = (Number) session.createNativeQuery(
                    "select idtohop from xt_tohop_monthi where matohop = :code limit 1")
                    .setParameter("code", code)
                    .uniqueResult();

            if (foundId == null) {
                session.createNativeMutationQuery(
                        "insert into xt_tohop_monthi (matohop, mon1, mon2, mon3, tentohop) values (:matohop, :mon1, :mon2, :mon3, :tentohop)")
                        .setParameter("matohop", code)
                        .setParameter("mon1", safeNullable(dto.getMon1()))
                        .setParameter("mon2", safeNullable(dto.getMon2()))
                        .setParameter("mon3", safeNullable(dto.getMon3()))
                        .setParameter("tentohop", safeNullable(dto.getTenToHop()))
                        .executeUpdate();
            } else {
                session.createNativeMutationQuery(
                        """
                        update xt_tohop_monthi
                        set mon1 = :mon1,
                            mon2 = :mon2,
                            mon3 = :mon3,
                            tentohop = :tentohop
                        where idtohop = :id
                        """)
                        .setParameter("mon1", safeNullable(dto.getMon1()))
                        .setParameter("mon2", safeNullable(dto.getMon2()))
                        .setParameter("mon3", safeNullable(dto.getMon3()))
                        .setParameter("tentohop", safeNullable(dto.getTenToHop()))
                        .setParameter("id", foundId.intValue())
                        .executeUpdate();
            }

            tx.commit();
            return true;
        } catch (Exception ex) {
            rollbackQuietly(tx);
            throw asSqlException("upsert to hop mon", ex);
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private String toStr(Object value) {
        return value == null ? "" : value.toString();
    }

    private String safe(String input) {
        return input == null ? "" : input.trim();
    }

    private String safeNullable(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SQLException asSqlException(String action, Exception ex) {
        if (ex instanceof SQLException) {
            return (SQLException) ex;
        }
        return new SQLException("Loi Hibernate khi " + action + ": " + ex.getMessage(), ex);
    }

    private void rollbackQuietly(Transaction tx) {
        if (tx != null) {
            try {
                if (tx.isActive()) {
                    tx.rollback();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
