package dto;

import java.math.BigDecimal;

public class NguyenVongXetTuyenDTO {
	private Integer idnv;
	private String nnCccd;
	private String nvManganh;
	private Integer nvTt;
	private BigDecimal diemThxt;
	private BigDecimal diemUtqd;
	private BigDecimal diemCong;
	private BigDecimal diemXettuyen;
	private String nvKetqua;
	private String nvKeys;
	private String ttPhuongthuc;
	private String ttThm;

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
}
