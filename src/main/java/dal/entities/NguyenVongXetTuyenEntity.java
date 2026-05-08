package dal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Hibernate entity for xt_nguyenvongxettuyen table.
 * Maps admission wish records for candidates.
 */
@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVongXetTuyenEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idnv")
	private Integer idnv;

	@Column(name = "nn_cccd", length = 45, nullable = false)
	private String nnCccd;

	@Column(name = "nv_manganh", length = 45, nullable = false)
	private String nvManganh;

	@Column(name = "nv_tt", nullable = false)
	private Integer nvTt;

	@Column(name = "diem_thxt", precision = 10, scale = 5)
	private BigDecimal diemThxt;

	@Column(name = "diem_utqd", precision = 10, scale = 5)
	private BigDecimal diemUtqd;

	@Column(name = "diem_cong", precision = 6, scale = 2)
	private BigDecimal diemCong;

	@Column(name = "diem_xettuyen", precision = 10, scale = 5)
	private BigDecimal diemXettuyen;

	@Column(name = "nv_ketqua", length = 45)
	private String nvKetqua;

	@Column(name = "nv_keys", length = 45, unique = true)
	private String nvKeys;

	@Column(name = "tt_phuongthuc", length = 45)
	private String ttPhuongthuc;

	@Column(name = "tt_thm", length = 45)
	private String ttThm;

	// Association to NganhEntity (lazy loaded)
	@ManyToOne
	@JoinColumn(name = "nv_manganh", referencedColumnName = "manganh", insertable = false, updatable = false)
	private NganhEntity nganh;

	// Constructors
	public NguyenVongXetTuyenEntity() {
	}

	public NguyenVongXetTuyenEntity(String nnCccd, String nvManganh, Integer nvTt) {
		this.nnCccd = nnCccd;
		this.nvManganh = nvManganh;
		this.nvTt = nvTt;
	}

	// Getters and Setters
	public Integer getIdnv() {
		return idnv;
	}

	public void setIdnv(Integer idnv) {
		this.idnv = idnv;
	}

	public String getNnCccd() {
		return nnCccd;
	}

	public void setNnCccd(String nnCccd) {
		this.nnCccd = nnCccd;
	}

	public String getNvManganh() {
		return nvManganh;
	}

	public void setNvManganh(String nvManganh) {
		this.nvManganh = nvManganh;
	}

	public Integer getNvTt() {
		return nvTt;
	}

	public void setNvTt(Integer nvTt) {
		this.nvTt = nvTt;
	}

	public BigDecimal getDiemThxt() {
		return diemThxt;
	}

	public void setDiemThxt(BigDecimal diemThxt) {
		this.diemThxt = diemThxt;
	}

	public BigDecimal getDiemUtqd() {
		return diemUtqd;
	}

	public void setDiemUtqd(BigDecimal diemUtqd) {
		this.diemUtqd = diemUtqd;
	}

	public BigDecimal getDiemCong() {
		return diemCong;
	}

	public void setDiemCong(BigDecimal diemCong) {
		this.diemCong = diemCong;
	}

	public BigDecimal getDiemXettuyen() {
		return diemXettuyen;
	}

	public void setDiemXettuyen(BigDecimal diemXettuyen) {
		this.diemXettuyen = diemXettuyen;
	}

	public String getNvKetqua() {
		return nvKetqua;
	}

	public void setNvKetqua(String nvKetqua) {
		this.nvKetqua = nvKetqua;
	}

	public String getNvKeys() {
		return nvKeys;
	}

	public void setNvKeys(String nvKeys) {
		this.nvKeys = nvKeys;
	}

	public String getTtPhuongthuc() {
		return ttPhuongthuc;
	}

	public void setTtPhuongthuc(String ttPhuongthuc) {
		this.ttPhuongthuc = ttPhuongthuc;
	}

	public String getTtThm() {
		return ttThm;
	}

	public void setTtThm(String ttThm) {
		this.ttThm = ttThm;
	}

	public NganhEntity getNganh() {
		return nganh;
	}

	public void setNganh(NganhEntity nganh) {
		this.nganh = nganh;
	}
}
