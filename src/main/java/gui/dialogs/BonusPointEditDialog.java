package gui.dialogs;

import dto.BonusPointDTO;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class BonusPointEditDialog extends JDialog {
    private final JTextField txtCccd = new JTextField(24);
    private final JTextField txtPhuongThuc = new JTextField(24);
    private final JTextField txtDiemCC = new JTextField(24);
    private final JTextField txtDiemUtxt = new JTextField(24);
    private final JTextField txtDiemTong = new JTextField(24);
    private final JTextField txtGhiChu = new JTextField(24);
    private BonusPointDTO result;

    private BonusPointEditDialog(Component parent, BonusPointDTO dto, boolean isReadOnly) {
        super(SwingUtilities.getWindowAncestor(parent), "Điểm cộng", ModalityType.APPLICATION_MODAL);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("CCCD:"));
        form.add(txtCccd);
        form.add(new JLabel("Chứng chỉ ngoại ngữ:"));
        form.add(txtPhuongThuc);
        form.add(new JLabel("Điểm:"));
        form.add(txtDiemCC);
        form.add(new JLabel("Điểm quy đổi:"));
        form.add(txtDiemUtxt);
        form.add(new JLabel("Điểm cộng:"));
        form.add(txtDiemTong);
        form.add(new JLabel("Ghi chú:"));
        form.add(txtGhiChu);

        setContentPane(form);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(620, 280);
        setLocationRelativeTo(parent);

        if (dto != null) {
            txtCccd.setText(dto.getTsCccd() != null ? dto.getTsCccd() : "");
            txtPhuongThuc.setText(dto.getPhuongThuc() != null ? dto.getPhuongThuc() : "");
            txtDiemCC.setText(dto.getDiemCC() != null ? dto.getDiemCC().toString() : "");
            txtDiemUtxt.setText(dto.getDiemUtxt() != null ? dto.getDiemUtxt().toString() : "");
            txtDiemTong.setText(dto.getDiemTong() != null ? dto.getDiemTong().toString() : "");
            txtGhiChu.setText(dto.getGhiChu() != null ? dto.getGhiChu() : "");
        }

        if (isReadOnly) {
            txtCccd.setEditable(false);
            txtPhuongThuc.setEditable(false);
            txtDiemCC.setEditable(false);
            txtDiemUtxt.setEditable(false);
            txtDiemTong.setEditable(false);
            txtGhiChu.setEditable(false);
        }

        int choice = JOptionPane.showConfirmDialog(parent, form, "Điểm cộng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            BonusPointDTO newDto = new BonusPointDTO();
            if (dto != null && dto.getId() != null) {
                newDto.setId(dto.getId());
            }
            newDto.setTsCccd(txtCccd.getText());
            newDto.setPhuongThuc(txtPhuongThuc.getText());
            newDto.setDiemCC(parseDouble(txtDiemCC.getText()));
            newDto.setDiemUtxt(parseDouble(txtDiemUtxt.getText()));
            newDto.setDiemTong(parseDouble(txtDiemTong.getText()));
            newDto.setGhiChu(txtGhiChu.getText());
            this.result = newDto;
        }
        this.dispose();
    }

    public static BonusPointDTO showDialog(Component parent, BonusPointDTO dto, boolean isReadOnly) {
        BonusPointEditDialog dialog = new BonusPointEditDialog(parent, dto, isReadOnly);
        return dialog.result;
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}