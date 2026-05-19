package dto;

import java.math.BigDecimal;

public class PriorityAdmissionDTO {
   private String cccd;
    private String cap;
    private String doiTuong; // Học sinh giỏi
    private String maMon;    // TO, VA, SU, SI, DI...
    private String loaiGiai; // Giải Nhất, Nhì, Ba...
    private BigDecimal diemCongTrungMon;
    private BigDecimal diemCongKhacMon;

    // Getters and Setters
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }
    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }
    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }
    public String getLoaiGiai() { return loaiGiai; }
    public void setLoaiGiai(String loaiGiai) { this.loaiGiai = loaiGiai; }
    public BigDecimal getDiemCongTrungMon() { return diemCongTrungMon; }
    public void setDiemCongTrungMon(BigDecimal diemCongTrungMon) { this.diemCongTrungMon = diemCongTrungMon; }
    public BigDecimal getDiemCongKhacMon() { return diemCongKhacMon; }
    public void setDiemCongKhacMon(BigDecimal diemCongKhacMon) { this.diemCongKhacMon = diemCongKhacMon; }
}