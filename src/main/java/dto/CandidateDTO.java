package dto;

public class CandidateDTO {

	private int idThisinh;
	private String cccd;
	private String soBaoDanh;
	private String ho;
	private String ten;
	private String ngaySinh;
	private String dienThoai;
	private String gioiTinh;
	private String email;
	private String noiSinh;
	private String doiTuong;
	private String khuVuc;
	private Double diemTo;
	private Double diemVa;
	private Double diemLi;
	private Double diemHo;
	private Double diemSi;
	private Double diemSu;
	private Double diemDi;
	private Double diemNn;
	private Double diemKtpl;
	private Double diemTi;
	private Double diemCncn;
	private Double diemCnnn;
	private Double diemGdcd;
	private Double diemNk1;
	private Double diemNk2;
	private Double diemNk3;
	private Double diemNk4;
	private Double diemNk5;
	private Double diemNk6;
	private Double diemNk7;
	private Double diemNk8;
	private Double diemNk9;
	private Double diemNk10;
	private Double diemXetTotNghiep;
	private String maMonNn;
	private String chuongTrinh;
	private String danToc;
	private String maDanToc;

	public int getIdThisinh() {
		return idThisinh;
	}

	public void setIdThisinh(int idThisinh) {
		this.idThisinh = idThisinh;
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

	public String getHo() {
		return ho;
	}

	public void setHo(String ho) {
		this.ho = ho;
	}

	public String getTen() {
		return ten;
	}

	public void setTen(String ten) {
		this.ten = ten;
	}

	public String getNgaySinh() {
		return ngaySinh;
	}

	public void setNgaySinh(String ngaySinh) {
		this.ngaySinh = ngaySinh;
	}

	public String getDienThoai() {
		return dienThoai;
	}

	public void setDienThoai(String dienThoai) {
		this.dienThoai = dienThoai;
	}

	public String getGioiTinh() {
		return gioiTinh;
	}

	public void setGioiTinh(String gioiTinh) {
		this.gioiTinh = gioiTinh;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNoiSinh() {
		return noiSinh;
	}

	public void setNoiSinh(String noiSinh) {
		this.noiSinh = noiSinh;
	}

	public String getDoiTuong() {
		return doiTuong;
	}

	public void setDoiTuong(String doiTuong) {
		this.doiTuong = doiTuong;
	}

	public String getKhuVuc() {
		return khuVuc;
	}

	public void setKhuVuc(String khuVuc) {
		this.khuVuc = khuVuc;
	}

	public String getHoTen() {
		String hoText = ho == null ? "" : ho.trim();
		String tenText = ten == null ? "" : ten.trim();
		if (hoText.isEmpty()) {
			return tenText;
		}
		if (tenText.isEmpty()) {
			return hoText;
		}
		return hoText + " " + tenText;
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

	public Double getDiemNn() {
		return diemNn;
	}

	public void setDiemNn(Double diemNn) {
		this.diemNn = diemNn;
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

	public Double getDiemGdcd() {
		return diemGdcd;
	}

	public void setDiemGdcd(Double diemGdcd) {
		this.diemGdcd = diemGdcd;
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

	public Double getDiemNk3() {
		return diemNk3;
	}

	public void setDiemNk3(Double diemNk3) {
		this.diemNk3 = diemNk3;
	}

	public Double getDiemNk4() {
		return diemNk4;
	}

	public void setDiemNk4(Double diemNk4) {
		this.diemNk4 = diemNk4;
	}

	public Double getDiemNk5() {
		return diemNk5;
	}

	public void setDiemNk5(Double diemNk5) {
		this.diemNk5 = diemNk5;
	}

	public Double getDiemNk6() {
		return diemNk6;
	}

	public void setDiemNk6(Double diemNk6) {
		this.diemNk6 = diemNk6;
	}

	public Double getDiemNk7() {
		return diemNk7;
	}

	public void setDiemNk7(Double diemNk7) {
		this.diemNk7 = diemNk7;
	}

	public Double getDiemNk8() {
		return diemNk8;
	}

	public void setDiemNk8(Double diemNk8) {
		this.diemNk8 = diemNk8;
	}

	public Double getDiemNk9() {
		return diemNk9;
	}

	public void setDiemNk9(Double diemNk9) {
		this.diemNk9 = diemNk9;
	}

	public Double getDiemNk10() {
		return diemNk10;
	}

	public void setDiemNk10(Double diemNk10) {
		this.diemNk10 = diemNk10;
	}

	public Double getDiemXetTotNghiep() {
		return diemXetTotNghiep;
	}

	public void setDiemXetTotNghiep(Double diemXetTotNghiep) {
		this.diemXetTotNghiep = diemXetTotNghiep;
	}

	public String getMaMonNn() {
		return maMonNn;
	}

	public void setMaMonNn(String maMonNn) {
		this.maMonNn = maMonNn;
	}

	public String getChuongTrinh() {
		return chuongTrinh;
	}

	public void setChuongTrinh(String chuongTrinh) {
		this.chuongTrinh = chuongTrinh;
	}

	public String getDanToc() {
		return danToc;
	}

	public void setDanToc(String danToc) {
		this.danToc = danToc;
	}

	public String getMaDanToc() {
		return maDanToc;
	}

	public void setMaDanToc(String maDanToc) {
		this.maDanToc = maDanToc;
	}
}
