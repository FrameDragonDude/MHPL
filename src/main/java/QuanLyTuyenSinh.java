import gui.MainFrame;
import javax.swing.SwingUtilities;

public class QuanLyTuyenSinh {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
