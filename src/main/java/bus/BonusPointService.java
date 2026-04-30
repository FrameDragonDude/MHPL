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

	public boolean create(BonusPointDTO dto) throws SQLException {
		return dao.create(dto);
	}

	public boolean update(BonusPointDTO dto) throws SQLException {
		return dao.update(dto);
	}

	public boolean deleteById(int id) throws SQLException {
		return dao.delete(id);
	}

	public boolean upsertByKey(BonusPointDTO dto) throws SQLException {
		return dao.upsertByKey(dto);
	}
}