package gui.dialogs;

import dto.SubjectCombinationDTO;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class SubjectCombinationEditDialog extends JDialog {
    private final JTextField txtMaToHop = new JTextField();
    private final JTextField txtMon1 = new JTextField();
    private final JTextField txtMon2 = new JTextField();
    private final JTextField txtMon3 = new JTextField();
    private final JTextField txtTenToHop = new JTextField();

    private SubjectCombinationDTO result;

    private SubjectCombinationEditDialog(Frame owner, SubjectCombinationDTO source, boolean editMode) {
        super(owner, editMode ? "Sửa tổ hợp môn" : "Thêm tổ hợp môn", true);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        form.add(new JLabel("Mã tổ hợp"));
        form.add(txtMaToHop);

        form.add(new JLabel("Môn 1"));
        form.add(txtMon1);

        form.add(new JLabel("Môn 2"));
        form.add(txtMon2);

        form.add(new JLabel("Môn 3"));
        form.add(txtMon3);

        form.add(new JLabel("Tên tổ hợp"));
        form.add(txtTenToHop);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        JPanel actions = new JPanel();
        actions.add(btnSave);
        actions.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        if (source != null) {
            txtMaToHop.setText(safe(source.getMaToHop()));
            txtMon1.setText(safe(source.getMon1()));
            txtMon2.setText(safe(source.getMon2()));
            txtMon3.setText(safe(source.getMon3()));
            txtTenToHop.setText(safe(source.getTenToHop()));
        }

        btnSave.addActionListener(e -> onSave(source));
        btnCancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        pack();
        setSize(520, Math.max(getHeight(), 260));
        setLocationRelativeTo(owner);
    }

    private void onSave(SubjectCombinationDTO source) {
        String maToHop = txtMaToHop.getText() == null ? "" : txtMaToHop.getText().trim();
        String mon1 = txtMon1.getText() == null ? "" : txtMon1.getText().trim();
        String mon2 = txtMon2.getText() == null ? "" : txtMon2.getText().trim();
        String mon3 = txtMon3.getText() == null ? "" : txtMon3.getText().trim();
        String tenToHop = txtTenToHop.getText() == null ? "" : txtTenToHop.getText().trim();

        if (maToHop.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã tổ hợp không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ba môn không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SubjectCombinationDTO dto = new SubjectCombinationDTO();
        if (source != null) {
            dto.setId(source.getId());
        }
        dto.setMaToHop(maToHop);
        dto.setMon1(mon1);
        dto.setMon2(mon2);
        dto.setMon3(mon3);
        dto.setTenToHop(tenToHop.isEmpty() ? null : tenToHop);

        result = dto;
        dispose();
    }

    public static SubjectCombinationDTO showDialog(java.awt.Component parent, SubjectCombinationDTO source, boolean editMode) {
        Frame owner = JOptionPane.getFrameForComponent(parent);
        SubjectCombinationEditDialog dialog = new SubjectCombinationEditDialog(owner, source, editMode);
        dialog.setVisible(true);
        return dialog.result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
