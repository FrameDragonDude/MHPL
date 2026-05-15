package backend.service;

import backend.dto.NguyenVongXetTuyenGenerationResultDTO;
import dal.entities.AspirationEntity;
import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.entities.NganhEntity;
import dal.hibernate.HibernateUtil;
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
import org.springframework.stereotype.Service;

@Service
public class NguyenVongXetTuyenGenerationService {
    private static final BigDecimal PRIORITY_THRESHOLD = new BigDecimal("22.5");
    private static final BigDecimal MAX_SCORE = new BigDecimal("30");
    private static final BigDecimal PRIORITY_DIVISOR = new BigDecimal("7.5");

    public NguyenVongXetTuyenGenerationResultDTO generate(boolean replaceExisting) throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                int deletedExistingRows = 0;
                if (replaceExisting) {
                    deletedExistingRows = session.createNativeMutationQuery("delete from xt_nguyenvongxettuyen")
                            .executeUpdate();
                }

                List<AspirationEntity> aspirations = session.createQuery(
                        "from AspirationEntity a order by a.cccd asc, a.thuTuNV asc",
                        AspirationEntity.class
                ).getResultList();
                Set<String> candidateCcds = loadCandidateCcds(session);
                Map<String, ExamScoreData> examScoresByCccd = loadExamScores(session);
                Map<String, PriorityData> priorityByCccd = loadPriorityScores(session);
                Map<String, BigDecimal> bonusByKey = loadBonusScores(session);
                Map<String, List<ComboData>> combosByMajor = loadCombinations(session);
                Map<String, BigDecimal> thresholdByMajor = loadThresholds(session);
                Map<String, List<ConversionRule>> conversionRules = loadConversionRules(session);

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

                    String matchedCccd = findMatchingCandidateNormalized(session, cccd, candidateCcds);
                    if (matchedCccd == null) {
                        skippedCount++;
                        continue;
                    }
                    cccd = matchedCccd;

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
                        BigDecimal examScore = calculateExamScore(exam, combo, conversionRules);
                        if (examScore == null) {
                            continue;
                        }

                        BigDecimal bonusScore = bonusByKey.getOrDefault(buildBonusKey(cccd, majorCode, combo.code, exam.method), BigDecimal.ZERO);
                        BigDecimal doLech = combo.doLech == null ? BigDecimal.ZERO : combo.doLech;
                        BigDecimal adjustedExamScore = examScore.add(doLech).setScale(5, RoundingMode.HALF_UP);
                        BigDecimal priorityScore = calculatePriorityScore(priorityByCccd.get(cccd), adjustedExamScore, bonusScore);
                        BigDecimal totalScore = adjustedExamScore.add(priorityScore).add(bonusScore).setScale(5, RoundingMode.HALF_UP);

