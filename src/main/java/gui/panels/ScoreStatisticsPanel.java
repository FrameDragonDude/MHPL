package gui.panels;

import bus.ExamScoreService;
import dto.StatisticDTO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ScoreStatisticsPanel extends JPanel {

    private final ExamScoreService service;

    private JComboBox<ComboItem> cbSubject;
    private JPanel chartContainer;

    public ScoreStatisticsPanel() {
        this.service = new ExamScoreService();

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Theo phương thức", buildMethodPanel());
        tabbedPane.addTab("Theo môn", buildSubjectPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildMethodPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        chartContainer = new JPanel(new BorderLayout());
        panel.add(chartContainer, BorderLayout.CENTER);

        loadMethodChart();

        return panel;
    }

    private void loadMethodChart() {
        try {
            List<StatisticDTO> data = service.statisticByMethod();

            DefaultPieDataset dataset = new DefaultPieDataset();

            for (StatisticDTO d : data) {
                String label = d.getLabel() + 
                        " (" + String.format("%.1f%%", d.getPercent()) + ")";
                dataset.setValue(label, d.getValue());
            }

            JFreeChart chart = ChartFactory.createPieChart(
                    "Thống kê theo phương thức",
                    dataset,
                    true,
                    true,
                    false
            );

            chartContainer.removeAll();
            chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi load chart: " + ex.getMessage());
        }
    }

    private JPanel buildSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        cbSubject = new JComboBox<>();
        cbSubject.addItem(new ComboItem("to", "Toán"));
        cbSubject.addItem(new ComboItem("va", "Văn"));
        cbSubject.addItem(new ComboItem("li", "Lý"));
        cbSubject.addItem(new ComboItem("ho", "Hóa"));
        cbSubject.addItem(new ComboItem("si", "Sinh"));
        cbSubject.addItem(new ComboItem("su", "Sử"));
        cbSubject.addItem(new ComboItem("di", "Địa"));
        cbSubject.addItem(new ComboItem("n1Thi", "N1 Thi"));
        cbSubject.addItem(new ComboItem("n1Cc", "N1 CC"));
        cbSubject.addItem(new ComboItem("ktpl", "Kinh tế pháp luật"));
        cbSubject.addItem(new ComboItem("ti", "Tin"));
        cbSubject.addItem(new ComboItem("cncn", "Công nghệ công nghiệp"));
        cbSubject.addItem(new ComboItem("cnnn", "Công nghệ nông nghiệp"));
        cbSubject.addItem(new ComboItem("nl1", "NL1"));
        cbSubject.addItem(new ComboItem("nk1", "NK1"));
        cbSubject.addItem(new ComboItem("nk2", "NK2"));

        top.add(new JLabel("Chọn môn:"));
        top.add(cbSubject);

        panel.add(top, BorderLayout.NORTH);

        // ===== Chart =====
        chartContainer = new JPanel(new BorderLayout());
        panel.add(chartContainer, BorderLayout.CENTER);

        // ===== Event =====
        cbSubject.addActionListener(e -> loadSubjectChart());

        loadSubjectChart();

        return panel;
    }

    private void loadSubjectChart() {
        try {
            ComboItem selectedItem = (ComboItem) cbSubject.getSelectedItem();
            String field = selectedItem.getValue();

            List<StatisticDTO> data = service.statisticBySubjectRange(field);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (StatisticDTO d : data) {
                String label = d.getLabel() + 
                        " (" + String.format("%.1f%%", d.getPercent()) + ")";
                dataset.addValue(d.getValue(), "Số lượng", label);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Phân bố điểm theo môn",
                    "Khoảng điểm",
                    "Số lượng",
                    dataset
            );

            chartContainer.removeAll();
            chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi load chart: " + ex.getMessage());
        }
    }

    private class ComboItem {
        private String value;
        private String label;

        public ComboItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

}