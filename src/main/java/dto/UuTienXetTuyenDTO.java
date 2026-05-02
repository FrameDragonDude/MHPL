package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UuTienXetTuyenDTO {
    private Integer idUtxt;
    private String tsCccd;
    private String capQuocGia;
    private String doiTuyen;
    private String maMon;
    private String loaiGiai;
    private BigDecimal diemCongMonDatMc;
    private BigDecimal diemCongKhongMonDatMc;
    private String coChungChi;
    private String ghiChu;
    private String utxtKeys;
    private LocalDateTime createdAt;

    public UuTienXetTuyenDTO() {}

    public Integer getIdUtxt() { return idUtxt; }
    public void setIdUtxt(Integer idUtxt) { this.idUtxt = idUtxt; }

    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String tsCccd) { this.tsCccd = tsCccd; }

    public String getCapQuocGia() { return capQuocGia; }
    public void setCapQuocGia(String capQuocGia) { this.capQuocGia = capQuocGia; }

    public String getDoiTuyen() { return doiTuyen; }
    public void setDoiTuyen(String doiTuyen) { this.doiTuyen = doiTuyen; }

    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }

    public String getLoaiGiai() { return loaiGiai; }
    public void setLoaiGiai(String loaiGiai) { this.loaiGiai = loaiGiai; }

    public BigDecimal getDiemCongMonDatMc() { return diemCongMonDatMc; }
    public void setDiemCongMonDatMc(BigDecimal diemCongMonDatMc) { this.diemCongMonDatMc = diemCongMonDatMc; }

    public BigDecimal getDiemCongKhongMonDatMc() { return diemCongKhongMonDatMc; }
    public void setDiemCongKhongMonDatMc(BigDecimal diemCongKhongMonDatMc) { this.diemCongKhongMonDatMc = diemCongKhongMonDatMc; }

    public String getCoChungChi() { return coChungChi; }
    public void setCoChungChi(String coChungChi) { this.coChungChi = coChungChi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getUtxtKeys() { return utxtKeys; }
    public void setUtxtKeys(String utxtKeys) { this.utxtKeys = utxtKeys; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
