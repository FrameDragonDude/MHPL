package utils.excel;

import dto.ExamScoreDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

public final class ExamScoresMultiSheetExcelImportUtil {

    private ExamScoresMultiSheetExcelImportUtil() {}

    public static List<ExamScoreDTO> importExamScores(File file) throws IOException {
        // Sử dụng Map với Key là CCCD để gom cụm nhiều dòng của cùng một thí sinh lại với nhau
        Map<String, ExamScoreDTO> studentMap = new LinkedHashMap<>();

        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) continue;

                Row headerRow = sheet.getRow(sheet.getFirstRowNum());
                if (headerRow == null) continue;

                // 1. Phân tích cấu trúc cột của Sheet hiện tại
                Map<String, Integer> headerIndex = new HashMap<>();
                for (Cell cell : headerRow) {
                    String val = formatter.formatCellValue(cell, evaluator);
                    if (val != null && !val.trim().isEmpty()) {
                        headerIndex.put(normalize(val), cell.getColumnIndex());
                    }
                }

                // Tìm vị trí các cột chính trong file thực tế
                Integer colCccd = findColumn(headerIndex, "cmnd", "cccd", "socccd", "socmnd");
                Integer colMaMon = findColumn(headerIndex, "mamonthi", "mamon");
                Integer colDiem = findColumn(headerIndex, "diem", "diemthi");

                if (colCccd == null || colDiem == null) {
                    continue; // Không phải sheet chứa điểm chuẩn cấu trúc, bỏ qua
                }

                // 2. Duyệt qua từng dòng dữ liệu
                for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String cccd = safe(formatter.formatCellValue(row.getCell(colCccd), evaluator));
                    if (cccd.isEmpty()) continue;

                    String rawDiem = safe(formatter.formatCellValue(row.getCell(colDiem), evaluator)).replace(',', '.');
                    if (rawDiem.isEmpty()) continue;
                    
                    Double diemVal;
                    try {
                        diemVal = Double.parseDouble(rawDiem);
                    } catch (NumberFormatException e) {
                        continue; // Điểm không hợp lệ (bỏ trống hoặc lỗi chữ)
                    }

                    // Đọc mã môn (Nếu không có cột mã môn như file ĐGNL thì mặc định là DGNL)
                    String maMon = "DGNL";
                    if (colMaMon != null) {
                        maMon = safe(formatter.formatCellValue(row.getCell(colMaMon), evaluator)).toUpperCase();
                    }

                    // Lấy hoặc tạo mới DTO cho thí sinh theo CCCD này
                    ExamScoreDTO dto = studentMap.computeIfAbsent(cccd, k -> {
                        ExamScoreDTO newDto = new ExamScoreDTO();
                        newDto.setCccd(cccd);
                        return newDto;
                    });

                    // 3. Phân loại môn thi và gán vào đúng thuộc tính của DTO
                    classifyAndSetScore(dto, maMon, diemVal);
                }
            }
        }

        return new ArrayList<>(studentMap.values());
    }

    /**
     * Nhận diện mã môn thực tế trong file để đổ vào đúng trường dữ liệu
     */
    private static void classifyAndSetScore(ExamScoreDTO dto, String maMon, Double diem) {
        // Phân loại Phương thức tuyển sinh dựa theo mã môn bắt gặp
        if (maMon.contains("VS") || "M1".equals(maMon) || "M8".equals(maMon)) {
            dto.setPhuongThuc("V-SAT");
        } else if ("DGNL".equals(maMon)) {
            dto.setPhuongThuc("ĐGNL");
        }

        switch (maMon) {
            case "TO_VS": case "M1":
                dto.setDiemTo(diem); break;
            case "LI_VS":
                dto.setDiemLi(diem); break;
            case "HO_VS":
                dto.setDiemHo(diem); break;
            case "SI_VS":
                dto.setDiemSi(diem); break;
            case "SU_VS":
                dto.setDiemSu(diem); break;
            case "DI_VS":
                dto.setDiemDi(diem); break;
            case "VA_VS":
                dto.setDiemVa(diem); break;
            case "N1_VS": case "M8":
                dto.setDiemN1Thi(diem); break; // Đổ điểm tiếng Anh gốc vào cột N1_THI
            case "DGNL":
                // ĐGNL thí sinh có thể thi nhiều đợt, luật yêu cầu giữ lại điểm cao nhất (Max)
                if (dto.getDiemNl1() == null || diem > dto.getDiemNl1()) {
                    dto.setDiemNl1(diem); // ĐGNL lưu vào cột NL1
                }
                break;
        }
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String... keys) {
        for (String key : keys) {
            Integer col = headerIndex.get(normalize(key));
            if (col != null) return col;
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT);
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }
}