package utils.excel;

import dto.SubjectCombinationDTO;
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

public final class SubjectCombinationExcelImportUtil {
    private SubjectCombinationExcelImportUtil() {
    }

    private static final java.util.Map<String, String> SUBJECT_CODE_MAP = new java.util.LinkedHashMap<>();
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

    public static List<SubjectCombinationDTO> importRows(File file) throws IOException {
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
            List<SubjectCombinationDTO> results = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                SubjectCombinationDTO dto = new SubjectCombinationDTO();
                String rawMa = text(row, headerIndex, formatter, evaluator, "ma_to_hop", "matohop", "ma to hop");
                dto.setMaToHop(rawMa);
                dto.setMon1(text(row, headerIndex, formatter, evaluator, "mon_1", "mon1", "môn 1", "mon 1"));
                dto.setMon2(text(row, headerIndex, formatter, evaluator, "mon_2", "mon2", "môn 2", "mon 2"));
                dto.setMon3(text(row, headerIndex, formatter, evaluator, "mon_3", "mon3", "môn 3", "mon 3"));
                dto.setTenToHop(text(row, headerIndex, formatter, evaluator, "ten_to_hop", "tentohop", "tên tổ hợp", "ten to hop"));

                // If subject columns are empty but MA contains parenthetical subject codes, parse them
                if ((isBlank(dto.getMon1()) || isBlank(dto.getMon2()) || isBlank(dto.getMon3())) && !isBlank(dto.getMaToHop())) {
                    parseAndFillSubjects(dto);
                }

                // Require at least a code and one subject/name to accept the row
                if (isBlank(dto.getMaToHop())) {
                    continue;
                }
                if (isBlank(dto.getMon1()) && isBlank(dto.getMon2()) && isBlank(dto.getMon3()) && isBlank(dto.getTenToHop())) {
                    continue;
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

    private static void parseAndFillSubjects(SubjectCombinationDTO dto) {
        String raw = dto.getMaToHop();
        if (isBlank(raw)) {
            return;
        }

        // extract code before parentheses
        String code = raw.split("\\(")[0].trim();
        dto.setMaToHop(code);

        String inside = "";
        int p = raw.indexOf('(');
        if (p >= 0) {
            int q = raw.indexOf(')', p + 1);
            if (q > p) {
                inside = raw.substring(p + 1, q);
            } else {
                inside = raw.substring(p + 1);
            }
        }

        // remove digits and hyphens, keep letters and separators
        inside = inside.replaceAll("[0-9-]", " ");
        // split on any non-letter sequence
        String[] tokens = inside.split("[^A-Za-z]+");
        java.util.List<String> codes = new java.util.ArrayList<>();
        for (String t : tokens) {
            if (!isBlank(t)) {
                codes.add(t.trim().toUpperCase(Locale.ROOT));
            }
        }

        String name1 = "", name2 = "", name3 = "";
        if (codes.size() > 0) {
            name1 = SUBJECT_CODE_MAP.getOrDefault(codes.get(0), prettyFromCode(codes.get(0)));
        }
        if (codes.size() > 1) {
            name2 = SUBJECT_CODE_MAP.getOrDefault(codes.get(1), prettyFromCode(codes.get(1)));
        }
        if (codes.size() > 2) {
            name3 = SUBJECT_CODE_MAP.getOrDefault(codes.get(2), prettyFromCode(codes.get(2)));
        }

        if (!isBlank(name1) && isBlank(dto.getMon1())) {
            dto.setMon1(name1);
        }
        if (!isBlank(name2) && isBlank(dto.getMon2())) {
            dto.setMon2(name2);
        }
        if (!isBlank(name3) && isBlank(dto.getMon3())) {
            dto.setMon3(name3);
        }

        if (isBlank(dto.getTenToHop())) {
            java.util.List<String> names = new java.util.ArrayList<>();
            if (!isBlank(name1)) names.add(name1);
            if (!isBlank(name2)) names.add(name2);
            if (!isBlank(name3)) names.add(name3);
            dto.setTenToHop(String.join(", ", names));
        }
    }

    private static String prettyFromCode(String code) {
        if (isBlank(code)) return "";
        String lower = code.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + (lower.length() > 1 ? lower.substring(1) : "");
    }
}