                        CandidateScore current = new CandidateScore(combo, examScore, priorityScore, bonusScore, doLech, totalScore);
                        if (best == null || current.totalScore.compareTo(best.totalScore) > 0
                                || (current.totalScore.compareTo(best.totalScore) == 0 && current.combo.code.compareTo(best.combo.code) < 0)) {
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

                NguyenVongXetTuyenGenerationResultDTO result = new NguyenVongXetTuyenGenerationResultDTO();
                result.setReplaceExisting(replaceExisting);
                result.setSourceRows(aspirations.size());
                result.setGeneratedRows(generatedCount);
                result.setInsertedRows(generatedCount);
                result.setSkippedRows(skippedCount);
                result.setDeletedExistingRows(deletedExistingRows);
                result.setMessage("Sinh dữ liệu xét tuyển thành công: " + generatedCount + "/" + aspirations.size());
                return result;
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new SQLException("Lỗi sinh dữ liệu: " + ex.getMessage(), ex);
            }
        }
    }

    private Set<String> loadCandidateCcds(Session session) {
        @SuppressWarnings("unchecked")
        List<String> rows = session.createQuery("select c.cccd from CandidateEntity c where c.cccd is not null", String.class)
                .getResultList();
        Set<String> results = new HashSet<>();
        for (String row : rows) {
            String cccd = normalize(row);
            if (!cccd.isEmpty()) {
                results.add(cccd);
            }
        }
        return results;
    }

    private String findMatchingCandidateNormalized(Session session, String normalizedCccd, Set<String> candidateCcds) {
        if (normalizedCccd == null || normalizedCccd.isEmpty()) return null;

        // Direct hit
        if (candidateCcds.contains(normalizedCccd)) return normalizedCccd;

        // Try variants: with/without 'ts' prefix, padded numbers
        String work = normalizedCccd;
        String noPrefix = work.startsWith("ts") ? work.substring(2) : work;

        List<String> variants = new ArrayList<>();
        variants.add(work);
        variants.add(noPrefix);
        if (!noPrefix.isEmpty()) {
            // Try padded numeric forms if possible
            try {
                int n = Integer.parseInt(noPrefix);
                variants.add("ts" + String.format("%04d", n));
                variants.add(String.format("%04d", n));
            } catch (NumberFormatException ignored) {
            }
        }

        for (String v : variants) {
            if (v != null && candidateCcds.contains(v)) return v;
        }

        // Last resort: query DB comparing ccid after removing common separators
        // Normalize in SQL by removing '_' '-' and spaces and lowercasing
        String sql = "select c.cccd from xt_thisinhxettuyen25 c where lower(replace(replace(replace(c.cccd,'_',''),'-',''),' ','')) = :norm limit 1";
        @SuppressWarnings("unchecked")
        List<String> rows = session.createNativeQuery(sql).setParameter("norm", normalizedCccd).getResultList();
        if (!rows.isEmpty()) {
            String found = normalize(rows.get(0));
            if (!found.isEmpty()) return found;
        }

        return null;
    }

    private Map<String, ExamScoreData> loadExamScores(Session session) {
        List<ExamScoreEntity> rows = session.createQuery("from ExamScoreEntity e", ExamScoreEntity.class).getResultList();
        Map<String, ExamScoreData> results = new HashMap<>();
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
            results.putIfAbsent(cccd, data);
        }
        return results;
    }

    private Map<String, PriorityData> loadPriorityScores(Session session) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select ts_cccd,
                       coalesce(max(diem_cong_mondatmc), 0),
                       coalesce(max(diem_cong_khongmondatmc), 0)
                from xt_uutien_xettuyen
                group by ts_cccd
                """).list();

        Map<String, PriorityData> results = new HashMap<>();
        for (Object[] row : rows) {
            String cccd = normalize(row[0]);
            if (!cccd.isEmpty()) {
                PriorityData data = new PriorityData();
                data.diemCongMonDatMc = scale(toBigDecimal(row[1]), 5);
                data.diemCongKhongMonDatMc = scale(toBigDecimal(row[2]), 5);
                results.put(cccd, data);
            }
        }
        return results;
    }

    // region Priority / bonus / combination rules

    private BigDecimal calculatePriorityScore(PriorityData priorityData, BigDecimal adjustedExamScore, BigDecimal bonusScore) {
        BigDecimal basePriority = priorityData == null ? BigDecimal.ZERO : priorityData.resolveBaseScore();
        if (basePriority.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(5, RoundingMode.HALF_UP);
        }

        BigDecimal combinedBeforePriority = safe(adjustedExamScore).add(safe(bonusScore));
        if (combinedBeforePriority.compareTo(PRIORITY_THRESHOLD) < 0) {
            return basePriority.setScale(5, RoundingMode.HALF_UP);
        }

        BigDecimal factor = MAX_SCORE.subtract(combinedBeforePriority)
                .divide(PRIORITY_DIVISOR, 10, RoundingMode.HALF_UP);
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            factor = BigDecimal.ZERO;
        }

        return basePriority.multiply(factor).setScale(5, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> loadBonusScores(Session session) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select ts_cccd, manganh, matohop, phuongthuc, coalesce(max(diemTong), 0)
                from xt_diemcongxetuyen
                group by ts_cccd, manganh, matohop, phuongthuc
                """).list();

        Map<String, BigDecimal> results = new HashMap<>();
        for (Object[] row : rows) {
            String key = buildBonusKey(normalize(row[0]), normalize(row[1]), normalize(row[2]), normalizeMethod(row[3]));
            if (!key.isEmpty()) {
                results.put(key, scale(toBigDecimal(row[4]), 2));
            }
        }
        return results;
    }

    private Map<String, List<ComboData>> loadCombinations(Session session) {
        // Load major-combo relationships separately to avoid collation JOIN issues
        @SuppressWarnings("unchecked")
        List<Object[]> comboRows = session.createNativeQuery("""
              select coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)),
                  nt.matohop,
                  coalesce(nt.dolech, 0),
                  coalesce(nt.hsmon1, 1),
                  coalesce(nt.hsmon2, 1),
                  coalesce(nt.hsmon3, 1)
                from xt_nganh_tohop nt
                """).list();

        // Load subject-combo mappings separately
        Map<String, Object[]> subjectsByCombo = loadSubjectsByCombo(session);

        Map<String, List<ComboData>> results = new HashMap<>();
        for (Object[] row : comboRows) {
            String majorCode = normalize(row[0]);
            String comboCode = normalize(row[1]);
            if (majorCode.isEmpty() || comboCode.isEmpty()) {
                continue;
            }

            ComboData combo = new ComboData();
            combo.majorCode = majorCode;
            combo.code = comboCode;
            combo.doLech = scale(toBigDecimal(row[2]), 2);
            combo.weight1 = scale(toBigDecimal(row[3]), 2);
            combo.weight2 = scale(toBigDecimal(row[4]), 2);
            combo.weight3 = scale(toBigDecimal(row[5]), 2);

            // Lookup subjects in the offline map
            Object[] subjectRow = subjectsByCombo.getOrDefault(comboCode, new Object[]{"", "", ""});
            combo.mon1 = scoreFieldForSubject(toStr(subjectRow[0]));
            combo.mon2 = scoreFieldForSubject(toStr(subjectRow[1]));
            combo.mon3 = scoreFieldForSubject(toStr(subjectRow[2]));

            results.computeIfAbsent(majorCode, key -> new ArrayList<>()).add(combo);
        }
        return results;
    }

    // endregion

    // region Threshold / conversion rules

    private Map<String, Object[]> loadSubjectsByCombo(Session session) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select matohop, coalesce(mon1, ''), coalesce(mon2, ''), coalesce(mon3, '')
                from xt_tohop_monthi
                """).list();

        Map<String, Object[]> results = new HashMap<>();
        for (Object[] row : rows) {
            String comboCode = normalize(row[0]);
            if (!comboCode.isEmpty()) {
                results.put(comboCode, new Object[]{row[1], row[2], row[3]});
            }
        }
        return results;
    }

    private Map<String, BigDecimal> loadThresholds(Session session) {
        List<NganhEntity> rows = session.createQuery("from NganhEntity n", NganhEntity.class).getResultList();
        Map<String, BigDecimal> results = new HashMap<>();
        for (NganhEntity row : rows) {
            String majorCode = normalize(row.getManganh());
            if (majorCode.isEmpty()) {
                continue;
            }
            BigDecimal threshold = row.getN_diemtrungtuyen() != null ? row.getN_diemtrungtuyen() : row.getN_diemsan();
            results.put(majorCode, scale(threshold, 5));
        }
        return results;
    }

    private Map<String, List<ConversionRule>> loadConversionRules(Session session) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select d_phuongthuc, coalesce(d_tohop, ''), coalesce(d_mon, ''), d_diema, d_diemb, d_diemc, d_diemd
                from xt_bangquydoi
                where d_phuongthuc in ('DGNL', 'VSAT')
                """).list();

        Map<String, List<ConversionRule>> results = new HashMap<>();
        for (Object[] row : rows) {
            String method = normalizeMethod(row[0]);
            String keyPart = method.equals("VSAT") ? scoreFieldForSubject(toStr(row[2])) : normalize(row[1]);
            if (method.equals("DGNL")) {
                keyPart = normalize(row[1]);
            }
            if (method.isEmpty() || keyPart.isEmpty()) {
                continue;
            }

            ConversionRule rule = new ConversionRule();
            rule.fromScore = toBigDecimal(row[3]);
            rule.toScore = toBigDecimal(row[4]);
            rule.convertedFrom = toBigDecimal(row[5]);
            rule.convertedTo = toBigDecimal(row[6]);

            String key = conversionKey(method, keyPart);
            results.computeIfAbsent(key, unused -> new ArrayList<>()).add(rule);
        }
        return results;
    }

    // endregion

    // region Score calculation

    private BigDecimal calculateExamScore(ExamScoreData exam, ComboData combo, Map<String, List<ConversionRule>> conversionRules) {
        if (exam == null || combo == null) {
            return null;
        }

        if ("DGNL".equals(exam.method)) {
            return calculateDgnlExamScore(exam, combo, conversionRules);
        }

        if ("VSAT".equals(exam.method)) {
            return calculateVsatExamScore(exam, combo, conversionRules);
        }

        return calculateThptExamScore(exam, combo);
    }

    private BigDecimal calculateDgnlExamScore(ExamScoreData exam, ComboData combo, Map<String, List<ConversionRule>> conversionRules) {
        BigDecimal raw = maxNonNull(exam.n1Thi, exam.n1Cc);
        if (raw == null) {
            return null;
        }
        return convertScore(conversionRules, conversionKey("DGNL", combo.code), raw, 30);
    }

    private BigDecimal calculateVsatExamScore(ExamScoreData exam, ComboData combo, Map<String, List<ConversionRule>> conversionRules) {
        BigDecimal subject1 = convertSubjectScore(exam, combo.mon1, conversionRules);
        BigDecimal subject2 = convertSubjectScore(exam, combo.mon2, conversionRules);
        BigDecimal subject3 = convertSubjectScore(exam, combo.mon3, conversionRules);
        if (subject1 == null || subject2 == null || subject3 == null) {
            return null;
        }
        return calculateWeightedScore(subject1, subject2, subject3, combo);
    }

    private BigDecimal calculateThptExamScore(ExamScoreData exam, ComboData combo) {
        BigDecimal subject1 = rawSubjectScore(exam, combo.mon1);
        BigDecimal subject2 = rawSubjectScore(exam, combo.mon2);
        BigDecimal subject3 = rawSubjectScore(exam, combo.mon3);
        if (subject1 == null || subject2 == null || subject3 == null) {
            return null;
        }
        return calculateWeightedScore(subject1, subject2, subject3, combo);
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

    private BigDecimal convertSubjectScore(ExamScoreData exam, String fieldName, Map<String, List<ConversionRule>> conversionRules) {
        BigDecimal raw = rawSubjectScore(exam, fieldName);
        if (raw == null) {
            return null;
        }
        if (raw.compareTo(BigDecimal.TEN) <= 0) {
            return raw.setScale(5, RoundingMode.HALF_UP);
        }
        return convertScore(conversionRules, conversionKey("VSAT", fieldName), raw, 10);
    }

    private BigDecimal rawSubjectScore(ExamScoreData exam, String fieldName) {
        if (exam == null || fieldName == null || fieldName.isEmpty()) {
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

    private BigDecimal convertScore(Map<String, List<ConversionRule>> conversionRules, String key, BigDecimal rawScore, int defaultScale) {
        if (rawScore == null) {
            return null;
        }
        if (rawScore.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        List<ConversionRule> rules = conversionRules.getOrDefault(key, List.of());
        for (ConversionRule rule : rules) {
            if (rule.contains(rawScore)) {
                BigDecimal converted = rule.interpolate(rawScore);
                return converted == null ? rawScore.setScale(defaultScale, RoundingMode.HALF_UP)
                        : converted.setScale(defaultScale, RoundingMode.HALF_UP);
            }
        }

        return rawScore.setScale(defaultScale, RoundingMode.HALF_UP);
    }

    // endregion

    // region Normalization / helpers

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

    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private static String conversionKey(String method, String value) {
        return normalizeMethod(method) + "|" + normalize(value);
    }

    // endregion

    // region Local model classes

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

    static class PriorityData {
        BigDecimal diemCongMonDatMc;
        BigDecimal diemCongKhongMonDatMc;

        BigDecimal resolveBaseScore() {
            BigDecimal left = diemCongMonDatMc == null ? BigDecimal.ZERO : diemCongMonDatMc;
            BigDecimal right = diemCongKhongMonDatMc == null ? BigDecimal.ZERO : diemCongKhongMonDatMc;
            return left.compareTo(right) >= 0 ? left : right;
        }
    }

    static class ConversionRule {
        BigDecimal fromScore;
        BigDecimal toScore;
        BigDecimal convertedFrom;
        BigDecimal convertedTo;

        boolean contains(BigDecimal rawScore) {
            if (rawScore == null || fromScore == null || toScore == null) {
                return false;
            }
            return rawScore.compareTo(fromScore) >= 0 && rawScore.compareTo(toScore) <= 0;
        }

        BigDecimal interpolate(BigDecimal rawScore) {
            if (rawScore == null || fromScore == null || toScore == null || convertedFrom == null || convertedTo == null) {
                return null;
            }
            if (toScore.compareTo(fromScore) == 0) {
                return convertedTo;
            }
            BigDecimal span = toScore.subtract(fromScore);
            BigDecimal position = rawScore.subtract(fromScore);
            BigDecimal convertedSpan = convertedTo.subtract(convertedFrom);
            return convertedFrom.add(position.multiply(convertedSpan).divide(span, 10, RoundingMode.HALF_UP));
        }
    }

    static class CandidateScore {
        final ComboData combo;
        final BigDecimal examScore;
        final BigDecimal priorityScore;
        final BigDecimal bonusScore;
        final BigDecimal doLechScore;
        final BigDecimal totalScore;

        CandidateScore(ComboData combo, BigDecimal examScore, BigDecimal priorityScore, BigDecimal bonusScore, BigDecimal doLechScore, BigDecimal totalScore) {
            this.combo = combo;
            this.examScore = examScore;
            this.priorityScore = priorityScore;
            this.bonusScore = bonusScore;
            this.doLechScore = doLechScore;
            this.totalScore = totalScore;
        }
    }

    // endregion
}
