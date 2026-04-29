package bus;

import dal.dao.NganhTuyenSinhDAO;
import dto.NganhTuyenSinhDTO;
import java.sql.SQLException;
import java.util.List;

public class NganhTuyenSinhService {
    private static final int PAGE_SIZE = 20;
    private final NganhTuyenSinhDAO dao = new NganhTuyenSinhDAO();

    public List<NganhTuyenSinhDTO> getRows(String code, String name, int page) throws SQLException {
        return dao.findRows(code, name, page, PAGE_SIZE);
    }

    public int countPages(String code, String name) throws SQLException {
        int total = dao.countRows(code, name);
        return Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
    }

    public int countRows(String code, String name) throws SQLException {
        return dao.countRows(code, name);
    }

    public boolean create(NganhTuyenSinhDTO dto) throws SQLException {
        return dao.create(dto);
    }

    public boolean update(NganhTuyenSinhDTO dto) throws SQLException {
        return dao.update(dto);
    }

    public boolean deleteById(int id) throws SQLException {
        return dao.deleteById(id);
    }

    public boolean upsertByCode(NganhTuyenSinhDTO dto) throws SQLException {
        return dao.upsertByCode(dto);
    }
}
