package dto;

import java.util.HashMap;
import java.util.Map;

public class StatisticsDTO {
    private int total;
    private Map<String, Integer> countByKhuVuc = new HashMap<>();
    private Map<String, Integer> countByDoiTuong = new HashMap<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Map<String, Integer> getCountByKhuVuc() {
        return countByKhuVuc;
    }

    public void setCountByKhuVuc(Map<String, Integer> countByKhuVuc) {
        this.countByKhuVuc = countByKhuVuc;
    }

    public Map<String, Integer> getCountByDoiTuong() {
        return countByDoiTuong;
    }

    public void setCountByDoiTuong(Map<String, Integer> countByDoiTuong) {
        this.countByDoiTuong = countByDoiTuong;
    }
}
