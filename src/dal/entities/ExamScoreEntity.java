package dal.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_diemthixettuyen")
public class ExamScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer idDiemThi;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "TO")
    private Double diemTo;

    @Column(name = "VA")
    private Double diemVa;

    @Column(name = "LI")
    private Double diemLi;

    @Column(name = "HO")
    private Double diemHo;

    @Column(name = "SI")
    private Double diemSi;

    @Column(name = "SU")
    private Double diemSu;

    @Column(name = "DI")
    private Double diemDi;

    @Column(name = "N1_CC")
    private Double diemN1Cc;

    @Column(name = "KTPL")
    private Double diemKtpl;

    @Column(name = "TI")
    private Double diemTi;

    @Column(name = "CNCN")
    private Double diemCncn;

    @Column(name = "CNNN")
    private Double diemCnnn;

    @Column(name = "NK1")
    private Double diemNk1;

    @Column(name = "NK2")
    private Double diemNk2;

    public Integer getIdDiemThi() {
        return idDiemThi;
    }

    public void setIdDiemThi(Integer idDiemThi) {
        this.idDiemThi = idDiemThi;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoBaoDanh() {
        return soBaoDanh;
    }

    public void setSoBaoDanh(String soBaoDanh) {
        this.soBaoDanh = soBaoDanh;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public Double getDiemTo() {
        return diemTo;
    }

    public void setDiemTo(Double diemTo) {
        this.diemTo = diemTo;
    }

    public Double getDiemVa() {
        return diemVa;
    }

    public void setDiemVa(Double diemVa) {
        this.diemVa = diemVa;
    }

    public Double getDiemLi() {
        return diemLi;
    }

    public void setDiemLi(Double diemLi) {
        this.diemLi = diemLi;
    }

    public Double getDiemHo() {
        return diemHo;
    }

    public void setDiemHo(Double diemHo) {
        this.diemHo = diemHo;
    }

    public Double getDiemSi() {
        return diemSi;
    }

    public void setDiemSi(Double diemSi) {
        this.diemSi = diemSi;
    }

    public Double getDiemSu() {
        return diemSu;
    }

    public void setDiemSu(Double diemSu) {
        this.diemSu = diemSu;
    }

    public Double getDiemDi() {
        return diemDi;
    }

    public void setDiemDi(Double diemDi) {
        this.diemDi = diemDi;
    }

    public Double getDiemN1Cc() {
        return diemN1Cc;
    }

    public void setDiemN1Cc(Double diemN1Cc) {
        this.diemN1Cc = diemN1Cc;
    }

    public Double getDiemKtpl() {
        return diemKtpl;
    }

    public void setDiemKtpl(Double diemKtpl) {
        this.diemKtpl = diemKtpl;
    }

    public Double getDiemTi() {
        return diemTi;
    }

    public void setDiemTi(Double diemTi) {
        this.diemTi = diemTi;
    }

    public Double getDiemCncn() {
        return diemCncn;
    }

    public void setDiemCncn(Double diemCncn) {
        this.diemCncn = diemCncn;
    }

    public Double getDiemCnnn() {
        return diemCnnn;
    }

    public void setDiemCnnn(Double diemCnnn) {
        this.diemCnnn = diemCnnn;
    }

    public Double getDiemNk1() {
        return diemNk1;
    }

    public void setDiemNk1(Double diemNk1) {
        this.diemNk1 = diemNk1;
    }

    public Double getDiemNk2() {
        return diemNk2;
    }

    public void setDiemNk2(Double diemNk2) {
        this.diemNk2 = diemNk2;
    }
}
