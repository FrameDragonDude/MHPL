/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

/**
 *
 * @author KIET
 */
public class NganhDTO {
    private int idnganh;
    private String manganh;
    private String tennganh;
    private String n_tohopmon;
    private int n_chitiet;
    private int n_diemsan;
    private int n_diemtrungtuyen;
    private String n_tuyenthang;
    private String n_dgnl;
    private String n_thpt;
    private String n_vsat;
    private int sl_xtt;
    private int sl_dgnl;
    private int sl_vsat;
    private String sl_thpt;

    public NganhDTO(int idnganh, String manganh, String tennganh, String n_tohopmon, int n_chitiet, int n_diemsan, int n_diemtrungtuyen, String n_tuyenthang, String n_dgnl, String n_thpt, String n_vsat, int sl_xtt, int sl_dgnl, int sl_vsat, String sl_thpt) {
        this.idnganh = idnganh;
        this.manganh = manganh;
        this.tennganh = tennganh;
        this.n_tohopmon = n_tohopmon;
        this.n_chitiet = n_chitiet;
        this.n_diemsan = n_diemsan;
        this.n_diemtrungtuyen = n_diemtrungtuyen;
        this.n_tuyenthang = n_tuyenthang;
        this.n_dgnl = n_dgnl;
        this.n_thpt = n_thpt;
        this.n_vsat = n_vsat;
        this.sl_xtt = sl_xtt;
        this.sl_dgnl = sl_dgnl;
        this.sl_vsat = sl_vsat;
        this.sl_thpt = sl_thpt;
    }

    public int getIdnganh() {
        return idnganh;
    }

    public void setIdnganh(int idnganh) {
        this.idnganh = idnganh;
    }

    public String getManganh() {
        return manganh;
    }

    public void setManganh(String manganh) {
        this.manganh = manganh;
    }

    public String getTennganh() {
        return tennganh;
    }

    public void setTennganh(String tennganh) {
        this.tennganh = tennganh;
    }

    public String getN_tohopmon() {
        return n_tohopmon;
    }

    public void setN_tohopmon(String n_tohopmon) {
        this.n_tohopmon = n_tohopmon;
    }

    public int getN_chitiet() {
        return n_chitiet;
    }

    public void setN_chitiet(int n_chitiet) {
        this.n_chitiet = n_chitiet;
    }

    public int getN_diemsan() {
        return n_diemsan;
    }

    public void setN_diemsan(int n_diemsan) {
        this.n_diemsan = n_diemsan;
    }

    public int getN_diemtrungtuyen() {
        return n_diemtrungtuyen;
    }

    public void setN_diemtrungtuyen(int n_diemtrungtuyen) {
        this.n_diemtrungtuyen = n_diemtrungtuyen;
    }

    public String getN_tuyenthang() {
        return n_tuyenthang;
    }

    public void setN_tuyenthang(String n_tuyenthang) {
        this.n_tuyenthang = n_tuyenthang;
    }

    public String getN_dgnl() {
        return n_dgnl;
    }

    public void setN_dgnl(String n_dgnl) {
        this.n_dgnl = n_dgnl;
    }

    public String getN_thpt() {
        return n_thpt;
    }

    public void setN_thpt(String n_thpt) {
        this.n_thpt = n_thpt;
    }

    public String getN_vsat() {
        return n_vsat;
    }

    public void setN_vsat(String n_vsat) {
        this.n_vsat = n_vsat;
    }

    public int getSl_xtt() {
        return sl_xtt;
    }

    public void setSl_xtt(int sl_xtt) {
        this.sl_xtt = sl_xtt;
    }

    public int getSl_dgnl() {
        return sl_dgnl;
    }

    public void setSl_dgnl(int sl_dgnl) {
        this.sl_dgnl = sl_dgnl;
    }

    public int getSl_vsat() {
        return sl_vsat;
    }

    public void setSl_vsat(int sl_vsat) {
        this.sl_vsat = sl_vsat;
    }

    public String getSl_thpt() {
        return sl_thpt;
    }

    public void setSl_thpt(String sl_thpt) {
        this.sl_thpt = sl_thpt;
    }
    
}
