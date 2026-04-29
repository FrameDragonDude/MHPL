package bus;

import dal.dao.MajorCombinationDAO;
import dto.MajorCombinationDTO;
import java.sql.SQLException;
import java.util.List;

public class MajorCombinationService {
    public static final int PAGE_SIZE = 20;

    private final MajorCombinationDAO dao;

    public MajorCombinationService() {
        this.dao = new MajorCombinationDAO();
    }

    public List<MajorCombinationDTO> getRows(String majorKeyword, String toHopKeyword, int page) throws SQLException {
        int safePage = Math.max(page, 1);
        return dao.findRows(majorKeyword, toHopKeyword, safePage, PAGE_SIZE);
    }

    public int countRows(String majorKeyword, String toHopKeyword) throws SQLException {
        return dao.countRows(majorKeyword, toHopKeyword);
    }

    public int countPages(String majorKeyword, String toHopKeyword) throws SQLException {
        int totalRows = countRows(majorKeyword, toHopKeyword);
        if (totalRows == 0) {
            return 1;
        }
        return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public boolean create(MajorCombinationDTO dto) throws SQLException {
        if (!isValid(dto)) {
            return false;
        }
        return dao.create(dto);
    }

    public boolean update(MajorCombinationDTO dto) throws SQLException {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            return false;
        }
        if (!isValid(dto)) {
            return false;
        }
        return dao.update(dto);
    }

    public boolean deleteById(int id) throws SQLException {
        if (id <= 0) {
            return false;
        }
        return dao.deleteById(id);
    }

    public boolean upsertByTbKeys(MajorCombinationDTO dto) throws SQLException {
        if (!isValid(dto)) {
            return false;
        }
        return dao.upsertByTbKeys(dto);
    }

    private boolean isValid(MajorCombinationDTO dto) {
        if (dto == null) {
            return false;
        }
        if (isBlank(dto.getTenNganhChuan()) && isBlank(dto.getManganh())) {
            return false;
        }
        if (isBlank(dto.getMaToHop())) {
            return false;
        }
        return !isBlank(dto.getMon1()) && !isBlank(dto.getMon2()) && !isBlank(dto.getMon3());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
