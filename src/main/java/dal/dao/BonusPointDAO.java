package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.BonusPointDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class BonusPointDAO {
	public int countRows(String cccdKeyword, String methodKeyword) throws SQLException {
		String cccd = safe(cccdKeyword);
		String method = safe(methodKeyword);

		String sql = """
			select count(*)
			from xt_diemcongxetuyen d
			where (:cccd = '' or d.ts_cccd like :cccdLike)
			  and (:method = '' or d.phuongthuc like :methodLike)
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Number result = (Number) session.createNativeQuery(sql)
					.setParameter("cccd", cccd)
					.setParameter("cccdLike", "%" + cccd + "%")
					.setParameter("method", method)
					.setParameter("methodLike", "%" + method + "%")
					.getSingleResult();
			return result == null ? 0 : result.intValue();
		} catch (Exception ex) {
			throw asSqlException("dem diem cong", ex);
		}
	}

	public List<BonusPointDTO> findRows(String cccdKeyword, String methodKeyword, int page, int pageSize) throws SQLException {
		String cccd = safe(cccdKeyword);
		String method = safe(methodKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		String sql = """
			select d.iddiemcong, d.ts_cccd, d.manganh, d.matohop, d.phuongthuc, d.diemCC, d.diemUtxt, d.diemTong, d.ghichu, d.dc_keys
			from xt_diemcongxetuyen d
			where (:cccd = '' or d.ts_cccd like :cccdLike)
			  and (:method = '' or d.phuongthuc like :methodLike)
			order by d.iddiemcong asc
			limit :limitValue offset :offsetValue
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("cccd", cccd)
					.setParameter("cccdLike", "%" + cccd + "%")
					.setParameter("method", method)
					.setParameter("methodLike", "%" + method + "%")
					.setParameter("limitValue", Math.max(pageSize, 1))
					.setParameter("offsetValue", Math.max(offset, 0))
					.list();

			List<BonusPointDTO> results = new ArrayList<>();
			for (Object[] row : rows) {
				BonusPointDTO dto = new BonusPointDTO();
				dto.setId(toInt(row[0]));
				dto.setTsCccd(toStr(row[1]));
				dto.setMaNganh(toStr(row[2]));
				dto.setMaToHop(toStr(row[3]));
				dto.setPhuongThuc(toStr(row[4]));
				dto.setDiemCC(toDouble(row[5]));
				dto.setDiemUtxt(toDouble(row[6]));
				dto.setDiemTong(toDouble(row[7]));
				dto.setGhiChu(toStr(row[8]));
				dto.setDcKeys(toStr(row[9]));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw asSqlException("tai danh sach diem cong", ex);
		}
	}

	public boolean create(BonusPointDTO dto) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.createNativeMutationQuery("""
				insert into xt_diemcongxetuyen (ts_cccd, manganh, matohop, phuongthuc, diemCC, diemUtxt, diemTong, ghichu, dc_keys)
				values (:tsCccd, :maNganh, :maToHop, :phuongThuc, :diemCC, :diemUtxt, :diemTong, :ghiChu, :dcKeys)
				""")
					.setParameter("tsCccd", safeNullable(dto.getTsCccd()))
					.setParameter("maNganh", safeNullable(dto.getMaNganh()))
					.setParameter("maToHop", safeNullable(dto.getMaToHop()))
					.setParameter("phuongThuc", safeNullable(dto.getPhuongThuc()))
					.setParameter("diemCC", dto.getDiemCC())
					.setParameter("diemUtxt", dto.getDiemUtxt())
					.setParameter("diemTong", dto.getDiemTong())
					.setParameter("ghiChu", safeNullable(dto.getGhiChu()))
					.setParameter("dcKeys", safeNullable(resolveKeys(dto)))
					.executeUpdate();
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("them diem cong", ex);
		}
	}

	public boolean update(BonusPointDTO dto) throws SQLException {
		if (dto == null || dto.getId() == null || dto.getId() <= 0) {
			return false;
		}
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.createNativeMutationQuery("""
				update xt_diemcongxetuyen
				set ts_cccd=:tsCccd,
					manganh=:maNganh,
					matohop=:maToHop,
					phuongthuc=:phuongThuc,
					diemCC=:diemCC,
					diemUtxt=:diemUtxt,
					diemTong=:diemTong,
					ghichu=:ghiChu,
					dc_keys=:dcKeys
				where iddiemcong=:id
				""")
					.setParameter("id", dto.getId())
					.setParameter("tsCccd", safeNullable(dto.getTsCccd()))
					.setParameter("maNganh", safeNullable(dto.getMaNganh()))
					.setParameter("maToHop", safeNullable(dto.getMaToHop()))
					.setParameter("phuongThuc", safeNullable(dto.getPhuongThuc()))
					.setParameter("diemCC", dto.getDiemCC())
					.setParameter("diemUtxt", dto.getDiemUtxt())
					.setParameter("diemTong", dto.getDiemTong())
					.setParameter("ghiChu", safeNullable(dto.getGhiChu()))
					.setParameter("dcKeys", safeNullable(resolveKeys(dto)))
					.executeUpdate();
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("cap nhat diem cong", ex);
		}
	}

	public boolean delete(Integer id) throws SQLException {
		if (id == null || id <= 0) {
			return false;
		}
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.createNativeMutationQuery("delete from xt_diemcongxetuyen where iddiemcong=:id")
					.setParameter("id", id)
					.executeUpdate();
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("xoa diem cong", ex);
		}
	}

	public boolean upsertByKey(BonusPointDTO dto) throws SQLException {
		if (dto == null) {
			return false;
		}
		String key = safeNullable(resolveKeys(dto));
		if (key == null) {
			return false;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object> ids = session.createNativeQuery("select iddiemcong from xt_diemcongxetuyen where dc_keys = :dcKeys limit 1")
					.setParameter("dcKeys", key)
					.list();
			if (ids != null && !ids.isEmpty()) {
				dto.setId(toInt(ids.get(0)));
				return update(dto);
			}
			return create(dto);
		} catch (SQLException ex) {
			throw ex;
		} catch (Exception ex) {
			throw asSqlException("upsert diem cong", ex);
		}
	}

	private static String resolveKeys(BonusPointDTO dto) {
		if (dto == null) {
			return null;
		}
		String rawKey = dto.getDcKeys();
		if (rawKey == null || rawKey.trim().isEmpty()) {
			rawKey = safe(dto.getTsCccd()) + "|" + safe(dto.getPhuongThuc()) + "|" + safe(dto.getMaNganh()) + "|" + safe(dto.getMaToHop());
		}
		return buildDbKey(rawKey);
	}

	private static String buildDbKey(String rawKey) {
		String normalized = safe(rawKey);
		if (normalized.length() <= 45) {
			return normalized;
		}
		return "bp_" + sha256Hex(normalized).substring(0, 32);
	}

	private static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException ex) {
			return Integer.toHexString(value.hashCode());
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
		if (value instanceof Number number) return number.intValue();
		try { return Integer.parseInt(value.toString().trim()); } catch (NumberFormatException ex) { return null; }
	}

	private static Double toDouble(Object value) {
		if (value == null) return null;
		if (value instanceof Number number) return number.doubleValue();
		try { return Double.parseDouble(value.toString().trim().replace(',', '.')); } catch (NumberFormatException ex) { return null; }
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
}