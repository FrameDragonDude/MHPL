package gui.dialogs;

import dto.BonusPointDTO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class BonusPointEditDialog extends JDialog {
    private final JTextField txtCccd = new JTextField(24);
    private final JTextField txtMaNganh = new JTextField(24);
    private final JTextField txtMaToHop = new JTextField(24);
    private final JTextField txtDiemCC = new JTextField(24);
    private final JTextField txtDiemUtxt = new JTextField(24);
    private final JTextField txtDiemTong = new JTextField(24);
    private BonusPointDTO result;

    private BonusPointEditDialog(Component parent, BonusPointDTO dto, boolean isReadOnly) {
        super(SwingUtilities.getWindowAncestor(parent), "Thông tin điểm cộng xét tuyển", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCccd);
        formPanel.add(new JLabel("Mã ngành:"));
        formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Tổ hợp môn:"));
        formPanel.add(txtMaToHop);
        formPanel.add(new JLabel("Điểm cộng TA:"));
        formPanel.add(txtDiemCC);
        formPanel.add(new JLabel("Điểm cộng UTXT:"));
        formPanel.add(txtDiemUtxt);
        formPanel.add(new JLabel("Tổng điểm cộng:"));
        formPanel.add(txtDiemTong);

        if (dto != null) {
            txtCccd.setText(dto.getTsCccd() != null ? dto.getTsCccd() : "");
            txtMaNganh.setText(dto.getMaNganh() != null ? dto.getMaNganh() : "");
            txtMaToHop.setText(dto.getMaToHop() != null ? dto.getMaToHop() : "");
            txtDiemCC.setText(dto.getDiemCC() != null ? dto.getDiemCC().toString() : "0.0");
            txtDiemUtxt.setText(dto.getDiemUtxt() != null ? dto.getDiemUtxt().toString() : "0.0");
            txtDiemTong.setText(dto.getDiemTong() != null ? dto.getDiemTong().toString() : "0.0");
        }

        if (isReadOnly) {
            txtCccd.setEditable(false);
            txtMaNganh.setEditable(false);
            txtMaToHop.setEditable(false);
            txtDiemCC.setEditable(false);
            txtDiemUtxt.setEditable(false);
            txtDiemTong.setEditable(false);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton btnSave = new JButton("Lưu cấu trúc");
        JButton btnCancel = new JButton("Hủy bỏ");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        btnSave.addActionListener(e -> {
            BonusPointDTO newDto = new BonusPointDTO();
            if (dto != null && dto.getId() != null) {
                newDto.setId(dto.getId());
            }
            newDto.setTsCccd(txtCccd.getText().trim());
            newDto.setMaNganh(txtMaNganh.getText().trim());
            newDto.setMaToHop(txtMaToHop.getText().trim());
            newDto.setDiemCC(parseDouble(txtDiemCC.getText()));
            newDto.setDiemUtxt(parseDouble(txtDiemUtxt.getText()));
            newDto.setDiemTong(parseDouble(txtDiemTong.getText()));
            this.result = newDto;
            this.dispose();
        });

        btnCancel.addActionListener(e -> {
            this.result = null;
            this.dispose();
        });

        if (isReadOnly) {
            btnSave.setVisible(false);
        }

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(480, 320);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public static BonusPointDTO showDialog(Component parent, BonusPointDTO dto, boolean isReadOnly) {
        BonusPointEditDialog dialog = new BonusPointEditDialog(parent, dto, isReadOnly);
        dialog.setVisible(true);
        return dialog.result;
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}