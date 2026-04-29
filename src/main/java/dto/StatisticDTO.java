package dto;

public class StatisticDTO {
    private String label;
    private long value;
    private double percent;

    public StatisticDTO(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public long getValue() { return value; }
    public double getPercent() { return percent; }
    public void setPercent(double percent) { this.percent = percent; }
    public void setValue(long value) { this.value = value; }
    public void setLabel(String label) { this.label = label; }
}