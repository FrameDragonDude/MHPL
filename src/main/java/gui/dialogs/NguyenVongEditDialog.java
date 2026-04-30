package gui.dialogs;

import dto.NguyenVongDTO;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class NguyenVongEditDialog extends JDialog {
    private final JTextField txtCccd = new JTextField(24);
    private final JTextField txtThuTuNV = new JTextField(24);
    private final JTextField txtMaTruong = new JTextField(24);
    private final JTextField txtTenTruong = new JTextField(24);
    private final JTextField txtMaXetTuyen = new JTextField(24);
    private final JTextField txtTenMaXetTuyen = new JTextField(24);
    private final JTextField txtNguyenVongThang = new JTextField(24);
    private NguyenVongDTO result;

    private NguyenVongEditDialog(Component parent, NguyenVongDTO dto, boolean isReadOnly) {
        super(SwingUtilities.getWindowAncestor(parent), "Nguyện vọng", ModalityType.APPLICATION_MODAL);
        this.result = null;

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("CCCD:"));
        form.add(txtCccd);
        form.add(new JLabel("Thứ tự NV:"));
        form.add(txtThuTuNV);
        form.add(new JLabel("Mã trường:"));
        form.add(txtMaTruong);
        form.add(new JLabel("Tên trường:"));
        form.add(txtTenTruong);
        form.add(new JLabel("Mã xét tuyển:"));
        form.add(txtMaXetTuyen);
        form.add(new JLabel("Tên mã xét tuyển:"));
        form.add(txtTenMaXetTuyen);
        form.add(new JLabel("Nguyện vọng tháng:"));
        form.add(txtNguyenVongThang);

        setContentPane(form);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 280);
        setLocationRelativeTo(parent);

        if (dto != null) {
            txtCccd.setText(dto.getCccd() != null ? dto.getCccd() : "");
            txtThuTuNV.setText(dto.getThuTuNV() != null ? dto.getThuTuNV().toString() : "");
            txtMaTruong.setText(dto.getMaTruong() != null ? dto.getMaTruong() : "");
            txtTenTruong.setText(dto.getTenTruong() != null ? dto.getTenTruong() : "");
            txtMaXetTuyen.setText(dto.getMaXetTuyen() != null ? dto.getMaXetTuyen() : "");
            txtTenMaXetTuyen.setText(dto.getTenMaXetTuyen() != null ? dto.getTenMaXetTuyen() : "");
            txtNguyenVongThang.setText(dto.getNguyenVongThang() != null ? dto.getNguyenVongThang() : "");
        }

        if (isReadOnly) {
            txtCccd.setEditable(false);
            txtThuTuNV.setEditable(false);
            txtMaTruong.setEditable(false);
            txtTenTruong.setEditable(false);
            txtMaXetTuyen.setEditable(false);
            txtTenMaXetTuyen.setEditable(false);
            txtNguyenVongThang.setEditable(false);
        }

        int choice = JOptionPane.showConfirmDialog(parent, form, "Nguyện vọng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            NguyenVongDTO newDto = new NguyenVongDTO();
            if (dto != null && dto.getId() != null) {
                newDto.setId(dto.getId());
            }
            newDto.setCccd(txtCccd.getText());
            try {
                newDto.setThuTuNV(Integer.parseInt(txtThuTuNV.getText().trim()));
            } catch (NumberFormatException e) {
                newDto.setThuTuNV(0);
            }
            newDto.setMaTruong(txtMaTruong.getText());
            newDto.setTenTruong(txtTenTruong.getText());
            newDto.setMaXetTuyen(txtMaXetTuyen.getText());
            newDto.setTenMaXetTuyen(txtTenMaXetTuyen.getText());
            newDto.setNguyenVongThang(txtNguyenVongThang.getText());
            this.result = newDto;
        }
        this.dispose();
    }

    public static NguyenVongDTO showDialog(Component parent, NguyenVongDTO dto, boolean isReadOnly) {
        NguyenVongEditDialog dialog = new NguyenVongEditDialog(parent, dto, isReadOnly);
        return dialog.result;
    }
}
