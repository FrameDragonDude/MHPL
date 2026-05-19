package utils.excel;

import dto.PriorityAdmissionDTO;
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

public final class PriorityAdmissionExcelImportUtil {

    private PriorityAdmissionExcelImportUtil() {}

    public static List<PriorityAdmissionDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            
            // Cố gắng lấy đúng sheet danh sách thí sinh đạt giải học sinh giỏi
            Sheet sheet = workbook.getSheet("ds thi sinh");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // Cứu cánh: lấy sheet đầu tiên nếu đổi tên
            }
            if (sheet == null) return List.of();

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return List.of();

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter, evaluator);
            List<PriorityAdmissionDTO> results = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                PriorityAdmissionDTO dto = new PriorityAdmissionDTO();
                dto.setCccd(text(row, headerIndex, formatter, evaluator, "cccd"));
                dto.setCap(text(row, headerIndex, formatter, evaluator, "cap"));
                dto.setDoiTuong(text(row, headerIndex, formatter, evaluator, "dt", "doituong", "đt"));

                String maMon = text(row, headerIndex, formatter, evaluator, "mamon");
                dto.setMaMon(maMon.toUpperCase().trim());
                
                dto.setLoaiGiai(text(row, headerIndex, formatter, evaluator, "loaigiai"));

                Double trungNum = number(row, headerIndex, formatter, evaluator, "diemcongchomondatgiai", "diemcongtrungmon");
                dto.setDiemCongTrungMon(trungNum != null ? BigDecimal.valueOf(trungNum) : BigDecimal.ZERO);

                Double khacNum = number(row, headerIndex, formatter, evaluator, "diemcongchothxtkocomondatgiai", "diemcongkhacmon");
                dto.setDiemCongKhacMon(khacNum != null ? BigDecimal.valueOf(khacNum) : BigDecimal.ZERO);

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