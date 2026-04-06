/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import dal.DBConnection;
import dto.ToHopMonDTO;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
/**
 *
 * @author KIET
 */
public class ToHopMonDAO {
    private Connection conn;

    public ToHopMonDAO(Connection conn) {
        this.conn = conn;
    }
    
    public ArrayList<ToHopMonDTO> ToanBoDanhSach() throws SQLException {
        // tạo các công cụ truy vấn
        Statement st = this.conn.createStatement();
        String sql = "SELECT * FROM xt_tohop_monthi";
        ResultSet rs = st.executeQuery(sql);
        ArrayList<ToHopMonDTO> danhsach = new ArrayList<>();
        // thực hiện lấy danh sách
        while(rs.next()) {
            ToHopMonDTO tmp = new ToHopMonDTO(rs.getInt("idtohop"), rs.getString("matohop"), rs.getString("mon1"),
                    rs.getString("mon2"), rs.getString("mon3"), rs.getString("tentohop"));
            danhsach.add(tmp);
        }
        return danhsach;
    }
    public ArrayList<ToHopMonDTO> TimKiem_MaTH_TenTH(String querry) throws SQLException {
        String sql = "SELECT * FROM xt_tohop_monthi"
                + "WHERE matohop=? or tentohop=?";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ps.setString(1, querry);
        ps.setString(2, querry);
        ResultSet rs = ps.executeQuery();
        ArrayList<ToHopMonDTO> danhsach = new ArrayList<>();
        // thực hiện lấy danh sách
        while(rs.next()) {
            ToHopMonDTO tmp = new ToHopMonDTO(rs.getInt("idtohop"), rs.getString("matohop"), rs.getString("mon1"),
                    rs.getString("mon2"), rs.getString("mon3"), rs.getString("tentohop"));
            danhsach.add(tmp);
        }
        return danhsach;
    }
    public ToHopMonDTO Lay1ToHopMon(String matohop) throws SQLException {
        String sql = "SELECT * FROM xt_tohop_monthi"
                + "WHERE matohop=?";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ps.setString(1, matohop);
        ResultSet rs = ps.executeQuery();
        ToHopMonDTO tmp = new ToHopMonDTO(rs.getInt("idtohop"), rs.getString("matohop"), rs.getString("mon1"),
                    rs.getString("mon2"), rs.getString("mon3"), rs.getString("tentohop"));
        return tmp;
    }
    public String TaoMaTuDong() throws SQLException { // hàm để tạo mã tổ hợp tự động
        // cú pháp mã là THMxxxxx;
        
        // tìm stt lớn nhất trong bảng sau đó +1
        String sql = "SELECT MAX(idtohop) FROM xt_tohop_monthi";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int max = rs.getInt("matohop");
        max++;
        // biến stt lớn nhất thành chuỗi sau đó nối chuỗi đến khi đủ 5 ký tự
        String strmax = max + "";
        while(strmax.length()<5) {
            strmax = "0" + strmax;
        }
        // thêm đầu THM và trước chuỗi để tạo mã cho tổ hợp môn
        strmax = "THM" + strmax;
        return strmax;
    }
    public void Them(String matohop,
            String mon1, String mon2, String mon3, String tentohop) throws SQLException {
        String sql = "INSERT INTO xt_tohop_monthi(matohop, mon1, mon2, mon3, tentohop)"
                + "VALUES(?, ?, ?, ?, ?);";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ps.setString(1, matohop);
        ps.setString(2, mon1);
        ps.setString(3, mon2);
        ps.setString(4, mon3);
        ps.setString(5, tentohop);
        ps.executeUpdate();
    }
    public void Xoa(String matohop) throws SQLException {
        String sql = "DELETE FROM xt_tohop_monthi WHERE matohop=?";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ps.setString(1, matohop);
        ps.executeUpdate();
    }
    public void Sua(String matohop, String mon1, String mon2,
            String mon3, String tentohop) throws SQLException {
        String sql = "UPDATE xt_tohop_monthi SET"
                + "mon1=?, mon2=?, mon3=?, tentohop=?"
                + "WHERE matohop=?";
        PreparedStatement ps = this.conn.prepareStatement(sql);
        ps.executeUpdate();
    }
}
