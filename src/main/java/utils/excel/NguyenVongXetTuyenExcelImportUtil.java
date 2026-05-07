package utils.excel;

import dto.NguyenVongXetTuyenDTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NguyenVongXetTuyenExcelImportUtil {

	public static List<NguyenVongXetTuyenDTO> importFromExcel(File file) throws IOException {
		List<NguyenVongXetTuyenDTO> results = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
			Map<String, Integer> headerMap = extractHeaders(sheet);

			int startRow = 1;
			for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null || isRowEmpty(row)) {
					continue;
				}

				NguyenVongXetTuyenDTO dto = new NguyenVongXetTuyenDTO();

				Integer cccdIdx = headerMap.get("CCCD");
				if (cccdIdx != null) {
					dto.setNnCccd(getCellString(row, cccdIdx));
				}

				Integer nganhIdx = headerMap.get("MANGANH");
				if (nganhIdx != null) {
					dto.setNvManganh(getCellString(row, nganhIdx));
				}

				Integer ttIdx = headerMap.get("TT");
				if (ttIdx != null) {
					dto.setNvTt(getCellInt(row, ttIdx));
				}

				Integer diemThxtIdx = headerMap.get("DIEMTHXT");
				if (diemThxtIdx != null) {
					dto.setDiemThxt(getCellDecimal(row, diemThxtIdx));
				}

				Integer diemUtqdIdx = headerMap.get("DIEMUTQD");
				if (diemUtqdIdx != null) {
					dto.setDiemUtqd(getCellDecimal(row, diemUtqdIdx));
				}

				Integer diemCongIdx = headerMap.get("DIEMCONG");
				if (diemCongIdx != null) {
					dto.setDiemCong(getCellDecimal(row, diemCongIdx));
				}

				Integer diemXettuyenIdx = headerMap.get("DIEMXETTUYEN");
				if (diemXettuyenIdx != null) {
					dto.setDiemXettuyen(getCellDecimal(row, diemXettuyenIdx));
				}

				Integer ketquaIdx = headerMap.get("KETQUA");
				if (ketquaIdx != null) {
					dto.setNvKetqua(getCellString(row, ketquaIdx));
				}

				Integer keysIdx = headerMap.get("KEYS");
				if (keysIdx != null) {
					dto.setNvKeys(getCellString(row, keysIdx));
				}

				Integer phuongthucIdx = headerMap.get("PHUONGTHUC");
				if (phuongthucIdx != null) {
					dto.setTtPhuongthuc(getCellString(row, phuongthucIdx));
				}

				Integer thmIdx = headerMap.get("THM");
				if (thmIdx != null) {
					dto.setTtThm(getCellString(row, thmIdx));
				}

				results.add(dto);
			}
		}
		return results;
	}

	private static Map<String, Integer> extractHeaders(Sheet sheet) {
		Map<String, Integer> headerMap = new HashMap<>();
		Row headerRow = sheet.getRow(0);
		if (headerRow == null) {
			return headerMap;
		}

		for (int i = 0; i < headerRow.getLastCellNum(); i++) {
			String raw = getCellString(headerRow, i);
			String normalized = normalizeHeaderName(raw);
			if (!normalized.isEmpty()) {
				headerMap.put(normalized, i);
			}
		}

		return headerMap;
	}

	private static String normalizeHeaderName(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return "";
		}
		return raw.trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
	}

	private static boolean isRowEmpty(Row row) {
		if (row == null) {
			return true;
		}
		for (int i = 0; i < row.getLastCellNum(); i++) {
			if (row.getCell(i) != null && row.getCell(i).getStringCellValue() != null && !row.getCell(i).getStringCellValue().trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static String getCellString(Row row, int colIndex) {
		if (row == null) {
			return "";
		}
		Cell cell = row.getCell(colIndex);
		if (cell == null) {
			return "";
		}
		String value = cell.getStringCellValue().trim();
		return value.isEmpty() ? null : value;
	}

	private static Integer getCellInt(Row row, int colIndex) {
		if (row == null) {
			return null;
		}
		Cell cell = row.getCell(colIndex);
		if (cell == null) {
			return null;
		}
		try {
			return (int) cell.getNumericCellValue();
		} catch (Exception ignored) {
		}
		try {
			return Integer.parseInt(cell.getStringCellValue().trim());
		} catch (Exception ignored) {
		}
		return null;
	}

	private static BigDecimal getCellDecimal(Row row, int colIndex) {
		if (row == null) {
			return null;
		}
		Cell cell = row.getCell(colIndex);
		if (cell == null) {
			return null;
		}

		try {
			return new BigDecimal(cell.getNumericCellValue());
		} catch (Exception ignored) {
		}

		try {
			String strValue = cell.getStringCellValue().trim();
			if (strValue.isEmpty()) {
				return null;
			}
			strValue = strValue.replace(",", ".");
			return new BigDecimal(strValue);
		} catch (Exception ignored) {
		}

		return null;
	}
}
