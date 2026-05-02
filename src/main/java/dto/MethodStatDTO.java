package dto;

public class MethodStatDTO {
    private String phuongThuc;
    private long soLuong;

    public MethodStatDTO(String phuongThuc, long soLuong) {
        this.phuongThuc = phuongThuc;
        this.soLuong = soLuong;
    }

    public String getPhuongThuc() { return phuongThuc; }
    public long getSoLuong() { return soLuong; }
}