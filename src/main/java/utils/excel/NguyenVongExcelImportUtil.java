package utils.excel;

import dto.NguyenVongDTO;
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

public final class NguyenVongExcelImportUtil {
    private NguyenVongExcelImportUtil() {
    }

    private static final String[] REQUIRED_HEADER_KEYS = {
        "cccd", "thutunv", "matruong", "tentruong", "maxettuyen", "tenmaxettuyen"
    };

    public static List<NguyenVongDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = null;
            Row headerRow = null;
            Map<String, Integer> headerIndex = null;

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet candidate = workbook.getSheetAt(sheetIndex);
                if (candidate == null) {
                    continue;
                }
                HeaderMatch match = findHeaderRow(candidate, formatter, evaluator);
                if (match != null) {
                    sheet = candidate;
                    headerRow = match.headerRow;
                    headerIndex = match.headerIndex;
                    break;
                }
            }

            if (sheet == null || headerRow == null || headerIndex == null) {
                return List.of();
            }

            List<NguyenVongDTO> results = new ArrayList<>();

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                NguyenVongDTO dto = new NguyenVongDTO();
                dto.setCccd(text(row, headerIndex, formatter, evaluator, "cccd", "so_cccd", "sothecandidbox", "so_the_cccd"));
                dto.setThuTuNV(number(row, headerIndex, formatter, evaluator, "thutunv", "thu_tu_nv", "thutunguyen", "thu tu nv").intValue());
                dto.setMaTruong(text(row, headerIndex, formatter, evaluator, "matruong", "ma_truong", "mã trường", "ma truong"));
                dto.setTenTruong(text(row, headerIndex, formatter, evaluator, "tentruong", "ten_truong", "tên trường", "ten truong"));
                dto.setMaXetTuyen(text(row, headerIndex, formatter, evaluator, "maxettuyen", "ma_xet_tuyen", "mã xét tuyển", "ma xet tuyen"));
                dto.setTenMaXetTuyen(text(row, headerIndex, formatter, evaluator, "tenmaxettuyen", "ten_ma_xet_tuyen", "tên mã xét tuyển", "ten ma xet tuyen"));
                dto.setNguyenVongThang(text(row, headerIndex, formatter, evaluator,
                        "nguyenvongtuyenthangdieu8",
                        "nguyenvongtuyenthang",
                        "nguyen_vong_tuyen_thang_dieu_8",
                        "nguyen vong tuyen thang dieu 8",
                        "nguyen vong tuyen thang(dieu 8)"));

                if (isBlank(dto.getCccd()) || isBlank(dto.getMaTruong()) || isBlank(dto.getMaXetTuyen())) {
                    continue;
                }

                results.add(dto);
            }

            return results;
        }
    }

    private static HeaderMatch findHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int last = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + 15);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= last; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(row, formatter, evaluator);
            if (matchesRequiredHeaders(headerIndex)) {
                return new HeaderMatch(row, headerIndex);
            }
        }
        return null;
    }

    private static boolean matchesRequiredHeaders(Map<String, Integer> headerIndex) {
        for (String key : REQUIRED_HEADER_KEYS) {
            if (headerIndex.get(normalize(key)) == null) {
                return false;
            }
        }
        return true;
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
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0.0;
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

    private static final class HeaderMatch {
        private final Row headerRow;
        private final Map<String, Integer> headerIndex;

        private HeaderMatch(Row headerRow, Map<String, Integer> headerIndex) {
            this.headerRow = headerRow;
            this.headerIndex = headerIndex;
        }
    }
}
