package utils.excel;

import dto.UuTienXetTuyenDTO;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class UuTienXetTuyenExcelImportUtil {

    public static List<UuTienXetTuyenDTO> importRows(File file) throws Exception {
        List<UuTienXetTuyenDTO> result = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        int headerRow = findHeaderRow(sheet);
        if (headerRow < 0) {
            throw new Exception("Không tìm thấy dòng tiêu đề");
        }

        Row header = sheet.getRow(headerRow);
        int colCccd = findColumn(header, "cccd");
        int colCapQuocGia = findColumn(header, "cấp|cap");
        int colDoiTuyen = findColumn(header, "đội|doi");
        int colMaMon = findColumn(header, "mã môn|ma mon");
        int colLoaiGiai = findColumn(header, "loại giải|loai giai");
        int colDiemMonDat = findColumn(header, "điểm.*môn đặt|diem.*mon dat");
        int colDiemKhongMon = findColumn(header, "thpt.*không|thpt.*khong");
        int colCoChungChi = findColumn(header, "c/c|chứng chỉ");

        if (colCccd < 0) {
            throw new Exception("Không tìm thấy cột CCCD");
        }

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String cccd = getCellValue(row, colCccd, formatter, evaluator);
            if (cccd == null || cccd.trim().isEmpty()) continue;

            UuTienXetTuyenDTO dto = new UuTienXetTuyenDTO();
            dto.setTsCccd(cccd.trim());
            dto.setCapQuocGia(colCapQuocGia >= 0 ? getCellValue(row, colCapQuocGia, formatter, evaluator) : null);
            dto.setDoiTuyen(colDoiTuyen >= 0 ? getCellValue(row, colDoiTuyen, formatter, evaluator) : null);
            dto.setMaMon(colMaMon >= 0 ? getCellValue(row, colMaMon, formatter, evaluator) : null);
            dto.setLoaiGiai(colLoaiGiai >= 0 ? getCellValue(row, colLoaiGiai, formatter, evaluator) : null);
            dto.setDiemCongMonDatMc(colDiemMonDat >= 0 ? parseDecimal(getCellValue(row, colDiemMonDat, formatter, evaluator)) : null);
            dto.setDiemCongKhongMonDatMc(colDiemKhongMon >= 0 ? parseDecimal(getCellValue(row, colDiemKhongMon, formatter, evaluator)) : null);
            dto.setCoChungChi(colCoChungChi >= 0 ? getCellValue(row, colCoChungChi, formatter, evaluator) : null);

            String key = buildKey(dto);
            dto.setUtxtKeys(key);

            result.add(dto);
        }

        workbook.close();
        return result;
    }

    private static int findHeaderRow(Sheet sheet) {
        for (int i = 0; i < Math.min(20, sheet.getLastRowNum() + 1); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            if (isLikelyHeaderRow(row)) return i;
        }
        return -1;
    }

    private static boolean isLikelyHeaderRow(Row row) {
        DataFormatter fmt = new DataFormatter();
        boolean hasCccd = false;
        boolean hasOther = false;
        for (Cell cell : row) {
            String val = fmt.formatCellValue(cell).toLowerCase();
            if (val.contains("cccd")) hasCccd = true;
            if (val.contains("cấp") || val.contains("cap") || val.contains("môn") || val.contains("mon") || 
                val.contains("giải") || val.contains("giai") || val.contains("chứng") || val.contains("chung")) {
                hasOther = true;
            }
        }
        return hasCccd && hasOther;
    }

    private static int findColumn(Row header, String pattern) {
        if (header == null) return -1;
        String normalizedPattern = normalize(pattern);
        DataFormatter fmt = new DataFormatter();
        for (Cell cell : header) {
            String raw = fmt.formatCellValue(cell);
            String val = normalize(raw);
            if (val.matches(".*" + normalizedPattern.replace("|", ".*|.*") + ".*")) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private static String getCellValue(Row row, int col, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (col < 0) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return formatter.formatCellValue(cell, evaluator);
        } catch (Exception ex) {
            try {
                return formatter.formatCellValue(cell);
            } catch (Exception ex2) {
                return null;
            }
        }
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            value = value.replaceAll("[^0-9.-]", "");
            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildKey(UuTienXetTuyenDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getTsCccd() != null) sb.append(dto.getTsCccd()).append("_");
        if (dto.getLoaiGiai() != null) sb.append(dto.getLoaiGiai()).append("_");
        if (dto.getMaMon() != null) sb.append(dto.getMaMon());
        
        String key = sb.toString().replaceAll("_$", "");
        if (key.length() > 100) {
            return "utxt_" + hashString(key);
        }
        return key.isEmpty() ? "utxt_empty_" + System.nanoTime() : key;
    }

    private static String hashString(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().substring(0, 32);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9|]", "");
    }
}
