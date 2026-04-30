package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.ConversionRuleDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;

public class ConversionRuleDAO {
	public int countRows(String phuongThucKeyword, String toHopKeyword) throws SQLException {
		String phuongThuc = safe(phuongThucKeyword);
		String toHop = safe(toHopKeyword);

		String sql = """
			select count(*)
			from xt_bangquydoi q
			where (:phuongThuc = '' or q.d_phuongthuc like :phuongThucLike)
			  and (:toHop = '' or q.d_tohop like :toHopLike)
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Number result = (Number) session.createNativeQuery(sql)
					.setParameter("phuongThuc", phuongThuc)
					.setParameter("phuongThucLike", "%" + phuongThuc + "%")
					.setParameter("toHop", toHop)
					.setParameter("toHopLike", "%" + toHop + "%")
					.getSingleResult();
			return result == null ? 0 : result.intValue();
		} catch (Exception ex) {
			throw asSqlException("dem bang quy doi", ex);
		}
	}

	public List<ConversionRuleDTO> findRows(String phuongThucKeyword, String toHopKeyword, int page, int pageSize) throws SQLException {
		String phuongThuc = safe(phuongThucKeyword);
		String toHop = safe(toHopKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		String sql = """
			select q.idqd, q.d_phuongthuc, q.d_tohop, q.d_mon, q.d_diema, q.d_diemb, q.d_diemc, q.d_diemd, q.d_maquydoi, q.d_phanvi
			from xt_bangquydoi q
			where (:phuongThuc = '' or q.d_phuongthuc like :phuongThucLike)
			  and (:toHop = '' or q.d_tohop like :toHopLike)
			order by q.idqd asc
			limit :limitValue offset :offsetValue
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("phuongThuc", phuongThuc)
					.setParameter("phuongThucLike", "%" + phuongThuc + "%")
					.setParameter("toHop", toHop)
					.setParameter("toHopLike", "%" + toHop + "%")
					.setParameter("limitValue", Math.max(pageSize, 1))
					.setParameter("offsetValue", Math.max(offset, 0))
					.list();

			List<ConversionRuleDTO> results = new ArrayList<>();
			for (Object[] row : rows) {
				ConversionRuleDTO dto = new ConversionRuleDTO();
				dto.setId(toInt(row[0]));
				dto.setPhuongThuc(toStr(row[1]));
				dto.setToHop(toStr(row[2]));
				dto.setMon(toStr(row[3]));
				dto.setDiemA(toDouble(row[4]));
				dto.setDiemB(toDouble(row[5]));
				dto.setDiemC(toDouble(row[6]));
				dto.setDiemD(toDouble(row[7]));
				dto.setMaQuyDoi(toStr(row[8]));
				dto.setPhanVi(toStr(row[9]));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw asSqlException("tai danh sach bang quy doi", ex);
		}
	}

	private static String safe(String value) {
		return value == null ? "" : value.trim();
	}

	private static Integer toInt(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.intValue();
		}
		try {
			return Integer.parseInt(value.toString().trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static Double toDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		try {
			return Double.parseDouble(value.toString().trim().replace(',', '.'));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static String toStr(Object value) {
		return value == null ? "" : value.toString().trim();
	}

	private static SQLException asSqlException(String context, Exception ex) {
		return new SQLException(context + ": " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()), ex);
	}
}