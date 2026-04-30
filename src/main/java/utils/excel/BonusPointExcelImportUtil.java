package utils.excel;

import dto.BonusPointDTO;
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

public final class BonusPointExcelImportUtil {
    private BonusPointExcelImportUtil() {
    }

    private static final String[] CCCD_HEADERS = {"cccd", "socccd", "socancuoc", "socancuoccongdan", "cancuoccongdan", "cancuoc"};
    private static final String[] CERT_HEADERS = {"chungchinangoaingu", "chungchi", "chungchiangoai", "chungchingoaingu", "ngoaingu", "phuongthuc"};
    private static final String[] RAW_SCORE_HEADERS = {"diem", "diembacchungchi", "diemgoc", "diemraw"};
    private static final String[] CONVERTED_SCORE_HEADERS = {"diemquydoi"};
    private static final String[] BONUS_SCORE_HEADERS = {"diemcong", "diemcongut", "diemut", "diemuut", "diemuutxetuyen"};
    private static final String[] NOTE_HEADERS = {"ghichu", "note", "ghi chu"};
    private static final int FALLBACK_CCCD_COL = 1;
    private static final int FALLBACK_CERT_COL = 2;
    private static final int FALLBACK_RAW_SCORE_COL = 3;
    private static final int FALLBACK_CONVERTED_SCORE_COL = 4;
    private static final int FALLBACK_BONUS_SCORE_COL = 5;
    private static final int FALLBACK_NOTE_COL = 6;

    public static List<BonusPointDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = null;
            Row headerRow = null;
            Map<String, Integer> headerIndex = null;

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet candidate = workbook.getSheetAt(sheetIndex);
                HeaderMatch match = findHeaderRow(candidate, formatter, evaluator);
                if (match != null) {
                    sheet = candidate;
                    headerRow = match.headerRow;
                    headerIndex = match.headerIndex;
                    break;
                }
            }

            if (sheet == null) {
                return List.of();
            }

            List<BonusPointDTO> results = new ArrayList<>();
            int startRow = headerRow != null ? headerRow.getRowNum() + 1 : findFirstDataRow(sheet, formatter, evaluator);
            for (int rowIndex = Math.max(startRow, 0); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                if (isLikelyHeaderRow(row, formatter, evaluator)) {
                    continue;
                }

                BonusPointDTO dto = new BonusPointDTO();
                dto.setTsCccd(readText(row, formatter, evaluator, headerIndex, CCCD_HEADERS, FALLBACK_CCCD_COL));
                dto.setPhuongThuc(readText(row, formatter, evaluator, headerIndex, CERT_HEADERS, FALLBACK_CERT_COL));
                dto.setDiemCC(readNumber(row, formatter, evaluator, headerIndex, RAW_SCORE_HEADERS, FALLBACK_RAW_SCORE_COL));
                dto.setDiemUtxt(readNumber(row, formatter, evaluator, headerIndex, CONVERTED_SCORE_HEADERS, FALLBACK_CONVERTED_SCORE_COL));
                dto.setDiemTong(readNumber(row, formatter, evaluator, headerIndex, BONUS_SCORE_HEADERS, FALLBACK_BONUS_SCORE_COL));
                dto.setGhiChu(readText(row, formatter, evaluator, headerIndex, NOTE_HEADERS, FALLBACK_NOTE_COL));

                if (isBlank(dto.getTsCccd())) {
                    continue;
                }

                if (isBlank(dto.getPhuongThuc())) {
                    dto.setPhuongThuc(readText(row, formatter, evaluator, null, CERT_HEADERS, FALLBACK_CERT_COL));
                }

                if (isBlank(dto.getPhuongThuc()) && isBlank(dto.getGhiChu()) && dto.getDiemCC() == null && dto.getDiemUtxt() == null && dto.getDiemTong() == null) {
                    continue;
                }

                dto.setDcKeys(dto.getTsCccd().trim() + "|" + safe(dto.getPhuongThuc()) + "|" + safe(dto.getGhiChu()) + "|" + safeNumber(dto.getDiemCC()) + "|" + safeNumber(dto.getDiemUtxt()) + "|" + safeNumber(dto.getDiemTong()));
                results.add(dto);
            }
            return results;
        }
    }

    private static HeaderMatch findHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (sheet == null) {
            return null;
        }
        int first = sheet.getFirstRowNum();
        int last = Math.min(sheet.getLastRowNum(), first + 20);
        for (int rowIndex = first; rowIndex <= last; rowIndex++) {
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

    private static boolean matchesRequiredHeaders(Map<String, Integer> headerIndex) {
        return hasAny(headerIndex, CCCD_HEADERS)
                && (hasAny(headerIndex, CERT_HEADERS)
                || hasAny(headerIndex, RAW_SCORE_HEADERS)
                || hasAny(headerIndex, CONVERTED_SCORE_HEADERS)
                || hasAny(headerIndex, BONUS_SCORE_HEADERS));
    }

    private static boolean hasAny(Map<String, Integer> headerIndex, String[] keys) {
        for (String key : keys) {
            if (headerIndex.containsKey(normalize(key))) {
                return true;
            }
        }
        return false;
    }

    private static int findFirstDataRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int first = sheet.getFirstRowNum();
        int last = Math.min(sheet.getLastRowNum(), first + 30);
        for (int rowIndex = first; rowIndex <= last; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            if (!isLikelyHeaderRow(row, formatter, evaluator)) {
                return rowIndex;
            }
        }
        return first;
    }

    private static boolean isLikelyHeaderRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        int matches = 0;
        for (Cell cell : row) {
            String value = normalize(formatter.formatCellValue(cell, evaluator));
            if (value.equals("cccd") || value.contains("chungchi") || value.contains("diem")) {
                matches++;
            }
        }
        return matches >= 2;
    }

    private static String readText(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        String value = readCellText(row, formatter, evaluator, headerIndex, headerKeys, fallbackColumn);
        return value == null ? "" : value;
    }

    private static Double readNumber(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        String value = readCellText(row, formatter, evaluator, headerIndex, headerKeys, fallbackColumn);
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.').replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String readCellText(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        if (headerIndex != null) {
            Integer col = findColumn(headerIndex, headerKeys);
            if (col != null) {
                Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = safe(formatter.formatCellValue(cell, evaluator));
                if (!isBlank(value)) {
                    return value;
                }
            }
        }

        Cell fallbackCell = row.getCell(fallbackColumn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        String fallbackValue = safe(formatter.formatCellValue(fallbackCell, evaluator));
        return isBlank(fallbackValue) ? "" : fallbackValue;
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String[] keys) {
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

    private static String safeNumber(Double value) {
        if (value == null) {
            return "";
        }
        if (value == Math.rint(value)) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT).replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace('đ', 'd');
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