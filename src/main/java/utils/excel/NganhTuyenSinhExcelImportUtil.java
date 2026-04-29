package utils.excel;

import dto.NganhTuyenSinhDTO;
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

public final class NganhTuyenSinhExcelImportUtil {
    private NganhTuyenSinhExcelImportUtil() {}

    public static List<NganhTuyenSinhDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return List.of();

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return List.of();

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter, evaluator);
            List<NganhTuyenSinhDTO> results = new ArrayList<>();

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                NganhTuyenSinhDTO dto = new NganhTuyenSinhDTO();
                dto.setMaXetTuyen(text(row, headerIndex, formatter, evaluator, "ma_xet_tuyen", "manganh", "ma xet tuyen", "mã xét tuyển", "mã"));

                String tenNganh = text(row, headerIndex, formatter, evaluator,
                        "ten_nganh", "tennganh", "ten_nganh_chuan", "tennganhchuan", "ten_nganh_chuong_trinh",
                        "tên_ngành", "tên ngành", "ten nganh", "ten nganh, chuong trinh dao tao", "ten nganh chuong trinh dao tao",
                        "tennganhchuongtrinhdaotao", "ten_nganh_chuong_trinh_dao_tao");
                String chuongTrinh = text(row, headerIndex, formatter, evaluator,
                        "chuong_trinh", "chuongtrinh", "chuong_trinh_dao_tao", "chuongtrinhdaotao",
                        "chuong trinh", "chương trình", "chương trình đào tạo", "chuong trinh dao tao");

                // Some files store the ngành name and program in a single combined column.
                // If one side is missing, reuse the available text so the UI is not blank.
                if (isBlank(tenNganh) && !isBlank(chuongTrinh)) {
                    tenNganh = chuongTrinh;
                } else if (isBlank(chuongTrinh) && !isBlank(tenNganh)) {
                    chuongTrinh = tenNganh;
                }

                dto.setTenNganh(tenNganh);
                dto.setChuongTrinh(chuongTrinh);
                dto.setNguongDauVao(text(row, headerIndex, formatter, evaluator, "nguong_dau_vao", "nguongdauvao", "ngưỡng", "n_diemsan", "ngưỡng đầu vào", "nguong"));
                String ct = text(row, headerIndex, formatter, evaluator, "chi_tieu_chot", "chitieu", "n_chitieu", "chỉ tiêu chốt");
                try { dto.setChiTieuChot(ct == null || ct.isBlank() ? null : Integer.parseInt(ct)); } catch (Exception ex) { dto.setChiTieuChot(null); }

                if (isBlank(dto.getMaXetTuyen()) && isBlank(dto.getTenNganh())) continue;
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
        return safe(formatter.formatCellValue(cell, evaluator));
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String... keys) {
        for (String k : keys) {
            Integer c = headerIndex.get(normalize(k));
            if (c != null) return c;
        }
        return null;
    }

    private static String safe(String v) { return v == null ? "" : v.trim(); }
    private static boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT);
        lower = lower.replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }
}
