package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.NguyenVongXetTuyenDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class NguyenVongXetTuyenDAO {

	public int countRows(String cccdKeyword, String nganhKeyword) throws SQLException {
		String cccd = safe(cccdKeyword);
		String nganh = safe(nganhKeyword);

		String sql = """
			select count(*)
			from xt_nguyenvongxettuyen nv
			where (:cccd = '' or nv.nn_cccd like :cccdLike)
			  and (:nganh = '' or nv.nv_manganh like :nganhLike)
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Number result = (Number) session.createNativeQuery(sql)
					.setParameter("cccd", cccd)
					.setParameter("cccdLike", "%" + cccd + "%")
					.setParameter("nganh", nganh)
					.setParameter("nganhLike", "%" + nganh + "%")
					.getSingleResult();
			return result == null ? 0 : result.intValue();
		} catch (Exception ex) {
			throw asSqlException("dem nguyen vong xet tuyen", ex);
		}
	}

	public List<NguyenVongXetTuyenDTO> findRows(String cccdKeyword, String nganhKeyword, int page, int pageSize) throws SQLException {
		String cccd = safe(cccdKeyword);
		String nganh = safe(nganhKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		String sql = """
			select nv.idnv, nv.nn_cccd, nv.nv_manganh, nv.nv_tt, nv.diem_thxt, nv.diem_utqd, nv.diem_cong, nv.diem_xettuyen,
			       nv.nv_ketqua, nv.nv_keys, nv.tt_phuongthuc, nv.tt_thm
			from xt_nguyenvongxettuyen nv
			where (:cccd = '' or nv.nn_cccd like :cccdLike)
			  and (:nganh = '' or nv.nv_manganh like :nganhLike)
			order by nv.idnv asc
			limit :limitValue offset :offsetValue
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("cccd", cccd)
					.setParameter("cccdLike", "%" + cccd + "%")
					.setParameter("nganh", nganh)
					.setParameter("nganhLike", "%" + nganh + "%")
					.setParameter("limitValue", Math.max(pageSize, 1))
					.setParameter("offsetValue", Math.max(offset, 0))
					.list();

			List<NguyenVongXetTuyenDTO> results = new ArrayList<>();
			for (Object[] row : rows) {
				NguyenVongXetTuyenDTO dto = new NguyenVongXetTuyenDTO();
				dto.setIdnv(toInt(row[0]));
				dto.setNnCccd(toStr(row[1]));
				dto.setNvManganh(toStr(row[2]));
				dto.setNvTt(toInt(row[3]));
				dto.setDiemThxt(toDecimal(row[4]));
				dto.setDiemUtqd(toDecimal(row[5]));
				dto.setDiemCong(toDecimal(row[6]));
				dto.setDiemXettuyen(toDecimal(row[7]));
				dto.setNvKetqua(toStr(row[8]));
				dto.setNvKeys(toStr(row[9]));
				dto.setTtPhuongthuc(toStr(row[10]));
				dto.setTtThm(toStr(row[11]));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw asSqlException("tai danh sach nguyen vong xet tuyen", ex);
		}
	}

	public List<Object[]> findAdmittedCountsByMajorAndMethod() throws SQLException {
		String sql = """
			select nv.nv_manganh, nv.tt_phuongthuc, nv.tt_thm, count(*)
			from xt_nguyenvongxettuyen nv
			where lower(coalesce(nv.nv_ketqua, '')) = 'trúng tuyển'
			group by nv.nv_manganh, nv.tt_phuongthuc, nv.tt_thm
			order by nv.nv_manganh asc, nv.tt_phuongthuc asc, nv.tt_thm asc
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql).list();
			return rows;
		} catch (Exception ex) {
			throw asSqlException("thong ke trung tuyen theo nganh va phuong thuc", ex);
		}
	}

	public boolean create(NguyenVongXetTuyenDTO dto) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.createNativeMutationQuery(
					"insert into xt_nguyenvongxettuyen (nn_cccd, nv_manganh, nv_tt, diem_thxt, diem_utqd, diem_cong, diem_xettuyen, nv_ketqua, nv_keys, tt_phuongthuc, tt_thm) "
					+ "values (:cccd, :manganh, :nv_tt, :diem_thxt, :diem_utqd, :diem_cong, :diem_xettuyen, :nv_ketqua, :nv_keys, :tt_phuongthuc, :tt_thm)")
					.setParameter("cccd", safeNullable(dto.getNnCccd()))
					.setParameter("manganh", safeNullable(dto.getNvManganh()))
					.setParameter("nv_tt", dto.getNvTt() == null ? 0 : dto.getNvTt())
					.setParameter("diem_thxt", dto.getDiemThxt())
					.setParameter("diem_utqd", dto.getDiemUtqd())
					.setParameter("diem_cong", dto.getDiemCong())
					.setParameter("diem_xettuyen", dto.getDiemXettuyen())
					.setParameter("nv_ketqua", safeNullable(dto.getNvKetqua()))
					.setParameter("nv_keys", safeNullable(dto.getNvKeys()))
					.setParameter("tt_phuongthuc", safeNullable(dto.getTtPhuongthuc()))
					.setParameter("tt_thm", safeNullable(dto.getTtThm()))
					.executeUpdate();
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("them nguyen vong xet tuyen", ex);
		}
	}

	public boolean update(NguyenVongXetTuyenDTO dto) throws SQLException {
		if (dto == null || dto.getIdnv() == null || dto.getIdnv() <= 0) {
			return false;
		}
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			int affected = session.createNativeMutationQuery(
					"update xt_nguyenvongxettuyen set nn_cccd=:cccd, nv_manganh=:manganh, nv_tt=:nv_tt, diem_thxt=:diem_thxt, diem_utqd=:diem_utqd, "
					+ "diem_cong=:diem_cong, diem_xettuyen=:diem_xettuyen, nv_ketqua=:nv_ketqua, nv_keys=:nv_keys, tt_phuongthuc=:tt_phuongthuc, tt_thm=:tt_thm "
					+ "where idnv=:idnv")
					.setParameter("idnv", dto.getIdnv())
					.setParameter("cccd", safeNullable(dto.getNnCccd()))
					.setParameter("manganh", safeNullable(dto.getNvManganh()))
					.setParameter("nv_tt", dto.getNvTt() == null ? 0 : dto.getNvTt())
					.setParameter("diem_thxt", dto.getDiemThxt())
					.setParameter("diem_utqd", dto.getDiemUtqd())
					.setParameter("diem_cong", dto.getDiemCong())
					.setParameter("diem_xettuyen", dto.getDiemXettuyen())
					.setParameter("nv_ketqua", safeNullable(dto.getNvKetqua()))
					.setParameter("nv_keys", safeNullable(dto.getNvKeys()))
					.setParameter("tt_phuongthuc", safeNullable(dto.getTtPhuongthuc()))
					.setParameter("tt_thm", safeNullable(dto.getTtThm()))
					.executeUpdate();
			tx.commit();
			return affected > 0;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("cap nhat nguyen vong xet tuyen", ex);
		}
	}

	public boolean delete(Integer id) throws SQLException {
		if (id == null || id <= 0) {
			return false;
		}
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			int affected = session.createNativeMutationQuery("delete from xt_nguyenvongxettuyen where idnv=:id")
					.setParameter("id", id)
					.executeUpdate();
			tx.commit();
			return affected > 0;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("xoa nguyen vong xet tuyen", ex);
		}
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

	private static java.math.BigDecimal toDecimal(Object value) {
		if (value == null) return null;
		if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
		if (value instanceof Number) return new java.math.BigDecimal(((Number) value).doubleValue());
		return null;
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
}
