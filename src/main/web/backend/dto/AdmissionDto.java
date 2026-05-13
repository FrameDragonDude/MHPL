package backend.dto;

public class AdmissionDto {
    private String majorCode;
    private String majorName;
    private String score;
    private String combination;
    private String method;
    private String resultLabel;
    private Integer priority;
    private String diemThxt;
    private String diemUtqd;
    private String diemCong;
    private String diemXettuyen;
    private String diemSan;

    public String getMajorCode() { return majorCode; }
    public void setMajorCode(String majorCode) { this.majorCode = majorCode; }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
    public String getCombination() { return combination; }
    public void setCombination(String combination) { this.combination = combination; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getResultLabel() { return resultLabel; }
    public void setResultLabel(String resultLabel) { this.resultLabel = resultLabel; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getDiemThxt() { return diemThxt; }
    public void setDiemThxt(String diemThxt) { this.diemThxt = diemThxt; }
    public String getDiemUtqd() { return diemUtqd; }
    public void setDiemUtqd(String diemUtqd) { this.diemUtqd = diemUtqd; }
    public String getDiemCong() { return diemCong; }
    public void setDiemCong(String diemCong) { this.diemCong = diemCong; }
    public String getDiemXettuyen() { return diemXettuyen; }
    public void setDiemXettuyen(String diemXettuyen) { this.diemXettuyen = diemXettuyen; }
    public String getDiemSan() { return diemSan; }
    public void setDiemSan(String diemSan) { this.diemSan = diemSan; }
}
