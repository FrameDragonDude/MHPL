package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.MajorCombinationDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MajorCombinationDAO {
	public int countRows(String majorKeyword, String toHopKeyword) throws SQLException {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return findFilteredRows(session, majorKeyword, toHopKeyword).size();
		} catch (Exception ex) {
			throw asSqlException("dem nganh-to-hop", ex);
		}
	}

	public List<MajorCombinationDTO> findRows(String majorKeyword, String toHopKeyword, int page, int pageSize) throws SQLException {
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<MajorCombinationDTO> filtered = findFilteredRows(session, majorKeyword, toHopKeyword);
			if (filtered.isEmpty()) {
				return List.of();
			}
			int safeOffset = Math.max(offset, 0);
			if (safeOffset >= filtered.size()) {
				return List.of();
			}
			int toIndex = Math.min(filtered.size(), safeOffset + Math.max(pageSize, 1));
			return new ArrayList<>(filtered.subList(safeOffset, toIndex));
		} catch (Exception ex) {
			throw asSqlException("tai danh sach nganh-to-hop", ex);
		}
	}

	private List<MajorCombinationDTO> findFilteredRows(Session session, String majorKeyword, String toHopKeyword) {
		String major = safe(majorKeyword).toLowerCase(Locale.ROOT);
		String toHop = safe(toHopKeyword).toLowerCase(Locale.ROOT);

		String ntSql = """
			select nt.id,
			       coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)),
			       nt.matohop,
			       nt.dolech
			from xt_nganh_tohop nt
			order by nt.id asc
			""";

		@SuppressWarnings("unchecked")
		List<Object[]> baseRows = session.createNativeQuery(ntSql).list();

		Map<String, String[]> majorMap = loadMajorMap(session);
		Map<String, String[]> subjectMap = loadSubjectMap(session);

		List<MajorCombinationDTO> results = new ArrayList<>();
		for (Object[] row : baseRows) {
			String majorCode = toStr(row[1]);
			String normalizedToHop = normalizeToHopCode(toStr(row[2]));
			String[] majorInfo = majorMap.getOrDefault(majorCode, new String[]{"", ""});
			String majorName = majorInfo[0];
			String goc = majorInfo[1];
			String[] subjects = subjectMap.getOrDefault(normalizedToHop, new String[]{"", "", ""});

			if (!major.isEmpty() && !majorName.toLowerCase(Locale.ROOT).contains(major)) {
				continue;
			}
			if (!toHop.isEmpty() && !normalizedToHop.toLowerCase(Locale.ROOT).contains(toHop)) {
				continue;
			}

			MajorCombinationDTO dto = new MajorCombinationDTO();
			dto.setId(toInt(row[0]));
			dto.setManganh(majorCode);
			dto.setTenNganhChuan(majorName);
			dto.setMaToHop(normalizedToHop);
			dto.setMon1(subjects[0]);
			dto.setMon2(subjects[1]);
			dto.setMon3(subjects[2]);
			dto.setGoc(goc);
			dto.setDoLech(toDouble(row[3]));
			dto.setTenToHop(buildTenToHop(dto));
			results.add(dto);
		}
		return results;
	}

	private Map<String, String[]> loadMajorMap(Session session) {
		String sql = "select manganh, coalesce(tennganh, ''), coalesce(n_tohopgoc, '') from xt_nganh";
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery(sql).list();
		Map<String, String[]> result = new HashMap<>();
		for (Object[] row : rows) {
			result.put(toStr(row[0]), new String[]{toStr(row[1]), toStr(row[2])});
		}
		return result;
	}

	private Map<String, String[]> loadSubjectMap(Session session) {
		String sql = "select matohop, coalesce(mon1, ''), coalesce(mon2, ''), coalesce(mon3, '') from xt_tohop_monthi";
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery(sql).list();
		Map<String, String[]> result = new HashMap<>();
		for (Object[] row : rows) {
			result.put(normalizeToHopCode(toStr(row[0])), new String[]{toStr(row[1]), toStr(row[2]), toStr(row[3])});
		}
		return result;
	}

	private String normalizeToHopCode(String rawCode) {
		String value = safe(rawCode);
		int bracketIndex = value.indexOf('(');
		if (bracketIndex > 0) {
			return value.substring(0, bracketIndex).trim();
		}
		return value;
	}

	public boolean create(MajorCombinationDTO dto) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			String manganh = resolveManganh(session, dto);
			if (manganh == null) {
				tx.rollback();
				return false;
			}

			String maToHop = safeNullable(dto.getMaToHop());
			if (maToHop == null) {
				tx.rollback();
				return false;
			}

			String tbKeys = buildTbKeys(dto, manganh, maToHop);
			Double doLech = dto.getDoLech() == null ? 0.0 : dto.getDoLech();

			session.createNativeMutationQuery(
					"insert into xt_nganh_tohop (manganh, matohop, tb_keys, dolech) values (:manganh, :matohop, :tbkeys, :dolech)")
					.setParameter("manganh", manganh)
					.setParameter("matohop", maToHop)
					.setParameter("tbkeys", tbKeys)
					.setParameter("dolech", doLech)
					.executeUpdate();

			updateNganhGoc(session, manganh, dto.getGoc());
			upsertToHopMon(session, maToHop, dto.getMon1(), dto.getMon2(), dto.getMon3(), dto.getTenToHop());

			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("them nganh-to-hop", ex);
		}
	}

	public boolean update(MajorCombinationDTO dto) throws SQLException {
		if (dto == null || dto.getId() == null || dto.getId() <= 0) {
			return false;
		}

		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			String manganh = resolveManganh(session, dto);
			if (manganh == null) {
				tx.rollback();
				return false;
			}

			String maToHop = safeNullable(dto.getMaToHop());
			if (maToHop == null) {
				tx.rollback();
				return false;
			}

			String tbKeys = buildTbKeys(dto, manganh, maToHop);
			Double doLech = dto.getDoLech() == null ? 0.0 : dto.getDoLech();

			int affected = session.createNativeMutationQuery(
					"""
					update xt_nganh_tohop
					set manganh = :manganh,
						matohop = :matohop,
						tb_keys = :tbkeys,
						dolech = :dolech
					where id = :id
					""")
					.setParameter("manganh", manganh)
					.setParameter("matohop", maToHop)
					.setParameter("tbkeys", tbKeys)
					.setParameter("dolech", doLech)
					.setParameter("id", dto.getId())
					.executeUpdate();

			if (affected <= 0) {
				tx.rollback();
				return false;
			}

			updateNganhGoc(session, manganh, dto.getGoc());
			upsertToHopMon(session, maToHop, dto.getMon1(), dto.getMon2(), dto.getMon3(), dto.getTenToHop());

			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("cap nhat nganh-to-hop", ex);
		}
	}

	public boolean deleteById(int id) throws SQLException {
		if (id <= 0) {
			return false;
		}

		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			int affected = session.createNativeMutationQuery("delete from xt_nganh_tohop where id = :id")
					.setParameter("id", id)
					.executeUpdate();
			tx.commit();
			return affected > 0;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("xoa nganh-to-hop", ex);
		}
	}

	public boolean upsertByTbKeys(MajorCombinationDTO dto) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			String manganh = resolveManganh(session, dto);
			if (manganh == null) {
				tx.rollback();
				return false;
			}

			String maToHop = safeNullable(dto.getMaToHop());
			if (maToHop == null) {
				tx.rollback();
				return false;
			}

			String tbKeys = buildTbKeys(dto, manganh, maToHop);
			Double doLech = dto.getDoLech() == null ? 0.0 : dto.getDoLech();

			Number foundId = (Number) session.createNativeQuery(
					"select id from xt_nganh_tohop where tb_keys = :tbkeys limit 1")
					.setParameter("tbkeys", tbKeys)
					.uniqueResult();

			if (foundId == null) {
				session.createNativeMutationQuery(
						"insert into xt_nganh_tohop (manganh, matohop, tb_keys, dolech) values (:manganh, :matohop, :tbkeys, :dolech)")
						.setParameter("manganh", manganh)
						.setParameter("matohop", maToHop)
						.setParameter("tbkeys", tbKeys)
						.setParameter("dolech", doLech)
						.executeUpdate();
			} else {
				session.createNativeMutationQuery(
						"""
						update xt_nganh_tohop
						set manganh = :manganh,
							matohop = :matohop,
							dolech = :dolech
						where id = :id
						""")
						.setParameter("manganh", manganh)
						.setParameter("matohop", maToHop)
						.setParameter("dolech", doLech)
						.setParameter("id", foundId.intValue())
						.executeUpdate();
			}

			updateNganhGoc(session, manganh, dto.getGoc());
			updateTenToHopIfExists(session, maToHop, dto.getTenToHop());

			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("upsert nganh-to-hop", ex);
		}
	}

	private String resolveManganh(Session session, MajorCombinationDTO dto) {
		String direct = safeNullable(dto.getManganh());
		if (direct != null) {
			return direct;
		}

		String tenNganh = safeNullable(dto.getTenNganhChuan());
		if (tenNganh == null) {
			return null;
		}

		Object result = session.createNativeQuery(
				"select manganh from xt_nganh where tennganh = :tennganh limit 1")
				.setParameter("tennganh", tenNganh)
				.uniqueResult();
		return result == null ? null : result.toString();
	}

	private String buildTbKeys(MajorCombinationDTO dto, String manganh, String maToHop) {
		String provided = safeNullable(dto.getTbKeys());
		if (provided != null) {
			return provided;
		}
		return manganh + "_" + maToHop;
	}


	private void updateNganhGoc(Session session, String manganh, String goc) {
		String gocValue = safeNullable(goc);
		if (gocValue == null) {
			return;
		}
		session.createNativeMutationQuery("update xt_nganh set n_tohopgoc = :goc where manganh = :manganh")
				.setParameter("goc", gocValue)
				.setParameter("manganh", manganh)
				.executeUpdate();
	}

	private void updateTenToHopIfExists(Session session, String maToHop, String tenToHop) {
    String code = safeNullable(maToHop);
    String tenValue = safeNullable(tenToHop);

    // Nếu không có mã tổ hợp hoặc tên tổ hợp để cập nhật thì bỏ qua
    if (code == null || tenValue == null) {
        return;
    }

    try {
        session.createNativeMutationQuery(
                "update xt_tohop_monthi set tentohop = :tentohop where matohop = :matohop")
                .setParameter("tentohop", tenValue)
                .setParameter("matohop", code)
                .executeUpdate();
    } catch (Exception e) {
        // Log lỗi hoặc xử lý tùy theo nhu cầu, ở đây ta để chạy tiếp vì đây là thao tác phụ
        System.err.println("Lỗi cập nhật tên tổ hợp: " + e.getMessage());
    }
}

	private void upsertToHopMon(Session session, String maToHop, String mon1, String mon2, String mon3, String tenToHop) {
		String code = safeNullable(maToHop);
		if (code == null) {
			return;
		}
		String tenValue = safeNullable(tenToHop);
		if (isBlank(tenValue)) {
			List<String> names = new ArrayList<>();
			if (!isBlank(mon1)) names.add(mon1.trim());
			if (!isBlank(mon2)) names.add(mon2.trim());
			if (!isBlank(mon3)) names.add(mon3.trim());
			tenValue = names.isEmpty() ? null : String.join(", ", names);
		}
		session.createNativeMutationQuery(
				"""
				insert into xt_tohop_monthi (matohop, mon1, mon2, mon3, tentohop)
				values (:matohop, :mon1, :mon2, :mon3, :tentohop)
				on duplicate key update mon1 = values(mon1), mon2 = values(mon2), mon3 = values(mon3), tentohop = values(tentohop)
				""")
				.setParameter("matohop", code)
				.setParameter("mon1", safeNullable(mon1))
				.setParameter("mon2", safeNullable(mon2))
				.setParameter("mon3", safeNullable(mon3))
				.setParameter("tentohop", tenValue)
				.executeUpdate();
	}

	private String buildTenToHop(MajorCombinationDTO dto) {
		List<String> names = new ArrayList<>();
		if (!isBlank(dto.getMon1())) names.add(dto.getMon1().trim());
		if (!isBlank(dto.getMon2())) names.add(dto.getMon2().trim());
		if (!isBlank(dto.getMon3())) names.add(dto.getMon3().trim());
		return names.isEmpty() ? "" : String.join(", ", names);
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

	private Double toDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException ex) {
			return null;
		}
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

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
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
				// Keep original exception context.
			}
		}
	}
}