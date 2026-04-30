package utils.excel;

import dto.MajorCombinationDTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class MajorCombinationExcelImportUtil {
    private MajorCombinationExcelImportUtil() {
    }

    private static final Map<String, String> SUBJECT_CODE_MAP = new java.util.LinkedHashMap<>();
    static {
        // New subject codes from Candidate sheet
        SUBJECT_CODE_MAP.put("TI", "Tin học");
        SUBJECT_CODE_MAP.put("KTPL", "Kinh tế Pháp luật");
        SUBJECT_CODE_MAP.put("CNCN", "Công nghệ công nghiệp");
        SUBJECT_CODE_MAP.put("CNNN", "Công nghệ nông nghiệp");
        SUBJECT_CODE_MAP.put("NK", "Năng khiếu");
        SUBJECT_CODE_MAP.put("N", "Tiếng Anh");
        // Legacy subject codes (for backward compatibility)
        SUBJECT_CODE_MAP.put("TO", "Toán");
        SUBJECT_CODE_MAP.put("VA", "Văn");
        SUBJECT_CODE_MAP.put("SI", "Sinh");
        SUBJECT_CODE_MAP.put("LI", "Lý");
        SUBJECT_CODE_MAP.put("HO", "Hóa");
        SUBJECT_CODE_MAP.put("AN", "Anh");
        SUBJECT_CODE_MAP.put("SU", "Sử");
        SUBJECT_CODE_MAP.put("DI", "Địa");
        SUBJECT_CODE_MAP.put("GD", "GDCD");
        SUBJECT_CODE_MAP.put("CN", "Công nghệ");
    }

    public static List<MajorCombinationDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return List.of();
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return List.of();
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter, evaluator);
            List<MajorCombinationDTO> results = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                MajorCombinationDTO dto = new MajorCombinationDTO();
                dto.setManganh(text(row, headerIndex, formatter, evaluator, "manganh", "ma_nganh"));
                dto.setTenNganhChuan(text(row, headerIndex, formatter, evaluator,
                        "tennganhchuan", "ten_nganhchuan", "tennganh", "ten_nganh"));
                dto.setMaToHop(codeValue(text(row, headerIndex, formatter, evaluator,
                    "matohop", "ma_to_hop", "ma_tohop")));
                dto.setTbKeys(text(row, headerIndex, formatter, evaluator,
                        "tbkeys", "tb_keys"));
                String parsedSubjects = text(row, headerIndex, formatter, evaluator,
                    "matohop", "ma_to_hop", "ma_tohop");
                fillSubjects(dto, parsedSubjects);
                dto.setTenToHop(subjectNames(dto));
                dto.setGoc(text(row, headerIndex, formatter, evaluator,
                        "goc", "tohopgoc", "to_hop_goc"));
                dto.setDoLech(number(row, headerIndex, formatter, evaluator,
                        "dolech", "do_lech", "dolech"));

                if (isBlank(dto.getMaToHop()) || (isBlank(dto.getTenNganhChuan()) && isBlank(dto.getManganh()))) {
                    continue;
                }

                if (isBlank(dto.getTbKeys()) && !isBlank(dto.getManganh())) {
                    dto.setTbKeys(dto.getManganh().trim() + "_" + dto.getMaToHop().trim());
                }

                if (isBlank(dto.getTenToHop())) {
                    dto.setTenToHop(subjectNames(dto));
                }

                results.add(dto);
            }

            return results;
        }
    }

    private static Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String value = formatter.formatCellValue(cell, evaluator);
            if (isBlank(value)) {
                continue;
            }
            map.put(normalize(value), cell.getColumnIndex());
        }
        return map;
    }

    private static String text(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, FormulaEvaluator evaluator, String... keys) {
        Integer col = findColumn(headerIndex, keys);
        if (col == null) {
            return "";
        }
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return safe(formatter.formatCellValue(cell, evaluator));
    }

    private static Double number(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, FormulaEvaluator evaluator, String... keys) {
        String value = text(row, headerIndex, formatter, evaluator, keys);
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String... keys) {
        for (String key : keys) {
            Integer col = headerIndex.get(normalize(key));
            if (col != null) {
                return col;
            }
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT);
        lower = lower.replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }

    private static String codeValue(String value) {
        String trimmed = safe(value);
        if (trimmed.isEmpty()) {
            return "";
        }
        int idx = trimmed.indexOf('(');
        if (idx > 0) {
            return trimmed.substring(0, idx).trim();
        }
        return trimmed;
    }

    private static void fillSubjects(MajorCombinationDTO dto, String rawMaToHop) {
        if (isBlank(rawMaToHop)) {
            return;
        }
        String inside = extractInsideParentheses(rawMaToHop);
        if (isBlank(inside)) {
            return;
        }

        String cleaned = inside.replaceAll("[0-9-]", " ");
        String[] tokens = cleaned.split("[^A-Za-z]+|");
        List<String> codes = new ArrayList<>();
        for (String token : tokens) {
            if (!isBlank(token)) {
                codes.add(token.trim().toUpperCase(Locale.ROOT));
            }
        }

        if (codes.size() > 0 && isBlank(dto.getMon1())) {
            dto.setMon1(mapSubject(codes.get(0)));
        }
        if (codes.size() > 1 && isBlank(dto.getMon2())) {
            dto.setMon2(mapSubject(codes.get(1)));
        }
        if (codes.size() > 2 && isBlank(dto.getMon3())) {
            dto.setMon3(mapSubject(codes.get(2)));
        }
    }

    private static String extractInsideParentheses(String value) {
        int open = value.indexOf('(');
        if (open < 0) {
            return "";
        }
        int close = value.indexOf(')', open + 1);
        if (close < 0) {
            return value.substring(open + 1);
        }
        return value.substring(open + 1, close);
    }

    private static String mapSubject(String code) {
        if (isBlank(code)) {
            return "";
        }
        return SUBJECT_CODE_MAP.getOrDefault(code.trim().toUpperCase(Locale.ROOT), prettyFromCode(code));
    }

    private static String subjectNames(MajorCombinationDTO dto) {
        List<String> names = new ArrayList<>();
        if (!isBlank(dto.getMon1())) names.add(dto.getMon1());
        if (!isBlank(dto.getMon2())) names.add(dto.getMon2());
        if (!isBlank(dto.getMon3())) names.add(dto.getMon3());
        return String.join(", ", names);
    }

    private static String prettyFromCode(String code) {
        if (isBlank(code)) {
            return "";
        }
        String lower = code.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + (lower.length() > 1 ? lower.substring(1) : "");
    }
}
