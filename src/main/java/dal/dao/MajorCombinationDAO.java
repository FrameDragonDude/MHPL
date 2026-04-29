package dal.dao;

import dal.hibernate.HibernateUtil;
import dto.MajorCombinationDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MajorCombinationDAO {
	public int countRows(String majorKeyword, String toHopKeyword) throws SQLException {
		String major = safe(majorKeyword);
		String toHop = safe(toHopKeyword);

		String sql = """
			select count(*)
			from xt_nganh_tohop nt
			left join xt_nganh n on n.manganh = coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1))
			where (:major = '' or n.tennganh like :majorLike)
			  and (:tohop = '' or (
					case
						when locate('(', nt.matohop) > 0 then trim(substring(nt.matohop, 1, locate('(', nt.matohop) - 1))
						else nt.matohop
					end
				) like :tohopLike)
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Number result = (Number) session.createNativeQuery(sql)
					.setParameter("major", major)
					.setParameter("majorLike", "%" + major + "%")
					.setParameter("tohop", toHop)
					.setParameter("tohopLike", "%" + toHop + "%")
					.getSingleResult();
			return result == null ? 0 : result.intValue();
		} catch (Exception ex) {
			throw asSqlException("dem nganh-to-hop", ex);
		}
	}

	public List<MajorCombinationDTO> findRows(String majorKeyword, String toHopKeyword, int page, int pageSize) throws SQLException {
		String major = safe(majorKeyword);
		String toHop = safe(toHopKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		String sql = """
			select nt.id,
				   coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)),
				   coalesce(n.tennganh, ''),
				   case
					   when locate('(', nt.matohop) > 0 then trim(substring(nt.matohop, 1, locate('(', nt.matohop) - 1))
					   else nt.matohop
				   end,
				   coalesce(tm.mon1, ''),
				   coalesce(tm.mon2, ''),
				   coalesce(tm.mon3, ''),
				   coalesce(n.n_tohopgoc, ''),
				   nt.dolech
			from xt_nganh_tohop nt
			left join xt_nganh n on n.manganh = coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1))
			left join xt_tohop_monthi tm on tm.matohop = (
				case
					when locate('(', nt.matohop) > 0 then trim(substring(nt.matohop, 1, locate('(', nt.matohop) - 1))
					else nt.matohop
				end
			)
			where (:major = '' or n.tennganh like :majorLike)
			  and (:tohop = '' or (
					case
						when locate('(', nt.matohop) > 0 then trim(substring(nt.matohop, 1, locate('(', nt.matohop) - 1))
						else nt.matohop
					end
				) like :tohopLike)
			order by nt.id asc
			limit :limitValue offset :offsetValue
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("major", major)
					.setParameter("majorLike", "%" + major + "%")
					.setParameter("tohop", toHop)
					.setParameter("tohopLike", "%" + toHop + "%")
					.setParameter("limitValue", Math.max(pageSize, 1))
					.setParameter("offsetValue", Math.max(offset, 0))
					.list();

			List<MajorCombinationDTO> results = new ArrayList<>();
			for (Object[] row : rows) {
				MajorCombinationDTO dto = new MajorCombinationDTO();
				dto.setId(toInt(row[0]));
				dto.setManganh(toStr(row[1]));
				dto.setTenNganhChuan(toStr(row[2]));
				dto.setMaToHop(toStr(row[3]));
				dto.setMon1(toStr(row[4]));
				dto.setMon2(toStr(row[5]));
				dto.setMon3(toStr(row[6]));
				dto.setGoc(toStr(row[7]));
				dto.setDoLech(toDouble(row[8]));
				dto.setTenToHop(buildTenToHop(dto));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw asSqlException("tai danh sach nganh-to-hop", ex);
		}
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