package dto;

public class ScoreStatDTO {
    private Double diem;
    private long soLuong;

    public ScoreStatDTO(Double diem, long soLuong) {
        this.diem = diem;
        this.soLuong = soLuong;
    }

    public Double getDiem() { return diem; }
    public long getSoLuong() { return soLuong; }
}
