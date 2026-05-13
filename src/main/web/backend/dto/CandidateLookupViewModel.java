package backend.dto;

public class CandidateLookupViewModel {
	private String username;
	private String cccd;
	private String fullName;
	private String ngaySinh;
	private boolean searched;
	private boolean found;
	private boolean authenticated;
	private boolean admitted;
	private java.util.List<AdmissionDto> admissions;
	private String message;
	private String majorCode;
	private String majorName;
	private String score;
	private String combination;
	private String method;
	private String resultLabel;
	// Điểm chi tiết
	private String diemThxt;
	private String diemUtqd;
	private String diemCong;
	private String diemXettuyen;
	private String diemSan;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCccd() {
		return cccd;
	}

	public void setCccd(String cccd) {
		this.cccd = cccd;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getNgaySinh() {
		return ngaySinh;
	}

	public void setNgaySinh(String ngaySinh) {
		this.ngaySinh = ngaySinh;
	}

	public boolean isSearched() {
		return searched;
	}

	public void setSearched(boolean searched) {
		this.searched = searched;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isAdmitted() {
		return admitted;
	}

	public void setAdmitted(boolean admitted) {
		this.admitted = admitted;
	}

	public java.util.List<AdmissionDto> getAdmissions() { return admissions; }
	public void setAdmissions(java.util.List<AdmissionDto> admissions) { this.admissions = admissions; }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMajorCode() {
		return majorCode;
	}

	public void setMajorCode(String majorCode) {
		this.majorCode = majorCode;
	}

	public String getMajorName() {
		return majorName;
	}

	public void setMajorName(String majorName) {
		this.majorName = majorName;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getCombination() {
		return combination;
	}

	public void setCombination(String combination) {
		this.combination = combination;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getResultLabel() {
		return resultLabel;
	}

	public void setResultLabel(String resultLabel) {
		this.resultLabel = resultLabel;
	}

	public String getDiemThxt() {
		return diemThxt;
	}

	public void setDiemThxt(String diemThxt) {
		this.diemThxt = diemThxt;
	}

	public String getDiemUtqd() {
		return diemUtqd;
	}

	public void setDiemUtqd(String diemUtqd) {
		this.diemUtqd = diemUtqd;
	}

	public String getDiemCong() {
		return diemCong;
	}

	public void setDiemCong(String diemCong) {
		this.diemCong = diemCong;
	}

	public String getDiemXettuyen() {
		return diemXettuyen;
	}

	public void setDiemXettuyen(String diemXettuyen) {
		this.diemXettuyen = diemXettuyen;
	}

	public String getDiemSan() {
		return diemSan;
	}

	public void setDiemSan(String diemSan) {
		this.diemSan = diemSan;
	}
}
