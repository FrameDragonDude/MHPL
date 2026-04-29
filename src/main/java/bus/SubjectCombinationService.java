package bus;

import dal.dao.SubjectCombinationDAO;
import dto.SubjectCombinationDTO;
import java.sql.SQLException;
import java.util.List;

public class SubjectCombinationService {
    public static final int PAGE_SIZE = 20;

    private final SubjectCombinationDAO dao;

    public SubjectCombinationService() {
        this.dao = new SubjectCombinationDAO();
    }

    public List<SubjectCombinationDTO> getRows(String codeKeyword, String nameKeyword, int page) throws SQLException {
        int safePage = Math.max(page, 1);
        return dao.findRows(codeKeyword, nameKeyword, safePage, PAGE_SIZE);
    }

    public int countRows(String codeKeyword, String nameKeyword) throws SQLException {
        return dao.countRows(codeKeyword, nameKeyword);
    }

    public int countPages(String codeKeyword, String nameKeyword) throws SQLException {
        int totalRows = countRows(codeKeyword, nameKeyword);
        if (totalRows == 0) {
            return 1;
        }
        return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public boolean create(SubjectCombinationDTO dto) throws SQLException {
        if (!isValid(dto)) {
            return false;
        }
        return dao.create(dto);
    }

    public boolean update(SubjectCombinationDTO dto) throws SQLException {
        if (dto == null || dto.getId() == null || dto.getId() <= 0 || !isValid(dto)) {
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

    public boolean upsertByCode(SubjectCombinationDTO dto) throws SQLException {
        if (!isValid(dto)) {
            return false;
        }
        return dao.upsertByCode(dto);
    }

    private boolean isValid(SubjectCombinationDTO dto) {
        if (dto == null) {
            return false;
        }
        if (isBlank(dto.getMaToHop())) {
            return false;
        }
        return !(isBlank(dto.getMon1()) || isBlank(dto.getMon2()) || isBlank(dto.getMon3()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
