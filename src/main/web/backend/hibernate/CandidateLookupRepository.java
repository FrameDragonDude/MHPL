package backend.hibernate;

import dal.entities.CandidateEntity;
import dal.entities.NganhEntity;
import dal.entities.NguyenVongXetTuyenEntity;
import dal.hibernate.HibernateUtil;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public class CandidateLookupRepository {

	public Optional<CandidateEntity> findByCccd(String cccd) {
		if (cccd == null || cccd.trim().isEmpty()) {
			return Optional.empty();
		}

		String input = cccd.trim();
		String inputDigits = input.replaceAll("\\D", "");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			CandidateEntity entity = session.createQuery(
					"from CandidateEntity c where c.cccd = :cccd",
					CandidateEntity.class
			)
					.setParameter("cccd", input)
					.uniqueResult();

			if (entity == null && !inputDigits.isEmpty()) {
				entity = session.createQuery(
						"from CandidateEntity c where replace(replace(replace(c.cccd, '.', ''), '-', ''), ' ', '') = :cccdDigits",
						CandidateEntity.class
				)
						.setParameter("cccdDigits", inputDigits)
						.uniqueResult();
			}

			return Optional.ofNullable(entity);
		} catch (Exception ex) {
			throw new RuntimeException("Khong the tim thi sinh theo CCCD", ex);
		}
	}

	public Optional<AdmissionRow> findWinningAdmission(String cccd) {
		List<AdmissionRow> rows = findAdmissionsByCccd(cccd);
		for (AdmissionRow row : rows) {
			if (isPositiveResult(row.getResultLabel())) {
				return Optional.of(row);
			}
		}
		return Optional.empty();
	}

	public List<AdmissionRow> findAdmissionsByCccd(String cccd) {
		if (cccd == null || cccd.trim().isEmpty()) {
			return List.of();
		}

		String normalizedCccd = cccd.trim();
		java.util.Set<String> cccdVariants = buildCccdVariants(normalizedCccd);

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			// load major names separately to avoid cross-table collation issues in JOIN
			java.util.Map<String,String> majorNames = loadMajorNames(session);

			String sql = """
			select nv.nv_manganh,
			       nv.diem_xettuyen,
			       nv.tt_thm,
			       nv.tt_phuongthuc,
			       nv.nv_ketqua,
			       nv.nv_tt,
			       coalesce(nv.diem_thxt, 0) as diem_thxt,
			       coalesce(nv.diem_utqd, 0) as diem_utqd,
			       coalesce(nv.diem_cong, 0) as diem_cong,
			       coalesce(nv.diem_xettuyen, 0) as diem_xettuyen
			from xt_nguyenvongxettuyen nv
			where nv.nn_cccd = :cccd
			order by nv.nv_tt asc, nv.diem_xettuyen desc
			""";

			@SuppressWarnings("unchecked")
			List<Object[]> rows = session.createNativeQuery(sql)
					.setParameter("cccd", normalizedCccd)
					.getResultList();

			if (rows.isEmpty() && !cccdVariants.isEmpty()) {
				String sqlByDigits = """
				select nv.nv_manganh,
				       nv.diem_xettuyen,
				       nv.tt_thm,
				       nv.tt_phuongthuc,
				       nv.nv_ketqua,
				       nv.nv_tt,
				       coalesce(nv.diem_thxt, 0) as diem_thxt,
				       coalesce(nv.diem_utqd, 0) as diem_utqd,
				       coalesce(nv.diem_cong, 0) as diem_cong,
				       coalesce(nv.diem_xettuyen, 0) as diem_xettuyen
				from xt_nguyenvongxettuyen nv
				where lower(replace(replace(replace(replace(nv.nn_cccd, '_', ''), '.', ''), '-', ''), ' ', '')) in (:cccdVariants)
				order by nv.nv_tt asc, nv.diem_xettuyen desc
				""";

				@SuppressWarnings("unchecked")
				List<Object[]> byDigits = session.createNativeQuery(sqlByDigits)
						.unwrap(NativeQuery.class)
						.setParameterList("cccdVariants", cccdVariants)
						.getResultList();
				rows = byDigits;
			}

			List<AdmissionRow> results = new ArrayList<>();
			for (Object[] row : rows) {
				results.add(mapAdmissionRow(row, majorNames));
			}
			return results;
		} catch (Exception ex) {
			String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
			throw new RuntimeException("Khong the truy van nguyen vong xet tuyen: " + detail, ex);
		}
	}

	private AdmissionRow mapAdmissionRow(Object[] row, java.util.Map<String,String> majorNames) {
		String majorCode = asString(row, 0);
		String majorName = majorNames.getOrDefault(majorCode, "");
		String score = formatNumber(row, 1);
		String combination = asString(row, 2);
		String method = asString(row, 3);
		String resultLabel = asString(row, 4);
		Integer priority = asInteger(row, 5);
		String diemThxt = formatNumber(row, 6);
		String diemUtqd = formatNumber(row, 7);
		String diemCong = formatNumber(row, 8);
		String diemSan = formatNumber(row, 9);
		return new AdmissionRow(majorCode, majorName, score, combination, method, resultLabel, priority,
					diemThxt, diemUtqd, diemCong, diemSan);
	}

	private java.util.Map<String,String> loadMajorNames(Session session) {
		@SuppressWarnings("unchecked")
		java.util.List<Object[]> rows = session.createNativeQuery("select manganh, coalesce(tennganh,'') from xt_nganh").getResultList();
		java.util.Map<String,String> map = new java.util.HashMap<>();
		for (Object[] r : rows) {
			String key = r == null || r[0] == null ? "" : r[0].toString().trim();
			String val = r == null || r[1] == null ? "" : r[1].toString().trim();
			if (!key.isEmpty()) map.put(key, val);
		}
		return map;
	}

	private boolean isPositiveResult(String resultLabel) {
		String normalized = normalize(resultLabel);
		if (normalized.contains("khongtrungtuyen") || normalized.contains("chuatrungtuyen")) {
			return false;
		}
		return normalized.equals("trungtuyen")
				|| normalized.startsWith("trungtuyen")
				|| normalized.contains("duoccongnhantrungtuyen")
				|| normalized.contains("pass")
				|| normalized.contains("accepted");
	}

	private String asString(Object[] row, int index) {
		if (row == null || index < 0 || index >= row.length || row[index] == null) {
			return "";
		}
		return row[index].toString().trim();
	}

	private Integer asInteger(Object[] row, int index) {
		if (row == null || index < 0 || index >= row.length || row[index] == null) {
			return null;
		}
		Object value = row[index];
		if (value instanceof Number number) {
			return number.intValue();
		}
		try {
			return Integer.parseInt(value.toString().trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private String formatNumber(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal.stripTrailingZeros().toPlainString();
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString();
		}
		return value.toString();
	}

	private String formatNumber(Object[] row, int index) {
		if (row == null || index < 0 || index >= row.length) {
			return "";
		}
		return formatNumber(row[index]);
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}
		String lower = value.toLowerCase(Locale.ROOT).trim().replace('đ', 'd');
		String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return noMark.replaceAll("[^a-z0-9]", "");
	}

	private java.util.Set<String> buildCccdVariants(String cccdInput) {
		java.util.LinkedHashSet<String> variants = new java.util.LinkedHashSet<>();
		if (cccdInput == null) {
			return variants;
		}

		String canonical = normalize(cccdInput);
		if (canonical.isEmpty()) {
			return variants;
		}
		variants.add(canonical);

		String withoutTs = canonical.startsWith("ts") ? canonical.substring(2) : canonical;
		if (!withoutTs.isEmpty()) {
			variants.add(withoutTs);
			variants.add("ts" + withoutTs);
		}

		String digits = canonical.replaceAll("\\D", "");
		if (!digits.isEmpty()) {
			variants.add(digits);
			variants.add("ts" + digits);
			try {
				int n = Integer.parseInt(digits);
				String pad4 = String.format("%04d", n);
				variants.add(pad4);
				variants.add("ts" + pad4);
			} catch (NumberFormatException ignored) {
			}
		}

		return variants;
	}

	public static class AdmissionRow {
		private final String majorCode;
		private final String majorName;
		private final String score;
		private final String combination;
		private final String method;
		private final String resultLabel;
		private final Integer priority;
		private final String diemThxt;
		private final String diemUtqd;
		private final String diemCong;
		private final String diemSan;

		public AdmissionRow(String majorCode, String majorName, String score, String combination, String method, 
				String resultLabel, Integer priority, String diemThxt, String diemUtqd, String diemCong, String diemSan) {
			this.majorCode = majorCode;
			this.majorName = majorName;
			this.score = score;
			this.combination = combination;
			this.method = method;
			this.resultLabel = resultLabel;
			this.priority = priority;
			this.diemThxt = diemThxt;
			this.diemUtqd = diemUtqd;
			this.diemCong = diemCong;
			this.diemSan = diemSan;
		}

		public String getMajorCode() {
			return majorCode;
		}

		public String getMajorName() {
			return majorName;
		}

		public String getScore() {
			return score;
		}

		public String getCombination() {
			return combination;
		}

		public String getMethod() {
			return method;
		}

		public String getResultLabel() {
			return resultLabel;
		}

		public Integer getPriority() {
			return priority;
		}

		public String getDiemThxt() {
			return diemThxt;
		}

		public String getDiemUtqd() {
			return diemUtqd;
		}

		public String getDiemCong() {
			return diemCong;
		}

		public String getDiemSan() {
			return diemSan;
		}
	}
}
