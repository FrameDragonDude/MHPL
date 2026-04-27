package webthisinh.hibernate;

import dal.entities.CandidateEntity;
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

		String sql = """
			select nv.nv_manganh,
			       ng.tennganh,
			       nv.diem_xettuyen,
			       nv.tt_thm,
			       nv.tt_phuongthuc,
			       nv.nv_ketqua,
			       nv.nv_tt
			from xt_nguyenvongxettuyen nv
			left join xt_nganh ng on binary ng.manganh = binary nv.nv_manganh
			where binary nv.nn_cccd = binary :cccd
			order by nv.nv_tt asc, nv.diem_xettuyen desc
			""";

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			NativeQuery<Object[]> query = session.createNativeQuery(sql, Object[].class);
			query.setParameter("cccd", cccd.trim());
			List<Object[]> rows = query.getResultList();
			List<AdmissionRow> results = new ArrayList<>();
			for (Object[] row : rows) {
				results.add(mapAdmissionRow(row));
			}
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Khong the truy van nguyen vong xet tuyen", ex);
		}
	}

	private AdmissionRow mapAdmissionRow(Object[] row) {
		String majorCode = asString(row, 0);
		String majorName = asString(row, 1);
		String score = formatNumber(row, 2);
		String combination = asString(row, 3);
		String method = asString(row, 4);
		String resultLabel = asString(row, 5);
		Integer priority = asInteger(row, 6);
		return new AdmissionRow(majorCode, majorName, score, combination, method, resultLabel, priority);
	}

	private boolean isPositiveResult(String resultLabel) {
		String normalized = normalize(resultLabel);
		return normalized.contains("trungtuyen")
				|| normalized.contains("dau")
				|| normalized.contains("dat")
				|| normalized.contains("trungtuyenv")
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

	public static class AdmissionRow {
		private final String majorCode;
		private final String majorName;
		private final String score;
		private final String combination;
		private final String method;
		private final String resultLabel;
		private final Integer priority;

		public AdmissionRow(String majorCode, String majorName, String score, String combination, String method, String resultLabel, Integer priority) {
			this.majorCode = majorCode;
			this.majorName = majorName;
			this.score = score;
			this.combination = combination;
			this.method = method;
			this.resultLabel = resultLabel;
			this.priority = priority;
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
	}
}
