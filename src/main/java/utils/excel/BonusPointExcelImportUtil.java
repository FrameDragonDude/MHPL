package utils.excel;

import dto.BonusPointDTO;
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

public final class BonusPointExcelImportUtil {
    private BonusPointExcelImportUtil() {
    }

    private static final String[] CCCD_HEADERS = {"cccd", "socccd", "socancuoc", "socancuoccongdan", "cancuoccongdan", "cancuoc"};
    private static final String[] TA_SCORE_HEADERS = {"diemcc", "diemcongta", "diemtienganh", "diemcong"};
    private static final String[] UT_SCORE_HEADERS = {"diemcongchomondatgiai", "mondatgiai"};
    private static final String[] UT_HSG_SCORE_HEADERS = {"diemcongchothxkmondatgiai", "diemcongchothxk", "thxkmondatgiai"};
    
    private static final String[] WISH_MA_XET_TUYEN_HEADERS = {"maxettuyen"};
    private static final String[] COMBINATION_MA_XET_TUYEN_HEADERS = {"manganh", "maxettuyen", "mangành", "mãxéttuyển"};
    
    // Đổi hẳn từ khóa nhận diện sang cấu trúc tên tổ hợp sạch từ Excel của bạn
    private static final String[] MA_TO_HOP_HEADERS = {"tentohop", "tohop", "mãtổhợp", "tổhợpmôn", "matohop"};

    private static final int FALLBACK_CCCD_COL = 1;
    private static final int FALLBACK_CERT_SCORE_COL = 4;
    private static final int FALLBACK_UTXT_SCORE_COL = 5;

    public static List<BonusPointDTO> importRows(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = null;
            Row headerRow = null;
            Map<String, Integer> headerIndex = null;

            int totalSheets = workbook.getNumberOfSheets();
            for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
                Sheet candidate = workbook.getSheetAt(sheetIndex);
                HeaderMatch match = findHeaderRow(candidate, formatter, evaluator);
                if (match != null) {
                    sheet = candidate;
                    headerRow = match.headerRow;
                    headerIndex = match.headerIndex;
                    break;
                }
            }

            if (sheet == null) {
                return List.of();
            }

            String fileName = file.getName().toLowerCase(Locale.ROOT);
            boolean isTiengAnh = fileName.contains("tieng anh") || fileName.contains("ielts") || fileName.contains("chung chi");

            List<BonusPointDTO> results = new ArrayList<>(sheet.getLastRowNum() + 1);
            int startRow = headerRow != null ? headerRow.getRowNum() + 1 : findFirstDataRow(sheet, formatter, evaluator);
            int lastRow = sheet.getLastRowNum();

