package dal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_thisinhxettuyen25")
public class CandidateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idthisinh")
	private Integer id;

	@Column(name = "cccd")
	private String cccd;

	@Column(name = "sobaodanh")
	private String soBaoDanh;

	@Column(name = "ho")
	private String ho;

	@Column(name = "ten")
	private String ten;

	@Column(name = "ngay_sinh")
	private String ngaySinh;

	@Column(name = "dien_thoai")
	private String dienThoai;

	@Column(name = "gioi_tinh")
	private String gioiTinh;

	@Column(name = "email")
	private String email;

	@Column(name = "noi_sinh")
	private String noiSinh;

	@Column(name = "doi_tuong")
	private String doiTuong;

	@Column(name = "khu_vuc")
	private String khuVuc;

	public Integer getId() {
		return id;
	}

	public Integer getIdThisinh() {
		return id;
	}

	public void setIdThisinh(Integer idThisinh) {
		this.id = idThisinh;
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

	public String getFullName() {
		String hoSafe = ho == null ? "" : ho.trim();
		String tenSafe = ten == null ? "" : ten.trim();
		return (hoSafe + " " + tenSafe).trim();
	}
}