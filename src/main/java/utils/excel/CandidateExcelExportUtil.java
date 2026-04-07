package utils.excel;

import dto.CandidateDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class CandidateExcelExportUtil {

	private static final String[] HEADERS = {
			"STT", "CCCD", "Họ", "Tên", "Họ tên", "Ngày sinh", "Giới tính", "Số điện thoại", "Email", "Nơi sinh", "ĐTƯT", "KVƯT",
			"TO", "VA", "LI", "HO", "SI", "SU", "DI", "GDCD", "NN", "Mã môn NN", "KTPL", "TI", "CNCN", "CNNN",
			"Chương trình", "NK1", "NK2", "NK3", "NK4", "NK5", "NK6", "NK7", "NK8", "NK9", "NK10",
			"Điểm xét tốt nghiệp", "Dân tộc", "Mã dân tộc"
	};

	private CandidateExcelExportUtil() {
	}

	public static void exportCandidates(File file, List<CandidateDTO> candidates) throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("ThiSinh");
			writeHeader(sheet);
			writeRows(sheet, candidates);
			autoSize(sheet, HEADERS.length);

			try (FileOutputStream out = new FileOutputStream(file)) {
				workbook.write(out);
			}
		}
	}

	private static void writeHeader(Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < HEADERS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(HEADERS[i]);
		}
	}

	private static void writeRows(Sheet sheet, List<CandidateDTO> candidates) {
		for (int i = 0; i < candidates.size(); i++) {
			CandidateDTO c = candidates.get(i);
			Row row = sheet.createRow(i + 1);
			int col = 0;

			row.createCell(col++).setCellValue(i + 1);
			row.createCell(col++).setCellValue(text(c.getCccd()));
			row.createCell(col++).setCellValue(text(c.getHo()));
			row.createCell(col++).setCellValue(text(c.getTen()));
			row.createCell(col++).setCellValue(text(c.getHoTen()));
			row.createCell(col++).setCellValue(text(c.getNgaySinh()));
			row.createCell(col++).setCellValue(text(c.getGioiTinh()));
			row.createCell(col++).setCellValue(text(c.getDienThoai()));
			row.createCell(col++).setCellValue(text(c.getEmail()));
			row.createCell(col++).setCellValue(text(c.getNoiSinh()));
			row.createCell(col++).setCellValue(text(c.getDoiTuong()));
			row.createCell(col++).setCellValue(text(c.getKhuVuc()));

			writeNumber(row, col++, c.getDiemTo());
			writeNumber(row, col++, c.getDiemVa());
			writeNumber(row, col++, c.getDiemLi());
			writeNumber(row, col++, c.getDiemHo());
			writeNumber(row, col++, c.getDiemSi());
			writeNumber(row, col++, c.getDiemSu());
			writeNumber(row, col++, c.getDiemDi());
			writeNumber(row, col++, c.getDiemGdcd());
			writeNumber(row, col++, c.getDiemNn());
			row.createCell(col++).setCellValue(text(c.getMaMonNn()));
			writeNumber(row, col++, c.getDiemKtpl());
			writeNumber(row, col++, c.getDiemTi());
			writeNumber(row, col++, c.getDiemCncn());
			writeNumber(row, col++, c.getDiemCnnn());

			row.createCell(col++).setCellValue(text(c.getChuongTrinh()));
			writeNumber(row, col++, c.getDiemNk1());
			writeNumber(row, col++, c.getDiemNk2());
			writeNumber(row, col++, c.getDiemNk3());
			writeNumber(row, col++, c.getDiemNk4());
			writeNumber(row, col++, c.getDiemNk5());
			writeNumber(row, col++, c.getDiemNk6());
			writeNumber(row, col++, c.getDiemNk7());
			writeNumber(row, col++, c.getDiemNk8());
			writeNumber(row, col++, c.getDiemNk9());
			writeNumber(row, col++, c.getDiemNk10());
			writeNumber(row, col++, c.getDiemXetTotNghiep());
			row.createCell(col++).setCellValue(text(c.getDanToc()));
			row.createCell(col).setCellValue(text(c.getMaDanToc()));
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
		for (int col = 0; col < colCount; col++) {
			sheet.autoSizeColumn(col);
		}
	}
}
