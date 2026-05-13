package bus;

import dal.dao.NguyenVongXetTuyenDAO;
import dal.entities.AspirationEntity;
import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.NganhEntity;
import dal.hibernate.HibernateUtil;
import dto.NguyenVongXetTuyenDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class NguyenVongXetTuyenService {
	private final NguyenVongXetTuyenDAO dao = new NguyenVongXetTuyenDAO();
	private static final int PAGE_SIZE = 20;

	public int countPages(String cccdKeyword, String nganhKeyword) throws SQLException {
		int totalRows = dao.countRows(cccdKeyword, nganhKeyword);
		return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
	}

	public int countRows(String cccdKeyword, String nganhKeyword) throws SQLException {
		return dao.countRows(cccdKeyword, nganhKeyword);
	}

	public List<NguyenVongXetTuyenDTO> getRows(String cccdKeyword, String nganhKeyword, int page) throws SQLException {
		return dao.findRows(cccdKeyword, nganhKeyword, page, PAGE_SIZE);
	}

	public boolean create(NguyenVongXetTuyenDTO dto) throws SQLException {
		if (!isValid(dto)) {
			throw new SQLException("Dữ liệu nguyện vọng xét tuyển không hợp lệ");
		}
		return dao.create(dto);
	}

	public boolean update(NguyenVongXetTuyenDTO dto) throws SQLException {
		if (!isValid(dto)) {
			throw new SQLException("Dữ liệu nguyện vọng xét tuyển không hợp lệ");
		}
		return dao.update(dto);
	}

	public boolean deleteById(int id) throws SQLException {
		if (id <= 0) {
			return false;
		}
		return dao.delete(id);
	}

	public GenerationResult generateFromDatabase(boolean replaceExisting) throws SQLException {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			try {
				int deletedExistingRows = 0;
				if (replaceExisting) {
					deletedExistingRows = session.createNativeMutationQuery("delete from xt_nguyenvongxettuyen")
							.executeUpdate();
				}

				Set<String> candidateCcds = loadCandidateCcds(session);
				Map<String, ExamScoreData> examScoresByCccd = loadExamScores(session);
				Map<String, BigDecimal> priorityByCccd = loadPriorityScores(session);
				Map<String, BigDecimal> bonusByKey = loadBonusScores(session);
				Map<String, List<ComboData>> combosByMajor = loadCombinations(session);
				Map<String, BigDecimal> thresholdByMajor = loadThresholds(session);

				if (combosByMajor.isEmpty()) {
					throw new SQLException("Chưa có dữ liệu tổ hợp môn theo ngành. Hãy nhập 'Ngành - Tổ hợp' hoặc cột n_tohopgoc của ngành trước khi sinh dữ liệu.");
				}

				@SuppressWarnings("unchecked")
				List<AspirationEntity> aspirations = session.createQuery(
						"from AspirationEntity a order by a.cccd asc, a.thuTuNV asc",
						AspirationEntity.class
				).getResultList();

				int generatedCount = 0;
				int skippedCount = 0;

				for (AspirationEntity aspiration : aspirations) {
					String cccd = normalize(aspiration.getCccd());
					String majorCode = normalize(aspiration.getMaXetTuyen());
					Integer thuTuNV = aspiration.getThuTuNV();

					if (cccd.isEmpty() || majorCode.isEmpty() || thuTuNV == null || thuTuNV <= 0) {
						skippedCount++;
						continue;
					}

					if (!candidateCcds.contains(cccd)) {
						skippedCount++;
						continue;
					}

					ExamScoreData exam = examScoresByCccd.get(cccd);
					if (exam == null) {
						skippedCount++;
						continue;
					}

					List<ComboData> combos = combosByMajor.getOrDefault(majorCode, List.of());
					if (combos.isEmpty()) {
						skippedCount++;
						continue;
					}

					CandidateScore best = null;
					for (ComboData combo : combos) {
						BigDecimal examScore = calculateExamScore(exam, combo);
						if (examScore == null) {
							continue;
						}

						BigDecimal priorityScore = priorityByCccd.getOrDefault(cccd, BigDecimal.ZERO);
						BigDecimal bonusScore = bonusByKey.getOrDefault(buildBonusKey(cccd, majorCode, combo.code, exam.method), BigDecimal.ZERO);
						BigDecimal totalScore = examScore.add(priorityScore).add(bonusScore).add(combo.doLech).setScale(5, RoundingMode.HALF_UP);
						CandidateScore current = new CandidateScore(combo, examScore, priorityScore, bonusScore, totalScore);
						if (best == null || current.totalScore.compareTo(best.totalScore) > 0) {
							best = current;
						}
					}

					if (best == null) {
						skippedCount++;
						continue;
					}

					BigDecimal threshold = thresholdByMajor.getOrDefault(majorCode, BigDecimal.ZERO);
					String resultLabel = best.totalScore.compareTo(threshold) >= 0 ? "Trúng tuyển" : "Chưa trúng tuyển";
					String nvKeys = buildNvKeys(cccd, majorCode, thuTuNV);

					session.createNativeMutationQuery("""
						insert into xt_nguyenvongxettuyen
							(nn_cccd, nv_manganh, nv_tt, diem_thxt, diem_utqd, diem_cong, diem_xettuyen, nv_ketqua, nv_keys, tt_phuongthuc, tt_thm)
						values
							(:cccd, :manganh, :nvTt, :diemThxt, :diemUtqd, :diemCong, :diemXettuyen, :nvKetqua, :nvKeys, :ttPhuongthuc, :ttThm)
						on duplicate key update
							nn_cccd = values(nn_cccd),
							nv_manganh = values(nv_manganh),
							nv_tt = values(nv_tt),
							diem_thxt = values(diem_thxt),
							diem_utqd = values(diem_utqd),
							diem_cong = values(diem_cong),
							diem_xettuyen = values(diem_xettuyen),
							nv_ketqua = values(nv_ketqua),
							nv_keys = values(nv_keys),
							tt_phuongthuc = values(tt_phuongthuc),
							tt_thm = values(tt_thm)
						""")
						.setParameter("cccd", cccd)
						.setParameter("manganh", majorCode)
						.setParameter("nvTt", thuTuNV)
						.setParameter("diemThxt", scale(best.examScore, 5))
						.setParameter("diemUtqd", scale(best.priorityScore, 5))
						.setParameter("diemCong", scale(best.bonusScore, 2))
						.setParameter("diemXettuyen", best.totalScore)
						.setParameter("nvKetqua", resultLabel)
						.setParameter("nvKeys", nvKeys)
						.setParameter("ttPhuongthuc", exam.method)
						.setParameter("ttThm", best.combo.code)
						.executeUpdate();

					generatedCount++;
				}

				tx.commit();
				return new GenerationResult(replaceExisting, aspirations.size(), generatedCount, skippedCount, deletedExistingRows,
						"Sinh dữ liệu xét tuyển thành công: " + generatedCount + "/" + aspirations.size());
			} catch (Exception ex) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw new SQLException("Lỗi sinh dữ liệu xét tuyển: " + ex.getMessage(), ex);
			}
		}
	}

	private Set<String> loadCandidateCcds(Session session) {
		List<String> rows = session.createQuery("select c.cccd from CandidateEntity c where c.cccd is not null", String.class).getResultList();
		Set<String> result = new HashSet<>();
		for (String row : rows) {
			String cccd = normalize(row);
			if (!cccd.isEmpty()) {
				result.add(cccd);
			}
		}
		return result;
	}

	private Map<String, ExamScoreData> loadExamScores(Session session) {
		List<ExamScoreEntity> rows = session.createQuery("from ExamScoreEntity e", ExamScoreEntity.class).getResultList();
		Map<String, ExamScoreData> result = new HashMap<>();
		for (ExamScoreEntity row : rows) {
			String cccd = normalize(row.getCccd());
			if (cccd.isEmpty()) {
				continue;
			}
			ExamScoreData data = new ExamScoreData();
			data.method = normalizeMethod(row.getDPhuongThuc());
			data.to = toBigDecimal(row.getDiemTo());
			data.li = toBigDecimal(row.getDiemLi());
			data.ho = toBigDecimal(row.getDiemHo());
			data.si = toBigDecimal(row.getDiemSi());
			data.su = toBigDecimal(row.getDiemSu());
			data.di = toBigDecimal(row.getDiemDi());
			data.va = toBigDecimal(row.getDiemVa());
			data.n1Thi = toBigDecimal(row.getDiemN1Thi());
			data.n1Cc = toBigDecimal(row.getDiemN1Cc());
			result.putIfAbsent(cccd, data);
		}
		return result;
	}

	private Map<String, BigDecimal> loadPriorityScores(Session session) {
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
				select ts_cccd, coalesce(max(diem_cong_mondatmc), coalesce(max(diem_cong_khongmondatmc), 0))
				from xt_uutien_xettuyen
				group by ts_cccd
				""").list();
		Map<String, BigDecimal> result = new HashMap<>();
		for (Object[] row : rows) {
			String cccd = normalize(row[0]);
			if (!cccd.isEmpty()) {
				result.put(cccd, scale(toBigDecimal(row[1]), 5));
			}
		}
		return result;
	}

	private Map<String, BigDecimal> loadBonusScores(Session session) {
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
				select ts_cccd, manganh, matohop, phuongthuc, coalesce(max(diemTong), 0)
				from xt_diemcongxetuyen
				group by ts_cccd, manganh, matohop, phuongthuc
				""").list();
		Map<String, BigDecimal> result = new HashMap<>();
		for (Object[] row : rows) {
			String key = buildBonusKey(normalize(row[0]), normalize(row[1]), normalize(row[2]), normalizeMethod(row[3]));
			if (!key.isEmpty()) {
				result.put(key, scale(toBigDecimal(row[4]), 2));
			}
		}
		return result;
	}

	private Map<String, List<ComboData>> loadCombinations(Session session) {
		Map<String, List<ComboData>> result = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
				select coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)), nt.matohop, coalesce(nt.dolech, 0),
				       coalesce(nt.th_mon1, ''), coalesce(nt.th_mon2, ''), coalesce(nt.th_mon3, '')
				from xt_nganh_tohop nt
				""").list();
		System.out.println("[DEBUG] loadCombinations: loaded " + rows.size() + " rows from xt_nganh_tohop");
		for (Object[] row : rows) {
			System.out.println("[DEBUG] Processing row: manganh=" + toStr(row[0]) + ", matohop=" + toStr(row[1]) + ", mon1=" + toStr(row[3]) + ", mon2=" + toStr(row[4]) + ", mon3=" + toStr(row[5]));
			addCombo(result, toStr(row[0]), toStr(row[1]), toBigDecimal(row[2]), toStr(row[3]), toStr(row[4]), toStr(row[5]));
		}
		System.out.println("[DEBUG] After main query: result size = " + result.size());

		if (result.isEmpty()) {
			System.out.println("[DEBUG] Main combo query empty, trying fallback from xt_nganh.n_tohopgoc");
			List<NganhEntity> majors = session.createQuery("from NganhEntity n", NganhEntity.class).getResultList();
			System.out.println("[DEBUG] Loaded " + majors.size() + " majors for fallback");
			for (NganhEntity major : majors) {
				addFallbackCombo(result, major);
			}
			System.out.println("[DEBUG] After fallback: result size = " + result.size());
		}

		return result;
	}

	private void addCombo(Map<String, List<ComboData>> result, String majorCode, String comboCode, BigDecimal doLech,
			String mon1, String mon2, String mon3) {
		String normalizedMajor = normalize(majorCode);
		String normalizedCombo = normalizeToHopCode(comboCode);
		System.out.println("[DEBUG] addCombo: majorCode=" + majorCode + ", normalized=" + normalizedMajor + ", comboCode=" + comboCode + 
			", rawMon1=" + mon1 + ", rawMon2=" + mon2 + ", rawMon3=" + mon3);
		if (normalizedMajor.isEmpty() || normalizedCombo.isEmpty()) {
			System.out.println("[DEBUG]   -> Skipped: empty normalized values");
			return;
		}
		ComboData combo = new ComboData();
		combo.majorCode = normalizedMajor;
		combo.code = normalizedCombo;
		combo.doLech = scale(doLech, 2);
		combo.mon1 = scoreFieldForSubject(mon1);
		combo.mon2 = scoreFieldForSubject(mon2);
		combo.mon3 = scoreFieldForSubject(mon3);
		System.out.println("[DEBUG]   -> After scoreFieldForSubject: mon1=" + combo.mon1 + ", mon2=" + combo.mon2 + ", mon3=" + combo.mon3);
		if (combo.mon1 == null || combo.mon2 == null || combo.mon3 == null) {
			String rawSubjects = extractSubjectsText(comboCode);
			String[] parsed = parseSubjectCodes(rawSubjects);
			System.out.println("[DEBUG]   -> Parsing from comboCode: rawSubjects=" + rawSubjects + ", parsed=" + java.util.Arrays.toString(parsed));
			if (combo.mon1 == null && parsed.length > 0) combo.mon1 = parsed[0];
			if (combo.mon2 == null && parsed.length > 1) combo.mon2 = parsed[1];
			if (combo.mon3 == null && parsed.length > 2) combo.mon3 = parsed[2];
		}
		if (combo.mon1 == null || combo.mon2 == null || combo.mon3 == null) {
			System.out.println("[DEBUG]   -> Skipped: null subjects: mon1=" + combo.mon1 + ", mon2=" + combo.mon2 + ", mon3=" + combo.mon3);
			return;
		}
		System.out.println("[DEBUG]   -> Added: mon1=" + combo.mon1 + ", mon2=" + combo.mon2 + ", mon3=" + combo.mon3);
		result.computeIfAbsent(normalizedMajor, key -> new ArrayList<>()).add(combo);
	}

	private void addFallbackCombo(Map<String, List<ComboData>> result, NganhEntity major) {
		String majorCode = normalize(major.getManganh());
		String raw = major.getN_tohopgoc();
		System.out.println("[DEBUG] addFallbackCombo: majorCode=" + major.getManganh() + ", n_tohopgoc=" + raw);
		if (majorCode.isEmpty() || raw == null || raw.trim().isEmpty()) {
			System.out.println("[DEBUG]   -> Skipped: empty major or n_tohopgoc");
			return;
		}
		String comboCode = normalizeToHopCode(raw);
		String[] parsed = parseSubjectCodes(extractSubjectsText(raw));
		if (parsed.length < 3) {
			parsed = parseSubjectCodes(raw);
		}
		System.out.println("[DEBUG]   -> Parsed " + parsed.length + " subjects");
		if (parsed.length < 3) {
			System.out.println("[DEBUG]   -> Skipped: not enough subjects");
			return;
		}
		ComboData combo = new ComboData();
		combo.majorCode = majorCode;
		combo.code = comboCode.isEmpty() ? normalize(raw) : comboCode;
		combo.doLech = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		combo.mon1 = parsed[0];
		combo.mon2 = parsed[1];
		combo.mon3 = parsed[2];
		System.out.println("[DEBUG]   -> Added fallback: mon1=" + combo.mon1 + ", mon2=" + combo.mon2 + ", mon3=" + combo.mon3);
		result.computeIfAbsent(majorCode, key -> new ArrayList<>()).add(combo);
	}

	private static String normalizeToHopCode(String rawCode) {
		String value = toStr(rawCode);
		int bracketIndex = value.indexOf('(');
		if (bracketIndex > 0) {
			return value.substring(0, bracketIndex).trim();
		}
		return value;
	}

	private static String extractSubjectsText(String rawText) {
		String value = toStr(rawText);
		int open = value.indexOf('(');
		int close = value.indexOf(')', open + 1);
		if (open >= 0 && close > open) {
			return value.substring(open + 1, close);
		}
		return value;
	}

	private static String[] parseSubjectCodes(String rawText) {
		String normalized = extractSubjectsText(rawText)
				.replace('/', ',')
				.replace(';', ',')
				.replace('+', ',');
		String[] tokens = normalized.split(",|\\s+");
		List<String> codes = new ArrayList<>();
		for (String token : tokens) {
			String code = scoreFieldForSubject(token);
			if (code != null && !codes.contains(code)) {
				codes.add(code);
			}
		}
		return codes.toArray(new String[0]);
	}

	private Map<String, BigDecimal> loadThresholds(Session session) {
		List<NganhEntity> rows = session.createQuery("from NganhEntity n", NganhEntity.class).getResultList();
		Map<String, BigDecimal> result = new HashMap<>();
		for (NganhEntity row : rows) {
			String majorCode = normalize(row.getManganh());
			if (!majorCode.isEmpty()) {
				BigDecimal threshold = row.getN_diemtrungtuyen() != null ? row.getN_diemtrungtuyen() : row.getN_diemsan();
				result.put(majorCode, scale(threshold, 5));
			}
		}
		return result;
	}

	private BigDecimal calculateExamScore(ExamScoreData exam, ComboData combo) {
		if (exam == null || combo == null) {
			return null;
		}

		if ("DGNL".equals(exam.method)) {
			BigDecimal raw = firstNonNull(exam.n1Thi, exam.n1Cc);
			return raw == null ? null : raw.multiply(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(1200), 5, RoundingMode.HALF_UP);
		}

		if ("VSAT".equals(exam.method)) {
			BigDecimal s1 = convertSubjectScore(exam, combo.mon1, true);
			BigDecimal s2 = convertSubjectScore(exam, combo.mon2, true);
			BigDecimal s3 = convertSubjectScore(exam, combo.mon3, true);
			if (s1 == null || s2 == null || s3 == null) {
				return null;
			}
			return s1.add(s2).add(s3).setScale(5, RoundingMode.HALF_UP);
		}

		BigDecimal s1 = convertSubjectScore(exam, combo.mon1, false);
		BigDecimal s2 = convertSubjectScore(exam, combo.mon2, false);
		BigDecimal s3 = convertSubjectScore(exam, combo.mon3, false);
		if (s1 == null || s2 == null || s3 == null) {
			return null;
		}
		return s1.add(s2).add(s3).setScale(5, RoundingMode.HALF_UP);
	}

	private BigDecimal convertSubjectScore(ExamScoreData exam, String fieldName, boolean isVsat) {
		BigDecimal raw = rawSubjectScore(exam, fieldName);
		if (raw == null) {
			return null;
		}
		if (!isVsat) {
			return raw.setScale(5, RoundingMode.HALF_UP);
		}
		if (raw.compareTo(BigDecimal.TEN) <= 0) {
			return raw.setScale(5, RoundingMode.HALF_UP);
		}
		return raw.divide(BigDecimal.valueOf(15), 5, RoundingMode.HALF_UP);
	}

	private BigDecimal rawSubjectScore(ExamScoreData exam, String fieldName) {
		if (exam == null || fieldName == null) {
			return null;
		}
		return switch (fieldName) {
			case "TO" -> exam.to;
			case "LI" -> exam.li;
			case "HO" -> exam.ho;
			case "SI" -> exam.si;
			case "SU" -> exam.su;
			case "DI" -> exam.di;
			case "VA" -> exam.va;
			case "N1_THI" -> exam.n1Thi;
			default -> null;
		};
	}

	private static String scoreFieldForSubject(String subject) {
		String norm = normalize(subject);
		if (norm.contains("toan") || norm.equals("to")) return "TO";
		if (norm.contains("vatly") || norm.contains("ly") || norm.equals("li")) return "LI";
		if (norm.contains("hoahoc") || norm.contains("hoa") || norm.equals("ho")) return "HO";
		if (norm.contains("sinhhoc") || norm.contains("sinh") || norm.equals("si")) return "SI";
		if (norm.contains("lichsu") || norm.equals("su")) return "SU";
		if (norm.contains("diaky") || norm.equals("di")) return "DI";
		if (norm.contains("nguvan") || norm.contains("van") || norm.equals("va")) return "VA";
		if (norm.contains("anh") || norm.equals("n") || norm.equals("n1")) return "N1_THI";
		return null;
	}

	private static String normalizeMethod(Object method) {
		String norm = normalize(method);
		if (norm.contains("thpt")) return "THPT";
		if (norm.contains("vsat")) return "VSAT";
		if (norm.contains("dgnl")) return "DGNL";
		return norm.toUpperCase(Locale.ROOT);
	}

	private static String normalize(Object value) {
		if (value == null) {
			return "";
		}
		String text = value.toString().trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
		return text.replaceAll("[^a-z0-9]", "");
	}

	private static BigDecimal scale(BigDecimal value, int scale) {
		return value == null ? BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP) : value.setScale(scale, RoundingMode.HALF_UP);
	}

	private static BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		try {
			String text = value.toString().trim().replace(',', '.');
			return text.isEmpty() ? null : new BigDecimal(text);
		} catch (Exception ex) {
			return null;
		}
	}

	private static BigDecimal firstNonNull(BigDecimal left, BigDecimal right) {
		return left != null ? left : right;
	}

	private static String toStr(Object value) {
		return value == null ? "" : value.toString().trim();
	}

	private static String buildNvKeys(String cccd, String majorCode, Integer thuTuNV) {
		return normalize(cccd) + "_" + normalize(majorCode) + "_" + (thuTuNV == null ? "0" : thuTuNV);
	}

	private static String buildBonusKey(String cccd, String majorCode, String comboCode, String method) {
		return normalize(cccd) + "|" + normalize(majorCode) + "|" + normalize(comboCode) + "|" + normalizeMethod(method);
	}

	private static boolean isValid(NguyenVongXetTuyenDTO dto) {
		if (dto == null) return false;
		String cccd = dto.getNnCccd();
		if (cccd == null || cccd.trim().isEmpty()) return false;
		String nganh = dto.getNvManganh();
		if (nganh == null || nganh.trim().isEmpty()) return false;
		return true;
	}

	static class ExamScoreData {
		String method;
		BigDecimal to;
		BigDecimal li;
		BigDecimal ho;
		BigDecimal si;
		BigDecimal su;
		BigDecimal di;
		BigDecimal va;
		BigDecimal n1Thi;
		BigDecimal n1Cc;
	}

	static class ComboData {
		String majorCode;
		String code;
		BigDecimal doLech;
		String mon1;
		String mon2;
		String mon3;
	}

	static class CandidateScore {
		private final ComboData combo;
		private final BigDecimal examScore;
		private final BigDecimal priorityScore;
		private final BigDecimal bonusScore;
		private final BigDecimal totalScore;

		CandidateScore(ComboData combo, BigDecimal examScore, BigDecimal priorityScore, BigDecimal bonusScore, BigDecimal totalScore) {
			this.combo = combo;
			this.examScore = examScore;
			this.priorityScore = priorityScore;
			this.bonusScore = bonusScore;
			this.totalScore = totalScore;
		}
	}

	public static class GenerationResult {
		private final boolean replaceExisting;
		private final int sourceRows;
		private final int generatedRows;
		private final int skippedRows;
		private final int deletedExistingRows;
		private final String message;

		public GenerationResult(boolean replaceExisting, int sourceRows, int generatedRows, int skippedRows, int deletedExistingRows, String message) {
			this.replaceExisting = replaceExisting;
			this.sourceRows = sourceRows;
			this.generatedRows = generatedRows;
			this.skippedRows = skippedRows;
			this.deletedExistingRows = deletedExistingRows;
			this.message = message;
		}

		public boolean isReplaceExisting() {
			return replaceExisting;
		}

		public int getSourceRows() {
			return sourceRows;
		}

		public int getGeneratedRows() {
			return generatedRows;
		}

		public int getSkippedRows() {
			return skippedRows;
		}

		public int getDeletedExistingRows() {
			return deletedExistingRows;
		}

		public String getMessage() {
			return message;
		}
	}
}
