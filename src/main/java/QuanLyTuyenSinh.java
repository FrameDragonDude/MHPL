import gui.LoginFrame;
import javax.swing.SwingUtilities;

public class QuanLyTuyenSinh {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
