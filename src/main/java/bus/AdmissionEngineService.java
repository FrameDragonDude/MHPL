package bus;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Xét tuyển: Xếp hạng, phân loại kết quả theo điểm, ngành, phương thức
 * Yêu cầu đồ án: Thực hiện xét tuyển theo ngành, xếp hạng theo điểm
 */
public class AdmissionEngineService {
    
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
}