package dto;

import java.math.BigDecimal;

public class EnglishConversionDTO {
    private String cccd;
    private String chungChi;
    private String diemGoc;
    private BigDecimal diemQuydoi;
    private BigDecimal diemCong;

    // Getters and Setters
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getChungChi() { return chungChi; }
    public void setChungChi(String chungChi) { this.chungChi = chungChi; }
    public String getDiemGoc() { return diemGoc; }
    public void setDiemGoc(String diemGoc) { this.diemGoc = diemGoc; }
    public BigDecimal getDiemQuydoi() { return diemQuydoi; }
    public void setDiemQuydoi(BigDecimal diemQuydoi) { this.diemQuydoi = diemQuydoi; }
    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal diemCong) { this.diemCong = diemCong; }
}