package bus;

import dal.dao.NguyenVongXetTuyenDAO;
import dal.entities.AspirationEntity;
import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.NganhEntity;
import dal.hibernate.HibernateUtil;
import dto.AdmissionMethodSummaryDTO;
import dto.NguyenVongXetTuyenDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

	public List<AdmissionMethodSummaryDTO> getAdmissionMethodSummaries() throws SQLException {
		List<Object[]> rows = dao.findAdmittedCountsByMajorAndMethod();
		Map<String, AdmissionMethodSummaryDTO> result = new LinkedHashMap<>();
		Map<String, String> majorNames = loadMajorNames();

		for (Object[] row : rows) {
			String majorCode = toStr(row[0]);
			String rawMethod = toStr(row[1]);
			String rawCombo = toStr(row[2]);
			String method = resolveAdmissionMethodBucket(rawMethod, rawCombo);
			long count = row[3] instanceof Number number ? number.longValue() : 0L;

			if (majorCode.isEmpty() || count <= 0 || method.isEmpty()) {
				continue;
			}

			AdmissionMethodSummaryDTO summary = result.computeIfAbsent(majorCode, key -> {
				AdmissionMethodSummaryDTO dto = new AdmissionMethodSummaryDTO();
				dto.setMajorCode(key);
				dto.setMajorName(majorNames.getOrDefault(key, key));
				return dto;
			});

			Map<String, Long> methodCounts = summary.getMethodCounts();
			methodCounts.put(method, methodCounts.getOrDefault(method, 0L) + count);
			summary.setTotalCount(summary.getTotalCount() + count);
		}

		return new ArrayList<>(result.values());
	}

	private String resolveAdmissionMethodBucket(String rawMethod, String rawCombo) {
		String method = normalizeMethod(rawMethod);
		if ("THPT".equals(method) || "VSAT".equals(method) || "DGNL".equals(method)) {
			return method;
		}

		String combo = normalize(rawCombo).toUpperCase(Locale.ROOT);
		String normalizedMethod = normalize(rawMethod).toUpperCase(Locale.ROOT);

		if (looksLikeThptCode(normalizedMethod) || looksLikeThptCode(combo)) {
			return "THPT";
		}

		if (normalizedMethod.contains("VSAT") || combo.contains("VSAT")) {
			return "VSAT";
		}

		if (normalizedMethod.contains("DGNL") || combo.contains("DGNL")) {
			return "DGNL";
		}

		return "";
	}

	private boolean looksLikeThptCode(String code) {
		if (code == null || code.isEmpty()) {
			return false;
		}
		if ("N1".equals(code) || "N1THI".equals(code) || "THPT".equals(code)) {
			return true;
		}
		return code.matches("^[ABCDXK][0-9]{2,3}[A-Z]*$");
	}

	private Map<String, String> loadMajorNames() throws SQLException {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<NganhEntity> majors = session.createQuery("from NganhEntity n", NganhEntity.class).getResultList();
			Map<String, String> result = new HashMap<>();
			for (NganhEntity major : majors) {
				String code = normalize(major.getManganh());
				if (code.isEmpty()) {
					continue;
				}
				String name = major.getTennganh() == null || major.getTennganh().trim().isEmpty()
						? major.getManganh()
						: major.getTennganh().trim();
				result.put(code, name);
			}
			return result;
		} catch (Exception ex) {
			throw new SQLException("Không thể tải tên ngành: " + ex.getMessage(), ex);
		}
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

				Map<String, String> candidateCcdByKey = loadCandidateCcdByKey(session);
				Set<String> candidateCcds = candidateCcdByKey.keySet();
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
				Map<String, Integer> reasonCounts = new HashMap<>();

				for (AspirationEntity aspiration : aspirations) {
					String cccd = normalize(aspiration.getCccd());
					String displayCccd = resolveDisplayCccd(aspiration.getCccd(), candidateCcdByKey.get(cccd));
					String majorCode = normalize(aspiration.getMaXetTuyen());
					Integer thuTuNV = aspiration.getThuTuNV();

					if (cccd.isEmpty() || displayCccd.isEmpty() || majorCode.isEmpty() || thuTuNV == null || thuTuNV <= 0) {
						incrementReason(reasonCounts, "invalid_input");
						skippedCount++;
						continue;
					}

					if (!candidateCcds.contains(cccd)) {
						incrementReason(reasonCounts, "candidate_not_found");
						skippedCount++;
						continue;
					}

					ExamScoreData exam = examScoresByCccd.get(cccd);
					if (exam == null) {
						incrementReason(reasonCounts, "exam_not_found");
						skippedCount++;
						continue;
					}

					List<ComboData> combos = combosByMajor.getOrDefault(majorCode, List.of());
					if (combos.isEmpty()) {
						incrementReason(reasonCounts, "combo_missing");
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
						incrementReason(reasonCounts, "score_uncomputable");
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
						.setParameter("cccd", displayCccd)
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
					incrementReason(reasonCounts, "generated");
				}

				tx.commit();
				String reasonSummary = buildReasonSummary(reasonCounts);
				return new GenerationResult(replaceExisting, aspirations.size(), generatedCount, skippedCount, deletedExistingRows,
						"Sinh dữ liệu xét tuyển thành công: " + generatedCount + "/" + aspirations.size()
								+ (reasonSummary.isEmpty() ? "" : " | " + reasonSummary));
			} catch (Exception ex) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw new SQLException("Lỗi sinh dữ liệu xét tuyển: " + ex.getMessage(), ex);
			}
		}
	}

	private void incrementReason(Map<String, Integer> reasonCounts, String reasonKey) {
		reasonCounts.merge(reasonKey, 1, Integer::sum);
	}

	private String buildReasonSummary(Map<String, Integer> reasonCounts) {
		if (reasonCounts == null || reasonCounts.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		appendReason(builder, reasonCounts, "generated", "generated");
		appendReason(builder, reasonCounts, "invalid_input", "invalid");
		appendReason(builder, reasonCounts, "candidate_not_found", "noCandidate");
		appendReason(builder, reasonCounts, "exam_not_found", "noExam");
		appendReason(builder, reasonCounts, "combo_missing", "noCombo");
		appendReason(builder, reasonCounts, "score_uncomputable", "noScore");
		return builder.toString();
	}

	private void appendReason(StringBuilder builder, Map<String, Integer> reasonCounts, String key, String label) {
		Integer value = reasonCounts.get(key);
		if (value == null || value <= 0) {
			return;
		}
		if (builder.length() > 0) {
			builder.append(", ");
		}
		builder.append(label).append("=").append(value);
	}

	private Map<String, String> loadCandidateCcdByKey(Session session) {
		List<String> rows = session.createQuery("select c.cccd from CandidateEntity c where c.cccd is not null", String.class).getResultList();
		Map<String, String> result = new HashMap<>();
		for (String row : rows) {
			String cccdKey = normalize(row);
			String displayCccd = toStr(row);
			if (!cccdKey.isEmpty() && !displayCccd.isEmpty()) {
				result.putIfAbsent(cccdKey, displayCccd);
			}
		}
		return result;
	}

	private String resolveDisplayCccd(String aspirationCccd, String candidateCccd) {
		if (candidateCccd != null && !candidateCccd.trim().isEmpty()) {
			return candidateCccd.trim();
		}
		return toStr(aspirationCccd);
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
				select coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)), nt.matohop, coalesce(nt.dolech, 0), coalesce(nt.hsmon1, 1), coalesce(nt.hsmon2, 1), coalesce(nt.hsmon3, 1)
				from xt_nganh_tohop nt
				""").list();
		System.out.println("[DEBUG] loadCombinations: loaded " + rows.size() + " rows from xt_nganh_tohop");
			for (Object[] row : rows) {
				String comboCode = toStr(row[1]);
				BigDecimal w1 = toBigDecimal(row[3]);
				BigDecimal w2 = toBigDecimal(row[4]);
				BigDecimal w3 = toBigDecimal(row[5]);
				System.out.println("[DEBUG] Processing row: manganh=" + toStr(row[0]) + ", matohop=" + comboCode + ", weights=" + w1 + "," + w2 + "," + w3);
				addComboFromCode(result, toStr(row[0]), comboCode, toBigDecimal(row[2]), w1, w2, w3);
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

	private void addComboFromCode(Map<String, List<ComboData>> result, String majorCode, String comboCode, BigDecimal doLech, BigDecimal w1, BigDecimal w2, BigDecimal w3) {
		// Map combo codes to subjects based on Vietnamese standard
		Map<String, String[]> comboMap = new HashMap<>();
		// Khối A: Toán, Lý, Hóa
		for (String code : new String[]{"A00", "A01", "A02", "A03", "A04", "A05", "A06", "A07"}) {
			comboMap.put(code, new String[]{"TO", "LI", "HO"});
		}
		// Khối B: Toán, Hóa, Sinh
		for (String code : new String[]{"B00", "B01", "B02", "B03", "B08"}) {
			comboMap.put(code, new String[]{"TO", "HO", "SI"});
		}
		// Khối C: Toán, Văn, Anh
		for (String code : new String[]{"C01", "C02", "C03", "C04"}) {
			comboMap.put(code, new String[]{"TO", "VA", "N1_THI"});
		}
		// Khối D: Toán, Văn, Anh (hoặc D01, D07, D09, D10, D11, D12, D13, D14, D15)
		for (String code : new String[]{"D01", "D07", "D09", "D10", "D11", "D12", "D13", "D14", "D15"}) {
			comboMap.put(code, new String[]{"TO", "VA", "N1_THI"});
		}
		// Khối X: varied combinations
		comboMap.put("X01", new String[]{"TO", "VA", "SU"});
		comboMap.put("X02", new String[]{"TO", "VA", "DI"});
		comboMap.put("X03", new String[]{"TO", "VA", "KHAC"});
		comboMap.put("X04", new String[]{"LI", "HO", "SI"});
		comboMap.put("X05", new String[]{"LI", "HO", "SI"});
		comboMap.put("X06", new String[]{"LI", "HO", "DI"});
		comboMap.put("X07", new String[]{"LI", "HO", "VA"});
		comboMap.put("X08", new String[]{"HO", "SI", "VA"});
		comboMap.put("X09", new String[]{"LI", "SI", "VA"});
		comboMap.put("X10", new String[]{"LI", "HO", "VA"});
		comboMap.put("X11", new String[]{"TO", "SU", "DI"});
		comboMap.put("X12", new String[]{"TO", "SU", "VA"});
		comboMap.put("X13", new String[]{"TO", "SI", "VA"});
		comboMap.put("X14", new String[]{"TO", "SI", "VA"});
		comboMap.put("X15", new String[]{"TO", "SI", "VA"});
		comboMap.put("X16", new String[]{"TO", "SI", "VA"});
		comboMap.put("X17", new String[]{"TO", "SU", "DI"});
		comboMap.put("X18", new String[]{"TO", "SU", "VA"});
		comboMap.put("X19", new String[]{"TO", "DI", "VA"});
		comboMap.put("X20", new String[]{"TO", "DI", "SU"});
		comboMap.put("X21", new String[]{"TO", "DI", "SU"});
		comboMap.put("X22", new String[]{"TO", "DI", "SU"});
		comboMap.put("X23", new String[]{"TO", "DI", "SU"});
		comboMap.put("X24", new String[]{"TO", "DI", "SU"});
		comboMap.put("X25", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X26", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X27", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X28", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X53", new String[]{"TO", "VA", "SU"});
		comboMap.put("X54", new String[]{"TO", "VA", "DI"});
		comboMap.put("X55", new String[]{"TO", "VA", "SU"});
		comboMap.put("X56", new String[]{"TO", "VA", "DI"});
		comboMap.put("X57", new String[]{"TO", "VA", "SU"});
		comboMap.put("X78", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X79", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X80", new String[]{"N1_THI", "LI", "HO"});
		comboMap.put("X81", new String[]{"N1_THI", "LI", "HO"});

		String[] subjects = comboMap.get(comboCode);
			if (subjects != null && subjects.length >= 3) {
				addCombo(result, majorCode, comboCode, doLech, w1, w2, w3, subjects[0], subjects[1], subjects[2]);
			} else {
			System.out.println("[DEBUG] Unknown combo code: " + comboCode + ", skipping");
		}
	}

	private void addCombo(Map<String, List<ComboData>> result, String majorCode, String comboCode, BigDecimal doLech, BigDecimal w1, BigDecimal w2, BigDecimal w3,
			String mon1, String mon2, String mon3) {
		String normalizedMajor = normalize(majorCode);
		String normalizedCombo = normalizeToHopCode(comboCode);
		System.out.println("[DEBUG] addCombo: majorCode=" + majorCode + ", normalized=" + normalizedMajor + ", comboCode=" + comboCode + 
			", mon1=" + mon1 + ", mon2=" + mon2 + ", mon3=" + mon3);
		if (normalizedMajor.isEmpty() || normalizedCombo.isEmpty()) {
			System.out.println("[DEBUG]   -> Skipped: empty normalized values");
			return;
		}
		ComboData combo = new ComboData();
		combo.majorCode = normalizedMajor;
		combo.code = normalizedCombo;
		combo.doLech = scale(doLech, 2);
		combo.weight1 = w1 == null ? BigDecimal.ONE : scale(w1, 2);
		combo.weight2 = w2 == null ? BigDecimal.ONE : scale(w2, 2);
		combo.weight3 = w3 == null ? BigDecimal.ONE : scale(w3, 2);
		combo.mon1 = mon1;
		combo.mon2 = mon2;
		combo.mon3 = mon3;
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
			combo.weight1 = BigDecimal.ONE;
			combo.weight2 = BigDecimal.ONE;
			combo.weight3 = BigDecimal.ONE;
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
			BigDecimal raw = maxNonNull(exam.n1Thi, exam.n1Cc);
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
		return calculateWeightedScore(s1, s2, s3, combo);
	}

	private BigDecimal calculateWeightedScore(BigDecimal subject1, BigDecimal subject2, BigDecimal subject3, ComboData combo) {
		BigDecimal weight1 = combo.weight1 == null ? BigDecimal.ONE : combo.weight1;
		BigDecimal weight2 = combo.weight2 == null ? BigDecimal.ONE : combo.weight2;
		BigDecimal weight3 = combo.weight3 == null ? BigDecimal.ONE : combo.weight3;
		BigDecimal totalWeight = weight1.add(weight2).add(weight3);
		if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
			totalWeight = BigDecimal.valueOf(3);
			weight1 = BigDecimal.ONE;
			weight2 = BigDecimal.ONE;
			weight3 = BigDecimal.ONE;
		}

		BigDecimal weightedAverage = subject1.multiply(weight1)
			.add(subject2.multiply(weight2))
			.add(subject3.multiply(weight3))
			.divide(totalWeight, 10, RoundingMode.HALF_UP);
		return weightedAverage.multiply(BigDecimal.valueOf(3)).setScale(5, RoundingMode.HALF_UP);
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

	private static BigDecimal maxNonNull(BigDecimal left, BigDecimal right) {
		if (left == null) return right;
		if (right == null) return left;
		return left.compareTo(right) >= 0 ? left : right;
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
		BigDecimal weight1;
		BigDecimal weight2;
		BigDecimal weight3;
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
