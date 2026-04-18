import gui.LoginFrame;
import javax.swing.SwingUtilities;

public class QuanLyTuyenSinh {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Hiển thị LoginFrame trước
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