            for (int rowIndex = Math.max(startRow, 0); rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                BonusPointDTO dto = new BonusPointDTO();
                dto.setTsCccd(readText(row, formatter, evaluator, headerIndex, CCCD_HEADERS, FALLBACK_CCCD_COL));
                
                dto.setMaNganh("");
                dto.setMaToHop("");
                dto.setPhuongThuc("");

                if (isTiengAnh) {
                    Double dcc = readNumber(row, formatter, evaluator, headerIndex, TA_SCORE_HEADERS, FALLBACK_CERT_SCORE_COL);
                    dto.setDiemCC(dcc != null ? dcc : 0.0);
                    dto.setDiemUtxt(0.0);
                } else {
                    Double scoreUt = readNumber(row, formatter, evaluator, headerIndex, UT_SCORE_HEADERS, FALLBACK_UTXT_SCORE_COL);
                    Double scoreHsg = readNumber(row, formatter, evaluator, headerIndex, UT_HSG_SCORE_HEADERS, FALLBACK_UTXT_SCORE_COL);

                    double v1 = (scoreUt != null) ? scoreUt : 0.0;
                    double v2 = (scoreHsg != null) ? scoreHsg : 0.0;

                    dto.setDiemCC(0.0);
                    dto.setDiemUtxt(v1 + v2);
                }

                if (isBlank(dto.getTsCccd())) {
                    continue;
                }

                double total = dto.getDiemCC() + dto.getDiemUtxt();
                dto.setDiemTong(total > 3.0 ? 3.0 : total);
                dto.setDcKeys(dto.getTsCccd().trim() + "||");
                results.add(dto);
            }
            return results;
        }
    }

    public static Map<String, List<String>> loadCombinationMap(File combinationFile) throws IOException {
        Map<String, List<String>> combinationMap = new HashMap<>();
        try (FileInputStream input = new FileInputStream(combinationFile);
             Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = null;
            Map<String, Integer> headerIndex = null;

            int limit = Math.min(sheet.getLastRowNum(), 20);
            for (int i = sheet.getFirstRowNum(); i <= limit; i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                Map<String, Integer> idx = buildHeaderIndex(r, formatter, evaluator);
                if (hasAny(idx, COMBINATION_MA_XET_TUYEN_HEADERS) && hasAny(idx, MA_TO_HOP_HEADERS)) {
                    headerRow = r;
                    headerIndex = idx;
                    break;
                }
            }

            int start = headerRow != null ? headerRow.getRowNum() + 1 : 1;
            int lastRow = sheet.getLastRowNum();
            for (int i = start; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String maNganh = readText(row, formatter, evaluator, headerIndex, COMBINATION_MA_XET_TUYEN_HEADERS, 1);
                String maToHop = readText(row, formatter, evaluator, headerIndex, MA_TO_HOP_HEADERS, 5); // Fallback cột 5 (TEN_TO_HOP)
                if (!isBlank(maNganh) && !isBlank(maToHop)) {
                    combinationMap.computeIfAbsent(maNganh.trim(), k -> new ArrayList<>()).add(maToHop.trim());
                }
            }
        }
        return combinationMap;
    }

    public static List<BonusPointDTO> importWishesOnly(File wishFile, Map<String, List<String>> combinationMap) throws IOException {
        List<BonusPointDTO> wishes = new ArrayList<>(80000);
        try (FileInputStream input = new FileInputStream(wishFile);
             Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int totalSheets = workbook.getNumberOfSheets();
            for (int sIdx = 0; sIdx < totalSheets; sIdx++) {
                Sheet sheet = workbook.getSheetAt(sIdx);
                String sheetName = sheet.getSheetName().toLowerCase(Locale.ROOT);
                
                if (sheetName.contains("tk") || sheetName.contains("thongke") || sheetName.contains("chung")) {
                    continue;
                }

                Row headerRow = null;
                Map<String, Integer> headerIndex = null;

                int limit = Math.min(sheet.getLastRowNum(), 20);
                for (int i = sheet.getFirstRowNum(); i <= limit; i++) {
                    Row r = sheet.getRow(i);
                    if (r == null) continue;
                    Map<String, Integer> idx = buildHeaderIndex(r, formatter, evaluator);
                    if (hasAny(idx, CCCD_HEADERS) && hasAny(idx, WISH_MA_XET_TUYEN_HEADERS)) {
                        headerRow = r;
                        headerIndex = idx;
                        break;
                    }
                }

                if (headerIndex == null) continue;

                int start = headerRow.getRowNum() + 1;
                int lastRow = sheet.getLastRowNum();
                for (int i = start; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String cccd = readText(row, formatter, evaluator, headerIndex, CCCD_HEADERS, 1);
                    String maXetTuyen = readText(row, formatter, evaluator, headerIndex, WISH_MA_XET_TUYEN_HEADERS, 5);

                    if (isBlank(cccd) || isBlank(maXetTuyen) || cccd.toUpperCase(Locale.ROOT).contains("CCCD")) {
                        continue;
                    }

                    Map<String, List<String>> safeMap = (combinationMap != null) ? combinationMap : new HashMap<>();
                    List<String> validToHops = safeMap.get(maXetTuyen.trim());

                    if (validToHops != null && !validToHops.isEmpty()) {
                        for (String toHop : validToHops) {
                            BonusPointDTO resultDto = new BonusPointDTO();
                            resultDto.setTsCccd(cccd.trim());
                            resultDto.setDiemCC(0.0);
                            resultDto.setDiemUtxt(0.0);
                            resultDto.setDiemTong(0.0);
                            resultDto.setMaNganh(maXetTuyen.trim());
                            resultDto.setMaToHop(toHop.trim());
                            resultDto.setPhuongThuc("");
                            resultDto.setDcKeys(cccd.trim() + "|" + maXetTuyen.trim() + "|" + toHop.trim());
                            wishes.add(resultDto);
                        }
                    } else {
                        BonusPointDTO resultDto = new BonusPointDTO();
                        resultDto.setTsCccd(cccd.trim());
                        resultDto.setDiemCC(0.0);
                        resultDto.setDiemUtxt(0.0);
                        resultDto.setDiemTong(0.0);
                        resultDto.setMaNganh(maXetTuyen.trim());
                        resultDto.setMaToHop("");
                        resultDto.setPhuongThuc("");
                        resultDto.setDcKeys(cccd.trim() + "|" + maXetTuyen.trim() + "|");
                        wishes.add(resultDto);
                    }
                }
            }
        }
        return wishes;
    }

    private static boolean matchesRequiredHeaders(Map<String, Integer> headerIndex) {
        return headerIndex.containsKey(normalize("cccd")) || headerIndex.containsKey(normalize("socccd")) 
            || headerIndex.containsKey(normalize("socancuoc")) || headerIndex.containsKey(normalize("socancuoccongdan"));
    }

    private static HeaderMatch findHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (sheet == null) {
            return null;
        }
        int first = sheet.getFirstRowNum();
        int last = Math.min(sheet.getLastRowNum(), first + 20);
        for (int rowIndex = first; rowIndex <= last; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(row, formatter, evaluator);
            if (matchesRequiredHeaders(headerIndex)) {
                return new HeaderMatch(row, headerIndex);
            }
        }
        return null;
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

    private static boolean hasAny(Map<String, Integer> headerIndex, String[] keys) {
        for (String key : keys) {
            if (headerIndex.containsKey(normalize(key))) {
                return true;
            }
        }
        return false;
    }

    private static int findFirstDataRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        int first = sheet.getFirstRowNum();
        int last = Math.min(sheet.getLastRowNum(), first + 30);
        for (int rowIndex = first; rowIndex <= last; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            if (!isLikelyHeaderRow(row, formatter, evaluator)) {
                return rowIndex;
            }
        }
        return first;
    }

    private static boolean isLikelyHeaderRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (Cell cell : row) {
            String value = normalize(formatter.formatCellValue(cell, evaluator));
            if (value.equals("cccd") || value.equals("socccd")) {
                return true;
            }
        }
        return false;
    }

    private static String readText(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        String value = readCellText(row, formatter, evaluator, headerIndex, headerKeys, fallbackColumn);
        return value == null ? "" : value;
    }

    private static Double readNumber(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        String value = readCellText(row, formatter, evaluator, headerIndex, headerKeys, fallbackColumn);
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.').replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String readCellText(Row row, DataFormatter formatter, FormulaEvaluator evaluator, Map<String, Integer> headerIndex, String[] headerKeys, int fallbackColumn) {
        if (headerIndex != null) {
            Integer col = findColumn(headerIndex, headerKeys);
            if (col != null) {
                Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = safe(formatter.formatCellValue(cell, evaluator));
                if (!isBlank(value)) {
                    return value;
                }
            }
        }
        Cell fallbackCell = row.getCell(fallbackColumn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        String fallbackValue = safe(formatter.formatCellValue(fallbackCell, evaluator));
        return isBlank(fallbackValue) ? "" : fallbackValue;
    }

    private static Integer findColumn(Map<String, Integer> headerIndex, String[] keys) {
        for (String key : keys) {
            Integer col = headerIndex.get(normalize(key));
            if (col != null) {
                return col;
            }
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalize(String value) {
        String lower = safe(value).toLowerCase(Locale.ROOT).replace('đ', 'd');
        String noMark = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace('đ', 'd');
        return noMark.replaceAll("[^a-z0-9]", "");
    }

    private static final class HeaderMatch {
        private final Row headerRow;
        private final Map<String, Integer> headerIndex;

        private HeaderMatch(Row headerRow, Map<String, Integer> headerIndex) {
            this.headerRow = headerRow;
            this.headerIndex = headerIndex;
        }
    }
}