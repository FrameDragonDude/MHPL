package bus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import dal.dao.AdmissionDAO;
import dto.EnglishConversionDTO;
import dto.PriorityAdmissionDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Xét tuyển: Xếp hạng, phân loại kết quả theo điểm, ngành, phương thức
 * Yêu cầu đồ án: Thực hiện xét tuyển theo ngành, xếp hạng theo điểm
 */
public class AdmissionEngineService {
    
    private final AdmissionDAO dao = new AdmissionDAO();

    /**
     * Tính điểm xét tuyển cuối cùng
     * 
     * Công thức: Điểm xét tuyển = Điểm thi + Ưu tiên + Cộng + Độ lệch
     * 
     * @param examScore điểm thi theo phương thức
     * @param priorityScore ưu tiên (KVƯT + ĐTƯT)
     * @param bonusScore điểm cộng
     * @param doLech độ lệch tổ hợp
     * @return điểm xét tuyển
     */
    public BigDecimal calculateAdmissionScore(
            BigDecimal examScore,
            BigDecimal priorityScore,
            BigDecimal bonusScore,
            BigDecimal doLech) {
        
        if (examScore == null) examScore = BigDecimal.ZERO;
        if (priorityScore == null) priorityScore = BigDecimal.ZERO;
        if (bonusScore == null) bonusScore = BigDecimal.ZERO;
        if (doLech == null) doLech = BigDecimal.ZERO;
        
        return examScore
                .add(priorityScore)
                .add(bonusScore)
                .add(doLech)
                .setScale(5, RoundingMode.HALF_UP);
    }
    
    /**
     * Kiểm tra trúng/không trúng tuyển
     */
    public boolean isAdmitted(BigDecimal admissionScore, BigDecimal thresholdScore) {
        if (admissionScore == null || thresholdScore == null) return false;
        return admissionScore.compareTo(thresholdScore) >= 0;
    }
    
    /**
     * Nhãn kết quả
     */
    public String getAdmissionLabel(boolean admitted) {
        return admitted ? "Trúng tuyển" : "Chưa trúng tuyển";
    }

    public boolean importEnglishFromExcel(List<EnglishConversionDTO> dtos) throws SQLException {
        if (dtos == null || dtos.isEmpty()) return false;
        
        for (EnglishConversionDTO dto : dtos) {
            if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) {
                System.err.println("Phát hiện bản ghi tiếng Anh lỗi: Trống dữ liệu CCCD");
                return false;
            }
        }
        dao.saveEnglishConversionData(dtos);
        return true;
    }

    public boolean importPriorityFromExcel(List<PriorityAdmissionDTO> dtos) throws SQLException {
        if (dtos == null || dtos.isEmpty()) return false;
        
        for (PriorityAdmissionDTO dto : dtos) {
            if (dto.getCccd() == null || dto.getCccd().trim().isEmpty()) {
                System.err.println("Phát hiện bản ghi ưu tiên xét tuyển lỗi: Trống dữ liệu CCCD");
                return false;
            }
        }
        dao.savePriorityAdmissionData(dtos);
        return true;
    }

    

    public static BigDecimal interpolateScore(BigDecimal x, Map<String, BigDecimal> range) {
        if (range == null || x == null) return BigDecimal.ZERO;

        BigDecimal a = range.get("a");
        BigDecimal b = range.get("b");
        BigDecimal c = range.get("c");
        BigDecimal d = range.get("d");

        if (b.compareTo(a) == 0) return c;

        BigDecimal tuSo = x.subtract(a);
        BigDecimal mauSo = b.subtract(a);
        BigDecimal ratio = tuSo.divide(mauSo, 4, RoundingMode.HALF_UP);
        BigDecimal deltaDest = d.subtract(c);
        BigDecimal y = c.add(ratio.multiply(deltaDest));

        BigDecimal maxLimit = c.compareTo(d) > 0 ? c : d;
        BigDecimal minLimit = c.compareTo(d) < 0 ? c : d;

        if (y.compareTo(maxLimit) > 0) return maxLimit;
        if (y.compareTo(minLimit) < 0) return minLimit;

        return y.setScale(2, RoundingMode.HALF_UP);
    }

}