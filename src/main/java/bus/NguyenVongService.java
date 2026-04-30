package bus;

import dal.dao.NguyenVongDAO;
import dto.NguyenVongDTO;
import java.sql.SQLException;
import java.util.List;

public class NguyenVongService {
    private final NguyenVongDAO dao = new NguyenVongDAO();
    private static final int PAGE_SIZE = 20;

    public int countPages(String cccdKeyword, String truongKeyword) throws SQLException {
        int totalRows = dao.countRows(cccdKeyword, truongKeyword);
        return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public int countRows(String cccdKeyword, String truongKeyword) throws SQLException {
        return dao.countRows(cccdKeyword, truongKeyword);
    }

    public List<NguyenVongDTO> getRows(String cccdKeyword, String truongKeyword, int page) throws SQLException {
        return dao.findRows(cccdKeyword, truongKeyword, page, PAGE_SIZE);
    }

    public boolean create(NguyenVongDTO dto) throws SQLException {
        if (!isValid(dto)) {
            throw new SQLException("Dữ liệu nguyện vọng không hợp lệ");
        }
        return dao.create(dto);
    }

    public boolean update(NguyenVongDTO dto) throws SQLException {
        if (!isValid(dto)) {
            throw new SQLException("Dữ liệu nguyện vọng không hợp lệ");
        }
        return dao.update(dto);
    }

    public boolean deleteById(int id) throws SQLException {
        if (id <= 0) {
            return false;
        }
        return dao.delete(id);
    }

    public boolean importBulk(List<NguyenVongDTO> dtos) throws SQLException {
        if (dtos == null || dtos.isEmpty()) {
            return false;
        }
        List<NguyenVongDTO> failed = dao.importBulk(dtos);
        return failed.isEmpty() || failed.size() < dtos.size();
    }

    private boolean isValid(NguyenVongDTO dto) {
        if (dto == null) return false;
        String cccd = dto.getCccd();
        if (cccd == null || cccd.trim().isEmpty()) return false;
        String maTruong = dto.getMaTruong();
        if (maTruong == null || maTruong.trim().isEmpty()) return false;
        return true;
    }
}
