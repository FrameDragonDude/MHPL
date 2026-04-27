package dal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemthixettuyen")
public class ExamScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer id;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "d_phuongthuc")
    private String dPhuongThuc;

    @Column(name = "TO")
    private BigDecimal to;

    @Column(name = "VA")
    private BigDecimal va;

    @Column(name = "LI")
    private BigDecimal li;

    @Column(name = "HO")
    private BigDecimal ho;

    @Column(name = "SI")
    private BigDecimal si;

    @Column(name = "SU")
    private BigDecimal su;

    @Column(name = "DI")
    private BigDecimal di;

    @Column(name = "N1_THI")
    private BigDecimal n1Thi;

    @Column(name = "N1_CC")
    private BigDecimal n1Cc;

    @Column(name = "KTPL")
    private BigDecimal ktpl;

    @Column(name = "TI")
    private BigDecimal ti;

    @Column(name = "CNCN")
    private BigDecimal cncn;

    @Column(name = "CNNN")
    private BigDecimal cnnn;

    @Column(name = "NL1")
    private BigDecimal nl1;

    @Column(name = "NK1")
    private BigDecimal nk1;

    @Column(name = "NK2")
    private BigDecimal nk2;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getSoBaoDanh() { return soBaoDanh; }
    public void setSoBaoDanh(String soBaoDanh) { this.soBaoDanh = soBaoDanh; }

    public String getDPhuongThuc() { return dPhuongThuc; }
    public void setDPhuongThuc(String dPhuongThuc) { this.dPhuongThuc = dPhuongThuc; }

    private Double toDouble(BigDecimal val) {
        return val == null ? null : val.doubleValue();
    }

    private BigDecimal toBigDecimal(Double val) {
        return val == null ? null : BigDecimal.valueOf(val);
    }

    public Double getDiemTo() { return toDouble(to); }
    public void setDiemTo(Double v) { this.to = toBigDecimal(v); }

    public Double getDiemVa() { return toDouble(va); }
    public void setDiemVa(Double v) { this.va = toBigDecimal(v); }

    public Double getDiemLi() { return toDouble(li); }
    public void setDiemLi(Double v) { this.li = toBigDecimal(v); }

    public Double getDiemHo() { return toDouble(ho); }
    public void setDiemHo(Double v) { this.ho = toBigDecimal(v); }

    public Double getDiemSi() { return toDouble(si); }
    public void setDiemSi(Double v) { this.si = toBigDecimal(v); }

    public Double getDiemSu() { return toDouble(su); }
    public void setDiemSu(Double v) { this.su = toBigDecimal(v); }

    public Double getDiemDi() { return toDouble(di); }
    public void setDiemDi(Double v) { this.di = toBigDecimal(v); }

    public Double getDiemN1Thi() { return toDouble(n1Thi); }
    public void setDiemN1Thi(Double v) { this.n1Thi = toBigDecimal(v); }

    public Double getDiemN1Cc() { return toDouble(n1Cc); }
    public void setDiemN1Cc(Double v) { this.n1Cc = toBigDecimal(v); }

    public Double getDiemKtpl() { return toDouble(ktpl); }
    public void setDiemKtpl(Double v) { this.ktpl = toBigDecimal(v); }

    public Double getDiemTi() { return toDouble(ti); }
    public void setDiemTi(Double v) { this.ti = toBigDecimal(v); }

    public Double getDiemCncn() { return toDouble(cncn); }
    public void setDiemCncn(Double v) { this.cncn = toBigDecimal(v); }

    public Double getDiemCnnn() { return toDouble(cnnn); }
    public void setDiemCnnn(Double v) { this.cnnn = toBigDecimal(v); }

    public Double getDiemNl1() { return toDouble(nl1); }
    public void setDiemNl1(Double v) { this.nl1 = toBigDecimal(v); }

    public Double getDiemNk1() { return toDouble(nk1); }
    public void setDiemNk1(Double v) { this.nk1 = toBigDecimal(v); }

    public Double getDiemNk2() { return toDouble(nk2); }
    public void setDiemNk2(Double v) { this.nk2 = toBigDecimal(v); }
}