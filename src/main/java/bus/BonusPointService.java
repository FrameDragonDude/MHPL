package bus;

import dal.dao.BonusPointDAO;
import dto.BonusPointDTO;
import java.sql.SQLException;
import java.util.List;

public class BonusPointService {
    private static final int PAGE_SIZE = 20;
    private final BonusPointDAO dao = new BonusPointDAO();

    public int countPages(String cccdKeyword, String methodKeyword) throws SQLException {
        int totalRows = dao.countRows(cccdKeyword, methodKeyword);
        return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public int countRows(String cccdKeyword, String methodKeyword) throws SQLException {
        return dao.countRows(cccdKeyword, methodKeyword);
    }

    public List<BonusPointDTO> getRows(String cccdKeyword, String methodKeyword, int page) throws SQLException {
        return dao.findRows(cccdKeyword, methodKeyword, page, PAGE_SIZE);
    }

    private void calculateAndClampBonusPoint(BonusPointDTO dto) {
        if (dto == null) return;
        double cc = dto.getDiemCC() != null ? dto.getDiemCC() : 0.0;
        double utxt = dto.getDiemUtxt() != null ? dto.getDiemUtxt() : 0.0;
        double total = cc + utxt;
        if (total > 3.0) {
            dto.setDiemTong(3.0);
        } else {
            dto.setDiemTong(total);
        }
    }

    public boolean create(BonusPointDTO dto) throws SQLException {
        calculateAndClampBonusPoint(dto);
        return dao.create(dto);
    }

    public boolean update(BonusPointDTO dto) throws SQLException {
        calculateAndClampBonusPoint(dto);
        return dao.update(dto);
    }

    public boolean deleteById(int id) throws SQLException {
        return dao.delete(id);
    }

    public boolean upsertByKey(BonusPointDTO dto) throws SQLException {
        if (dto == null || dto.getTsCccd() == null || dto.getTsCccd().trim().isEmpty()) {
            return false;
        }
        calculateAndClampBonusPoint(dto);
        return dao.upsertByKey(dto);
    }
}