package utils.excel;

import dto.CandidateDTO;
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

public final class CandidateExcelImportUtil {

    private CandidateExcelImportUtil() {
    }

    public static List<CandidateDTO> importCandidates(File file) throws IOException {
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
            List<CandidateDTO> results = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String cccd = text(row, headerIndex, formatter, evaluator, "cccd");
                String fullName = text(row, headerIndex, formatter, evaluator, "ho_va_ten", "hovaten", "hoten", "ho ten", "họ và tên", "họ tên");
                if (cccd.isEmpty() && fullName.isEmpty()) {
                    continue;
                }

                CandidateDTO dto = new CandidateDTO();
                dto.setCccd(cccd);
                dto.setSoBaoDanh(text(row, headerIndex, formatter, evaluator, "sobaodanh", "so_bao_danh", "sbd", "số báo danh"));

                String ho = text(row, headerIndex, formatter, evaluator, "ho", "họ");
                String ten = text(row, headerIndex, formatter, evaluator, "ten", "tên");
                if (!fullName.isEmpty() && (ho.isEmpty() || ten.isEmpty())) {
                    String[] split = splitFullName(fullName);
                    if (ho.isEmpty()) {
                        ho = split[0];
                    }
                    if (ten.isEmpty()) {
                        ten = split[1];
                    }
                }
                dto.setHo(ho);
                dto.setTen(ten);
                dto.setNgaySinh(text(row, headerIndex, formatter, evaluator, "ngay_sinh", "ngaysinh", "ngày sinh", "ngay sinh"));
                dto.setGioiTinh(text(row, headerIndex, formatter, evaluator, "gioi_tinh", "gioitinh", "giới tính", "gioi tinh"));
                dto.setDienThoai(text(
                        row,
                        headerIndex,
                        formatter,
                        evaluator,
                        "dien_thoai",
                        "dien thoai",
                        "điện thoại",
                        "dienthoai",
                        "so_dien_thoai",
                        "so dien thoai",
                        "sodienthoai",
                        "sdt",
                        "dtdt",
                        "đtđt",
                        "số điện thoại"
                ));
                dto.setEmail(text(row, headerIndex, formatter, evaluator, "email"));
                dto.setNoiSinh(text(row, headerIndex, formatter, evaluator, "noi_sinh", "noisinh", "nơi sinh", "noi sinh"));
                dto.setDoiTuong(text(row, headerIndex, formatter, evaluator, "doi_tuong", "doituong", "dtut", "đtut"));
                dto.setKhuVuc(text(row, headerIndex, formatter, evaluator, "khu_vuc", "khuvuc", "kvut", "kv"));

                dto.setDiemTo(number(row, headerIndex, formatter, evaluator, "to", "toan"));
                dto.setDiemVa(number(row, headerIndex, formatter, evaluator, "va", "van"));
                dto.setDiemLi(number(row, headerIndex, formatter, evaluator, "li", "ly"));
                dto.setDiemHo(number(row, headerIndex, formatter, evaluator, "ho", "hoa"));
                dto.setDiemSi(number(row, headerIndex, formatter, evaluator, "si", "sinh"));
                dto.setDiemSu(number(row, headerIndex, formatter, evaluator, "su", "lich_su", "lich su"));
                dto.setDiemDi(number(row, headerIndex, formatter, evaluator, "di", "dia", "dia_ly", "dia ly"));
                dto.setDiemGdcd(number(row, headerIndex, formatter, evaluator, "gdcd"));
                dto.setDiemNn(number(row, headerIndex, formatter, evaluator, "nn", "ngoai_ngu", "ngoai ngu"));
                dto.setMaMonNn(text(row, headerIndex, formatter, evaluator, "ma_mon_nn", "mamonnn", "ma mon nn"));
                dto.setDiemKtpl(number(row, headerIndex, formatter, evaluator, "ktpl"));
                dto.setDiemTi(number(row, headerIndex, formatter, evaluator, "ti", "tin"));
                dto.setDiemCncn(number(row, headerIndex, formatter, evaluator, "cncn"));
                dto.setDiemCnnn(number(row, headerIndex, formatter, evaluator, "cnnn"));
                dto.setChuongTrinh(text(row, headerIndex, formatter, evaluator, "chuong_trinh", "chuong trinh"));
                dto.setDiemNk1(number(row, headerIndex, formatter, evaluator, "nk1"));
                dto.setDiemNk2(number(row, headerIndex, formatter, evaluator, "nk2"));
                dto.setDiemNk3(number(row, headerIndex, formatter, evaluator, "nk3"));
                dto.setDiemNk4(number(row, headerIndex, formatter, evaluator, "nk4"));
                dto.setDiemNk5(number(row, headerIndex, formatter, evaluator, "nk5"));
                dto.setDiemNk6(number(row, headerIndex, formatter, evaluator, "nk6"));
                dto.setDiemNk7(number(row, headerIndex, formatter, evaluator, "nk7"));
                dto.setDiemNk8(number(row, headerIndex, formatter, evaluator, "nk8"));
                dto.setDiemNk9(number(row, headerIndex, formatter, evaluator, "nk9"));
                dto.setDiemNk10(number(row, headerIndex, formatter, evaluator, "nk10"));
                dto.setDiemXetTotNghiep(number(row, headerIndex, formatter, evaluator, "diem_xet_tot_nghiep", "diem xet tot nghiep", "dxtn"));
                dto.setDanToc(text(row, headerIndex, formatter, evaluator, "dan_toc", "dantoc"));
                dto.setMaDanToc(text(row, headerIndex, formatter, evaluator, "ma_dan_toc", "madan toc", "madt"));

                results.add(dto);
            }

            return results;
        }
    }

    private static Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String value = formatter.formatCellValue(cell, evaluator);
            if (value == null || value.trim().isEmpty()) {
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
        if (value.isEmpty()) {
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

    private static String[] splitFullName(String fullName) {
        String normalized = safe(fullName).replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return new String[]{"", ""};
        }
        int lastSpace = normalized.lastIndexOf(' ');
        if (lastSpace < 0) {
            return new String[]{"", normalized};
        }
        return new String[]{normalized.substring(0, lastSpace).trim(), normalized.substring(lastSpace + 1).trim()};
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT);
        lower = lower.replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }
}
