package backend.hibernate;

import dal.hibernate.HibernateUtil;
import dto.MajorCombinationDTO;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class MajorCombinationRepository {

	public List<MajorCombinationDTO> findByMajorCode(String majorCode) {
		String safeMajorCode = majorCode == null ? "" : majorCode.trim();
		if (safeMajorCode.isEmpty()) {
			return List.of();
		}

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
			where coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)) = :majorCode
			order by nt.id asc
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("majorCode", safeMajorCode)
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
			throw new RuntimeException("Khong the tai danh sach to hop theo nganh", ex);
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

	private String buildTenToHop(MajorCombinationDTO dto) {
		List<String> names = new ArrayList<>();
		if (dto.getMon1() != null && !dto.getMon1().trim().isEmpty()) names.add(dto.getMon1().trim());
		if (dto.getMon2() != null && !dto.getMon2().trim().isEmpty()) names.add(dto.getMon2().trim());
		if (dto.getMon3() != null && !dto.getMon3().trim().isEmpty()) names.add(dto.getMon3().trim());
		return names.isEmpty() ? "" : String.join(", ", names);
	}
}