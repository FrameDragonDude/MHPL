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
                Map<String, BigDecimal> priorityByCccd = loadPriorityScores(session);
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
                        BigDecimal examScore = calculateExamScore(exam, combo, conversionRules);
                        if (examScore == null) {
                            continue;
                        }

                        BigDecimal priorityScore = priorityByCccd.getOrDefault(cccd, BigDecimal.ZERO);
                        BigDecimal bonusScore = bonusByKey.getOrDefault(buildBonusKey(cccd, majorCode, combo.code, exam.method), BigDecimal.ZERO);
                        BigDecimal doLech = combo.doLech == null ? BigDecimal.ZERO : combo.doLech;
                        BigDecimal totalScore = examScore.add(priorityScore).add(bonusScore).add(doLech).setScale(5, RoundingMode.HALF_UP);

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

    private Map<String, BigDecimal> loadPriorityScores(Session session) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select ts_cccd,
                       coalesce(max(diem_cong_mondatmc), coalesce(max(diem_cong_khongmondatmc), 0))
                from xt_uutien_xettuyen
                group by ts_cccd
                """).list();

        Map<String, BigDecimal> results = new HashMap<>();
        for (Object[] row : rows) {
            String cccd = normalize(row[0]);
            if (!cccd.isEmpty()) {
                results.put(cccd, scale(toBigDecimal(row[1]), 5));
            }
        }
        return results;
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
        @SuppressWarnings("unchecked")
        List<Object[]> rows = session.createNativeQuery("""
                select coalesce(nullif(nt.manganh, ''), substring_index(nt.tb_keys, '_', 1)),
                       nt.matohop,
                       coalesce(nt.dolech, 0),
                       coalesce(th.mon1, ''),
                       coalesce(th.mon2, ''),
                       coalesce(th.mon3, '')
                from xt_nganh_tohop nt
                left join xt_tohop_monthi th on th.matohop = nt.matohop
                """).list();

        Map<String, List<ComboData>> results = new HashMap<>();
        for (Object[] row : rows) {
            String majorCode = normalize(row[0]);
            String comboCode = normalize(row[1]);
            if (majorCode.isEmpty() || comboCode.isEmpty()) {
                continue;
            }

            ComboData combo = new ComboData();
            combo.majorCode = majorCode;
            combo.code = comboCode;
            combo.doLech = scale(toBigDecimal(row[2]), 2);
            combo.mon1 = scoreFieldForSubject(toStr(row[3]));
            combo.mon2 = scoreFieldForSubject(toStr(row[4]));
            combo.mon3 = scoreFieldForSubject(toStr(row[5]));

            results.computeIfAbsent(majorCode, key -> new ArrayList<>()).add(combo);
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

    private BigDecimal calculateExamScore(ExamScoreData exam, ComboData combo, Map<String, List<ConversionRule>> conversionRules) {
        if (exam == null || combo == null) {
            return null;
        }

        if ("DGNL".equals(exam.method)) {
            BigDecimal raw = firstNonNull(exam.n1Thi, exam.n1Cc);
            if (raw == null) {
                return null;
            }
            return convertScore(conversionRules, conversionKey("DGNL", combo.code), raw, 30);
        }

        if ("VSAT".equals(exam.method)) {
            BigDecimal subject1 = convertSubjectScore(exam, combo.mon1, conversionRules);
            BigDecimal subject2 = convertSubjectScore(exam, combo.mon2, conversionRules);
            BigDecimal subject3 = convertSubjectScore(exam, combo.mon3, conversionRules);
            if (subject1 == null || subject2 == null || subject3 == null) {
                return null;
            }
            return subject1.add(subject2).add(subject3).setScale(5, RoundingMode.HALF_UP);
        }

        BigDecimal subject1 = rawSubjectScore(exam, combo.mon1);
        BigDecimal subject2 = rawSubjectScore(exam, combo.mon2);
        BigDecimal subject3 = rawSubjectScore(exam, combo.mon3);
        if (subject1 == null || subject2 == null || subject3 == null) {
            return null;
        }
        return subject1.add(subject2).add(subject3).setScale(5, RoundingMode.HALF_UP);
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

    private static String conversionKey(String method, String value) {
        return normalizeMethod(method) + "|" + normalize(value);
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
}
