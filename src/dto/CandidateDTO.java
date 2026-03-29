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
}
