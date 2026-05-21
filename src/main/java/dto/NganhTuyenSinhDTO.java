package dto;

public class NganhTuyenSinhDTO {
    private Integer id;
    private String maXetTuyen;
    private String tenNganh;
    private String chuongTrinh;
    private String nguongDauVao; // store as string to allow empty/decimal
    private Integer chiTieuChot;
    private String nTuyenthang;
    private String nDgnl;
    private String nThpt;
    private String nVsat;
    private String diemTrungTuyen;
    private String phuongThuc;
    private Integer soThiSinhDangKy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaXetTuyen() {
        return maXetTuyen;
    }

    public void setMaXetTuyen(String maXetTuyen) {
        this.maXetTuyen = maXetTuyen;
    }

    public String getTenNganh() {
        return tenNganh;
    }

    public void setTenNganh(String tenNganh) {
        this.tenNganh = tenNganh;
    }

    public String getChuongTrinh() {
        return chuongTrinh;
    }

    public void setChuongTrinh(String chuongTrinh) {
        this.chuongTrinh = chuongTrinh;
    }

    public String getNguongDauVao() {
        return nguongDauVao;
    }

    public void setNguongDauVao(String nguongDauVao) {
        this.nguongDauVao = nguongDauVao;
    }

    public Integer getChiTieuChot() {
        return chiTieuChot;
    }

    public void setChiTieuChot(Integer chiTieuChot) {
        this.chiTieuChot = chiTieuChot;
    }

    public String getnTuyenthang() {
        return nTuyenthang;
    }

    public void setnTuyenthang(String nTuyenthang) {
        this.nTuyenthang = nTuyenthang;
    }

    public String getnDgnl() {
        return nDgnl;
    }

    public void setnDgnl(String nDgnl) {
        this.nDgnl = nDgnl;
    }

    public String getnThpt() {
        return nThpt;
    }

    public void setnThpt(String nThpt) {
        this.nThpt = nThpt;
    }

    public String getnVsat() {
        return nVsat;
    }

    public void setnVsat(String nVsat) {
        this.nVsat = nVsat;
    }

    public String getDiemTrungTuyen() {
        return diemTrungTuyen;
    }

    public void setDiemTrungTuyen(String diemTrungTuyen) {
        this.diemTrungTuyen = diemTrungTuyen;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public Integer getSoThiSinhDangKy() {
        return soThiSinhDangKy;
    }

    public void setSoThiSinhDangKy(Integer soThiSinhDangKy) {
        this.soThiSinhDangKy = soThiSinhDangKy;
    }
}
