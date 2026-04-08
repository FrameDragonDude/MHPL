
import dal.dao.ToHopMonDAO;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author KIET
 */
public class daoTester {
    public static void main(String[] args) {
        ToHopMonDAO dao = new ToHopMonDAO();
//        dao.Sua("tohop_test", "Toan", "Tin học", "Bóng đá", "tổ hợp thử nghiệm");
//        dao.Xoa();
        dao.LayDanhSach();
    }
}
