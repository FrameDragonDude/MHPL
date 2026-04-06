/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bus;
import java.sql.Connection;
import dal.dao.ToHopMonDAO;
import java.sql.SQLException;
/**
 *
 * @author KIET
 */
public class ToHopMonBUS {
    private Connection conn;
    private ToHopMonDAO dao;

    public ToHopMonBUS(Connection conn) {
        this.conn = conn;
        this.dao = new ToHopMonDAO(conn);
    }
//    public void ToanBoDanhSach()
    public void Them(String mon1, String mon2, String mon3, String tentohop) throws SQLException {
        String matohop = this.dao.TaoMaTuDong();
        this.dao.Them(matohop, mon1, mon2, mon3, tentohop);
    }
    public void Xoa(String matohop) throws SQLException {
        this.dao.Xoa(matohop);
    }
    public void Sua(String matohop, String mon1, String mon2, String mon3, String tentohop) throws SQLException {
        this.dao.Sua(matohop, mon1, mon2, mon3, tentohop);
    }
}
