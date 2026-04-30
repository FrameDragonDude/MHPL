package utils.excel;

import dto.ExamScoreDTO;
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

public final class ExamScoresExcelImportUtil {

    private ExamScoresExcelImportUtil() {}

    public static List<ExamScoreDTO> importExamScores(File file) throws IOException {

        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return List.of();

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return List.of();

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter, evaluator);

            List<ExamScoreDTO> results = new ArrayList<>();

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cccd = text(row, headerIndex, formatter, evaluator, "cccd");
                if (cccd.isEmpty()) continue;

                ExamScoreDTO dto = new ExamScoreDTO();

                dto.setCccd(cccd);
                dto.setSoBaoDanh(text(row, headerIndex, formatter, evaluator, "sobaodanh", "sbd", "so_bao_danh", "so bao danh", "số báo danh"));
                String method = text(row, headerIndex, formatter, evaluator, "phuongthuc", "phuong_thuc", "phuong thuc", "phương thức").toUpperCase();

                if (method.contains("THPT")) method = "THPT";
                else if (method.contains("DGNL") || method.contains("ĐGNL")) method = "ĐGNL";
                else if (method.contains("VSAT")) method = "VSAT";

                dto.setPhuongThuc(method);
                dto.setDiemTo(number(row, headerIndex, formatter, evaluator, "to", "toan", "toán"));
                dto.setDiemLi(number(row, headerIndex, formatter, evaluator, "li", "ly", "vat ly", "vật lý"));
                dto.setDiemHo(number(row, headerIndex, formatter, evaluator, "ho", "hoa", "hoá"));
                dto.setDiemSi(number(row, headerIndex, formatter, evaluator, "si", "sinh"));
                dto.setDiemSu(number(row, headerIndex, formatter, evaluator, "su", "lich su", "lịch sử", "lichsu", "lich_su"));
                dto.setDiemDi(number(row, headerIndex, formatter, evaluator, "di", "dia", "địa", "dia ly", "địa lý", "dia_ly"));
                dto.setDiemVa(number(row, headerIndex, formatter, evaluator, "va", "van", "văn"));
                dto.setDiemN1Thi(number(row, headerIndex, formatter, evaluator, "n1_thi"));
                dto.setDiemN1Cc(number(row, headerIndex, formatter, evaluator, "n1_cc"));
                dto.setDiemCncn(number(row, headerIndex, formatter, evaluator, "cncn", "cong nghe cong nghiep", "công nghệ công nghiệp"));
                dto.setDiemCnnn(number(row, headerIndex, formatter, evaluator, "cnnn", "cong nghe nong nghiep", "công nghệ nông nghiệp"));
                dto.setDiemTi(number(row, headerIndex, formatter, evaluator, "ti", "tin"));
                dto.setDiemKtpl(number(row, headerIndex, formatter, evaluator, "ktpl", "kinh te phap luat", "kinh tế pháp luật"));
                dto.setDiemNl1(number(row, headerIndex, formatter, evaluator, "nl1"));
                dto.setDiemNk1(number(row, headerIndex, formatter, evaluator, "nk1"));
                dto.setDiemNk2(number(row, headerIndex, formatter, evaluator, "nk2"));

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
