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
        int colCccd = findColumnByKeywords(header,
            new String[]{"cccd"},
            new String[]{});

        int colCapQuocGia = findColumnByKeywords(header,
            new String[]{"capquocgia", "cap", "quocgia"},
            new String[]{});

        int colDoiTuyen = findColumnByKeywords(header,
            new String[]{"doituyen", "dt"},
            new String[]{"diem", "mon", "giai"});

        int colMaMon = findColumnByKeywords(header,
            new String[]{"mamon", "mamonhoc"},
            new String[]{"diem"});

        int colLoaiGiai = findColumnByKeywords(header,
            new String[]{"loaigiai"},
            new String[]{});

        int colDiemMonDat = findColumnByKeywords(header,
            new String[]{
                "diemcongchomondatgiai",
                "diemcongmondatgiai",
                "diemmondatgiai"
            },
            new String[]{"khong", "ko", "thxt", "thpt"});

        int colDiemKhongMon = findColumnByKeywords(header,
            new String[]{
                "diemcongchothxtkocomondatgiai",
                "diemcongchothxtkhongcomondatgiai",
                "diemcongchothptkocomondatgiai",
                "diemcongchothptkhongcomondatgiai",
                "khongcomondatgiai",
                "kocomondatgiai"
            },
            new String[]{});

        int colCoChungChi = findColumnByKeywords(header,
            new String[]{"coc", "cochungchi", "chungchi"},
            new String[]{});

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

    private static int findColumnByKeywords(Row header, String[] includeKeywords, String[] excludeKeywords) {
        if (header == null) return -1;

        DataFormatter fmt = new DataFormatter();
        int bestCol = -1;
        int bestScore = -1;

        for (Cell cell : header) {
            String raw = fmt.formatCellValue(cell);
            String val = normalize(raw);

            if (val.isEmpty()) {
                continue;
            }

            boolean excluded = false;
            for (String keyword : excludeKeywords) {
                String key = normalize(keyword);
                if (!key.isEmpty() && containsKeyword(val, key)) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) {
                continue;
            }

            int score = 0;
            for (String keyword : includeKeywords) {
                String key = normalize(keyword);
                if (!key.isEmpty() && containsKeyword(val, key)) {
                    score += 10 + key.length();
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestCol = cell.getColumnIndex();
            }
        }

        return bestScore > 0 ? bestCol : -1;
    }

    private static boolean containsKeyword(String value, String keyword) {
        if (keyword.length() <= 2) {
            return value.equals(keyword);
        }
        return value.contains(keyword);
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
            String raw = value.trim().replace(" ", "");

            int lastComma = raw.lastIndexOf(',');
            int lastDot = raw.lastIndexOf('.');

            if (lastComma >= 0 && lastDot >= 0) {
                // If both separators exist, keep the right-most separator as decimal point.
                if (lastComma > lastDot) {
                    raw = raw.replace(".", "");
                    raw = raw.replace(',', '.');
                } else {
                    raw = raw.replace(",", "");
                }
            } else if (lastComma >= 0) {
                raw = raw.replace(',', '.');
            }

            raw = raw.replaceAll("[^0-9.-]", "");
            if (raw.isEmpty() || raw.equals("-") || raw.equals(".")) {
                return null;
            }

            return new BigDecimal(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildKey(UuTienXetTuyenDTO dto) {
        StringBuilder sb = new StringBuilder();
        appendKeyPart(sb, dto.getTsCccd());
        appendKeyPart(sb, dto.getCapQuocGia());
        appendKeyPart(sb, dto.getDoiTuyen());
        appendKeyPart(sb, dto.getMaMon());
        appendKeyPart(sb, dto.getLoaiGiai());
        appendKeyPart(sb, dto.getCoChungChi());

        String key = sb.toString().replaceAll("_$", "");
        if (key.length() > 100) {
            return "utxt_" + hashString(key);
        }
        return key.isEmpty() ? "utxt_empty_" + System.nanoTime() : key;
    }

    private static void appendKeyPart(StringBuilder sb, String value) {
        String part = normalizeKeyPart(value);
        if (!part.isEmpty()) {
            sb.append(part).append("_");
        }
    }

    private static String normalizeKeyPart(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]", "");
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
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]", "");
    }
}
