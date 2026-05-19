package utils.excel;

import dto.EnglishConversionDTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class EnglishConversionExcelImportUtil {

    private EnglishConversionExcelImportUtil() {}

    public static List<EnglishConversionDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return List.of();

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return List.of();

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter, evaluator);
            List<EnglishConversionDTO> results = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                EnglishConversionDTO dto = new EnglishConversionDTO();
                dto.setCccd(text(row, headerIndex, formatter, evaluator, "cccd"));
                dto.setChungChi(text(row, headerIndex, formatter, evaluator, "chungchingoaingu", "chungchingaingu", "chungchi"));
                dto.setDiemGoc(text(row, headerIndex, formatter, evaluator, "diembacchungchi", "diemgoc"));
                
                Double qdNum = number(row, headerIndex, formatter, evaluator, "diemquydoi");
                dto.setDiemQuydoi(qdNum != null ? BigDecimal.valueOf(qdNum) : BigDecimal.ZERO);
                
                Double congNum = number(row, headerIndex, formatter, evaluator, "diemcong");
                dto.setDiemCong(congNum != null ? BigDecimal.valueOf(congNum) : BigDecimal.ZERO);

                if (isBlank(dto.getCccd())) {
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
            if (isBlank(value)) continue;
            map.put(normalize(value), cell.getColumnIndex());
        }
        return map;
    }

    private static String text(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, FormulaEvaluator evaluator, String... keys) {
        Integer col = findColumn(headerIndex, keys);
        if (col == null) return "";
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }

    private static Double number(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, FormulaEvaluator evaluator, String... keys) {
        String value = text(row, headerIndex, formatter, evaluator, keys);
        if (isBlank(value)) return null;
        try { return Double.parseDouble(value.replace(',', '.')); } catch (NumberFormatException ex) { return null; }
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String... keys) {
        for (String key : keys) {
            Integer col = headerIndex.get(normalize(key));
            if (col != null) return col;
        }
        return null;
    }

    private static boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    private static String normalize(String value) {
        if (value == null) return "";
        String lower = value.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]", "");
    }
}