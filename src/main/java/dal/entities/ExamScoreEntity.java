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

	@Column(name = "NK1")
	private BigDecimal nk1;

	@Column(name = "NK2")
	private BigDecimal nk2;

	@Column(name = "N1_THI")
	private BigDecimal n1Thi;

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

	public String getDPhuongThuc() {
		return dPhuongThuc;
	}

	public String getPhuongThuc() {
		return dPhuongThuc;
	}

	public void setPhuongThuc(String phuongThuc) {
		this.dPhuongThuc = phuongThuc;
	}

	public BigDecimal getTo() {
		return to;
	}

	public Double getDiemTo() {
		return to == null ? null : to.doubleValue();
	}

	public void setDiemTo(Double value) {
		this.to = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getVa() {
		return va;
	}

	public Double getDiemVa() {
		return va == null ? null : va.doubleValue();
	}

	public void setDiemVa(Double value) {
		this.va = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getLi() {
		return li;
	}

	public Double getDiemLi() {
		return li == null ? null : li.doubleValue();
	}

	public void setDiemLi(Double value) {
		this.li = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getHo() {
		return ho;
	}

	public Double getDiemHo() {
		return ho == null ? null : ho.doubleValue();
	}

	public void setDiemHo(Double value) {
		this.ho = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getSi() {
		return si;
	}

	public Double getDiemSi() {
		return si == null ? null : si.doubleValue();
	}

	public void setDiemSi(Double value) {
		this.si = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getSu() {
		return su;
	}

	public Double getDiemSu() {
		return su == null ? null : su.doubleValue();
	}

	public void setDiemSu(Double value) {
		this.su = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getDi() {
		return di;
	}

	public Double getDiemDi() {
		return di == null ? null : di.doubleValue();
	}

	public void setDiemDi(Double value) {
		this.di = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getN1Cc() {
		return n1Cc;
	}

	public Double getDiemN1Cc() {
		return n1Cc == null ? null : n1Cc.doubleValue();
	}

	public void setDiemN1Cc(Double value) {
		this.n1Cc = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getKtpl() {
		return ktpl;
	}

	public Double getDiemKtpl() {
		return ktpl == null ? null : ktpl.doubleValue();
	}

	public void setDiemKtpl(Double value) {
		this.ktpl = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getTi() {
		return ti;
	}

	public Double getDiemTi() {
		return ti == null ? null : ti.doubleValue();
	}

	public void setDiemTi(Double value) {
		this.ti = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getCncn() {
		return cncn;
	}

	public Double getDiemCncn() {
		return cncn == null ? null : cncn.doubleValue();
	}

	public void setDiemCncn(Double value) {
		this.cncn = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getCnnn() {
		return cnnn;
	}

	public Double getDiemCnnn() {
		return cnnn == null ? null : cnnn.doubleValue();
	}

	public void setDiemCnnn(Double value) {
		this.cnnn = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getNk1() {
		return nk1;
	}

	public Double getDiemNk1() {
		return nk1 == null ? null : nk1.doubleValue();
	}

	public void setDiemNk1(Double value) {
		this.nk1 = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getNk2() {
		return nk2;
	}

	public Double getDiemNk2() {
		return nk2 == null ? null : nk2.doubleValue();
	}

	public void setDiemNk2(Double value) {
		this.nk2 = value == null ? null : BigDecimal.valueOf(value);
	}

	public BigDecimal getN1Thi() {
		return n1Thi;
	}
}