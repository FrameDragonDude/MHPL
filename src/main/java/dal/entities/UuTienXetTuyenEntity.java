package dal.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_uutien_xettuyen")
public class UuTienXetTuyenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utxt")
    private Integer idUtxt;

    @Column(name = "ts_cccd", nullable = false)
    private String tsCccd;

    @Column(name = "cap_quoc_gia")
    private String capQuocGia;

    @Column(name = "doi_tuyen")
    private String doiTuyen;

    @Column(name = "ma_mon")
    private String maMon;

    @Column(name = "loai_giai")
    private String loaiGiai;

    @Column(name = "diem_cong_mondatmc")
    private BigDecimal diemCongMonDatMc;

    @Column(name = "diem_cong_khongmondatmc")
    private BigDecimal diemCongKhongMonDatMc;

    @Column(name = "co_chung_chi")
    private String coChungChi;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "utxt_keys", nullable = false, unique = true)
    private String utxtKeys;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UuTienXetTuyenEntity() {}

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
