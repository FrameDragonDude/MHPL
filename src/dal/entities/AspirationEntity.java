package dal.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class AspirationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private Integer idNv;

    @Column(name = "nn_cccd")
    private String cccd;

    @Column(name = "nv_manganh")
    private String maNganh;

    @Column(name = "nv_tt")
    private Integer thuTuNguyenVong;

    @Column(name = "diem_thxt")
    private Double diemToHopXetTuyen;

    @Column(name = "diem_utqd")
    private Double diemUuTienQuyDoi;

    @Column(name = "diem_cong")
    private Double diemCong;

    @Column(name = "diem_xettuyen")
    private Double diemXetTuyen;

    @Column(name = "nv_ketqua")
    private String ketQua;

    @Column(name = "nv_keys")
    private String nvKey;

    @Column(name = "tt_phuongthuc")
    private String phuongThuc;

    @Column(name = "tt_thm")
    private String toHopMon;

    public Integer getIdNv() {
        return idNv;
    }

    public void setIdNv(Integer idNv) {
        this.idNv = idNv;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public Integer getThuTuNguyenVong() {
        return thuTuNguyenVong;
    }

    public void setThuTuNguyenVong(Integer thuTuNguyenVong) {
        this.thuTuNguyenVong = thuTuNguyenVong;
    }

    public Double getDiemToHopXetTuyen() {
        return diemToHopXetTuyen;
    }

    public void setDiemToHopXetTuyen(Double diemToHopXetTuyen) {
        this.diemToHopXetTuyen = diemToHopXetTuyen;
    }

    public Double getDiemUuTienQuyDoi() {
        return diemUuTienQuyDoi;
    }

    public void setDiemUuTienQuyDoi(Double diemUuTienQuyDoi) {
        this.diemUuTienQuyDoi = diemUuTienQuyDoi;
    }

    public Double getDiemCong() {
        return diemCong;
    }

    public void setDiemCong(Double diemCong) {
        this.diemCong = diemCong;
    }

    public Double getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public void setDiemXetTuyen(Double diemXetTuyen) {
        this.diemXetTuyen = diemXetTuyen;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }

    public String getNvKey() {
        return nvKey;
    }

    public void setNvKey(String nvKey) {
        this.nvKey = nvKey;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getToHopMon() {
        return toHopMon;
    }

    public void setToHopMon(String toHopMon) {
        this.toHopMon = toHopMon;
    }
}
