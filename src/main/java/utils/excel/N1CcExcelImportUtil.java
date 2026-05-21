package utils.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class N1CcExcelImportUtil {

    private N1CcExcelImportUtil() {}

    /**
     * Đọc file Excel và trả về map: cccd -> diemQuyDoi (Double)
     */
    public static Map<String, Double> importN1Cc(File file) throws IOException {
        Map<String, Double> result = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return result;

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) return result;

            Map<String, Integer> headerIndex = new HashMap<>();
            for (Cell cell : header) {
                String v = formatter.formatCellValue(cell, evaluator);
                if (v == null || v.trim().isEmpty()) continue;
                headerIndex.put(normalize(v), cell.getColumnIndex());
            }

            Integer cccdCol = findColumn(headerIndex, new String[]{"cccd", "cccd"});
            Integer diemCol = findColumn(headerIndex, new String[]{"diemquydoi", "diem_quy_doi", "điểm quy đổi", "diem quy doi", "diemquydoi"});

            if (cccdCol == null || diemCol == null) {
                // try looser matches
                if (cccdCol == null) cccdCol = findColumn(headerIndex, new String[]{"cccd", "căn cccd", "id"});
                if (diemCol == null) diemCol = findColumn(headerIndex, new String[]{"diem", "diem_quy_doi", "diemquydoi", "diem quy doi", "diem_quydoi"});
            }

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String cccd = "";
                if (cccdCol != null) {
                    Cell c = row.getCell(cccdCol, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    cccd = safe(formatter.formatCellValue(c, evaluator));
                }
                if (cccd.isEmpty()) continue;

                Double diem = null;
                if (diemCol != null) {
                    Cell d = row.getCell(diemCol, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String s = safe(formatter.formatCellValue(d, evaluator));
                    if (!s.isEmpty()) {
                        try { diem = Double.parseDouble(s.replace(',', '.')); } catch (Exception ex) { diem = null; }
                    }
                }
                if (diem != null) result.put(cccd.trim(), diem);
            }
        }
        return result;
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String[] keys) {
        for (String k : keys) {
            Integer c = headerIndex.get(normalize(k));
            if (c != null) return c;
        }
        return null;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static String normalize(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.ROOT);
        lower = lower.replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }
}
