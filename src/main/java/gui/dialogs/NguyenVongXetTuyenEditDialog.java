package gui.dialogs;

import dto.NguyenVongXetTuyenDTO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class NguyenVongXetTuyenEditDialog {
	private JDialog dialog;
	private JTextField txtIdnv;
	private JTextField txtCccd;
	private JTextField txtManganh;
	private JTextField txtTt;
	private JTextField txtDiemThxt;
	private JTextField txtDiemUtqd;
	private JTextField txtDiemCong;
	private JTextField txtDiemXettuyen;
	private JTextField txtKetqua;
	private JTextField txtKeys;
	private JTextField txtPhuongthuc;
	private JTextField txtThm;
	private boolean accepted = false;

	public NguyenVongXetTuyenEditDialog(JFrame parent, NguyenVongXetTuyenDTO dto) {
		dialog = new JDialog(parent, "Nguyện vọng xét tuyển", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setSize(620, 520);
		SwingUtilities.invokeLater(() -> dialog.setLocationRelativeTo(parent));

		JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel fieldsPanel = buildFieldsPanel(dto);
		mainPanel.add(fieldsPanel, BorderLayout.CENTER);

		JPanel buttonPanel = buildButtonPanel();
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(mainPanel);
	}

	private JPanel buildFieldsPanel(NguyenVongXetTuyenDTO dto) {
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

		txtIdnv = createTextField(dto == null ? null : dto.getIdnv() != null ? dto.getIdnv().toString() : null, true);
		txtCccd = createTextField(dto == null ? null : dto.getNnCccd(), false);
		txtManganh = createTextField(dto == null ? null : dto.getNvManganh(), false);
		txtTt = createTextField(dto == null ? null : dto.getNvTt() != null ? dto.getNvTt().toString() : null, false);
		txtDiemThxt = createTextField(dto == null ? null : formatDecimal(dto.getDiemThxt()), false);
		txtDiemUtqd = createTextField(dto == null ? null : formatDecimal(dto.getDiemUtqd()), false);
		txtDiemCong = createTextField(dto == null ? null : formatDecimal(dto.getDiemCong()), false);
		txtDiemXettuyen = createTextField(dto == null ? null : formatDecimal(dto.getDiemXettuyen()), false);
		txtKetqua = createTextField(dto == null ? null : dto.getNvKetqua(), false);
		txtKeys = createTextField(dto == null ? null : dto.getNvKeys(), false);
		txtPhuongthuc = createTextField(dto == null ? null : dto.getTtPhuongthuc(), false);
		txtThm = createTextField(dto == null ? null : dto.getTtThm(), false);

		addRow(panel, "IDNV:", txtIdnv);
		addRow(panel, "CCCD:", txtCccd);
		addRow(panel, "Mã ngành:", txtManganh);
		addRow(panel, "Thứ tự NV:", txtTt);
		addRow(panel, "Điểm THXT:", txtDiemThxt);
		addRow(panel, "Điểm UTQD:", txtDiemUtqd);
		addRow(panel, "Điểm cộng:", txtDiemCong);
		addRow(panel, "Điểm xét tuyển:", txtDiemXettuyen);
		addRow(panel, "Kết quả:", txtKetqua);
		addRow(panel, "Keys:", txtKeys);
		addRow(panel, "Phương thức:", txtPhuongthuc);
		addRow(panel, "THM:", txtThm);

		return panel;
	}

	private JPanel buildButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
		JButton btnOK = new JButton("Lưu");
		JButton btnCancel = new JButton("Hủy");
		btnOK.setMargin(new Insets(8, 24, 8, 24));
		btnCancel.setMargin(new Insets(8, 24, 8, 24));

		btnOK.addActionListener(e -> {
			accepted = true;
			dialog.dispose();
		});

		btnCancel.addActionListener(e -> {
			accepted = false;
			dialog.dispose();
		});

		panel.add(btnOK);
		panel.add(btnCancel);
		return panel;
	}

	private void addRow(JPanel panel, String label, JTextField field) {
		JPanel row = new JPanel(new BorderLayout(8, 0));
		JLabel lbl = new JLabel(label);
		lbl.setPreferredSize(new java.awt.Dimension(120, 24));
		row.add(lbl, BorderLayout.WEST);
		row.add(field, BorderLayout.CENTER);
		row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
		panel.add(row);
		panel.add(javax.swing.Box.createVerticalStrut(6));
	}

	private JTextField createTextField(String value, boolean readOnly) {
		JTextField field = new JTextField(value == null ? "" : value);
		field.setEditable(!readOnly);
		return field;
	}

	private String formatDecimal(BigDecimal value) {
		if (value == null) return "";
		DecimalFormat df = new DecimalFormat("0.00000");
		return df.format(value);
	}

	public NguyenVongXetTuyenDTO showDialog() {
		dialog.setVisible(true);
		if (!accepted) {
			return null;
		}

		NguyenVongXetTuyenDTO result = new NguyenVongXetTuyenDTO();
		try {
			String idnvStr = txtIdnv.getText().trim();
			if (!idnvStr.isEmpty()) {
				result.setIdnv(Integer.parseInt(idnvStr));
			}
		} catch (NumberFormatException ignored) {
		}

		result.setNnCccd(emptyToNull(txtCccd.getText()));
		result.setNvManganh(emptyToNull(txtManganh.getText()));

		try {
			String ttStr = txtTt.getText().trim();
			if (!ttStr.isEmpty()) {
				result.setNvTt(Integer.parseInt(ttStr));
			}
		} catch (NumberFormatException ignored) {
		}

		result.setDiemThxt(parseDecimal(txtDiemThxt.getText()));
		result.setDiemUtqd(parseDecimal(txtDiemUtqd.getText()));
		result.setDiemCong(parseDecimal(txtDiemCong.getText()));
		result.setDiemXettuyen(parseDecimal(txtDiemXettuyen.getText()));
		result.setNvKetqua(emptyToNull(txtKetqua.getText()));
		result.setNvKeys(emptyToNull(txtKeys.getText()));
		result.setTtPhuongthuc(emptyToNull(txtPhuongthuc.getText()));
		result.setTtThm(emptyToNull(txtThm.getText()));

		return result;
	}

	private String emptyToNull(String value) {
		String trimmed = value == null ? "" : value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private BigDecimal parseDecimal(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			String cleaned = value.trim().replace(",", ".");
			return new BigDecimal(cleaned);
		} catch (Exception e) {
			return null;
		}
	}
}
