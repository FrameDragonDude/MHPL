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
    public ArrayList<ToHopMonDTO> ToanBoDanhSach(Connection conn) throws SQLException {
        // tạo các công cụ truy vấn
        Statement st = conn.createStatement();
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
    public ArrayList<ToHopMonDTO> TimKiem_MaTH_TenTH(Connection conn, String querry) throws SQLException {
        String sql = "SELECT * FROM xt_tohop_monthi"
                + "WHERE matohop=? or tentohop=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, querry);
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
}
