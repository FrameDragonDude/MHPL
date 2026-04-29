package dto;

public class NganhTuyenSinhDTO {
    private Integer id;
    private String maXetTuyen;
    private String tenNganh;
    private String chuongTrinh;
    private String nguongDauVao; // store as string to allow empty/decimal
    private Integer chiTieuChot;

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
}
