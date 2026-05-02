package utils.excel;

import dto.ExamScoreDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class ExamScoresExcelExportUtil {

    private static final String[] HEADERS = {
            "STT", "CCCD", "Số báo danh", "Phương thức",
            "Toán", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Văn",
            "N1_THI", "N1_CC",
            "CNCN", "CNNN", "Tin", "KTPL",
            "NL1", "NK1", "NK2"
    };

    private ExamScoresExcelExportUtil() {}

    public static void exportExamScores(File file, List<ExamScoreDTO> list) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DiemThi");

            writeHeader(sheet);
            writeRows(sheet, list);
            autoSize(sheet, HEADERS.length);

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }

    private static void writeHeader(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADERS[i]);
        }
    }

    private static void writeRows(Sheet sheet, List<ExamScoreDTO> list) {
        for (int i = 0; i < list.size(); i++) {
            ExamScoreDTO d = list.get(i);
            Row row = sheet.createRow(i + 1);

            int col = 0;

            row.createCell(col++).setCellValue(i + 1);
            row.createCell(col++).setCellValue(text(d.getCccd()));
            row.createCell(col++).setCellValue(text(d.getSoBaoDanh()));
            row.createCell(col++).setCellValue(text(d.getPhuongThuc()));

            writeNumber(row, col++, d.getDiemTo());
            writeNumber(row, col++, d.getDiemLi());
            writeNumber(row, col++, d.getDiemHo());
            writeNumber(row, col++, d.getDiemSi());
            writeNumber(row, col++, d.getDiemSu());
            writeNumber(row, col++, d.getDiemDi());
            writeNumber(row, col++, d.getDiemVa());

            writeNumber(row, col++, d.getDiemN1Thi());
            writeNumber(row, col++, d.getDiemN1Cc());

            writeNumber(row, col++, d.getDiemCncn());
            writeNumber(row, col++, d.getDiemCnnn());
            writeNumber(row, col++, d.getDiemTi());
            writeNumber(row, col++, d.getDiemKtpl());

            writeNumber(row, col++, d.getDiemNl1());
            writeNumber(row, col++, d.getDiemNk1());
            writeNumber(row, col++, d.getDiemNk2());
        }
    }

    private static void writeNumber(Row row, int col, Double value) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static void autoSize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}