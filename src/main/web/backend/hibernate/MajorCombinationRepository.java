package backend.hibernate;

import dal.hibernate.HibernateUtil;
import dto.MajorCombinationDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class MajorCombinationRepository {

	public List<MajorCombinationDTO> findByMajorCode(String majorCode) {
		String safeMajorCode = majorCode == null ? "" : majorCode.trim();
		if (safeMajorCode.isEmpty()) {
			return List.of();
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String sql = """
				select nt.id,
				       coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)),
				       nt.matohop,
				       nt.dolech
				from xt_nganh_tohop nt
				where coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)) = :majorCode
				order by nt.id asc
				""";

			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("majorCode", safeMajorCode)
					.list();

			if (rows.isEmpty()) {
				return List.of();
			}

			Map<String, String[]> majorMap = loadMajorMap(session);
			Map<String, String[]> subjectMap = loadSubjectMap(session);

			List<MajorCombinationDTO> results = new ArrayList<>();
			for (Object[] row : rows) {
				String majorCodeValue = toStr(row[1]);
				String normalizedToHop = normalizeToHopCode(toStr(row[2]));
				String[] majorInfo = majorMap.getOrDefault(majorCodeValue, new String[]{"", ""});
				String[] subjects = subjectMap.getOrDefault(normalizedToHop, new String[]{"", "", ""});

				MajorCombinationDTO dto = new MajorCombinationDTO();
				dto.setId(toInt(row[0]));
				dto.setManganh(majorCodeValue);
				dto.setTenNganhChuan(majorInfo[0]);
				dto.setMaToHop(normalizedToHop);
				dto.setMon1(subjects[0]);
				dto.setMon2(subjects[1]);
				dto.setMon3(subjects[2]);
				dto.setGoc(majorInfo[1]);
				dto.setDoLech(toDouble(row[3]));
				dto.setTenToHop(buildTenToHop(dto));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Khong the tai danh sach to hop theo nganh", ex);
		}
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
		String value = rawCode == null ? "" : rawCode.trim();
		int bracketIndex = value.indexOf('(');
		if (bracketIndex > 0) {
			return value.substring(0, bracketIndex).trim();
		}
		return value;
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