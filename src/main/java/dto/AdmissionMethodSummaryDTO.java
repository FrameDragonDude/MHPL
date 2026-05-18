package dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdmissionMethodSummaryDTO {
    private String majorCode;
    private String majorName;
    private long totalCount;
    private Map<String, Long> methodCounts = new LinkedHashMap<>();

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Long> getMethodCounts() {
        return methodCounts;
    }

    public void setMethodCounts(Map<String, Long> methodCounts) {
        this.methodCounts = methodCounts;
    }
}