package bus;

import dal.dao.NguyenVongXetTuyenDAO;
import dto.NguyenVongXetTuyenDTO;
import java.sql.SQLException;
import java.util.List;

public class NguyenVongXetTuyenService {
	private final NguyenVongXetTuyenDAO dao = new NguyenVongXetTuyenDAO();
	private static final int PAGE_SIZE = 20;

	public int countPages(String cccdKeyword, String nganhKeyword) throws SQLException {
		int totalRows = dao.countRows(cccdKeyword, nganhKeyword);
		return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
	}

	public int countRows(String cccdKeyword, String nganhKeyword) throws SQLException {
		return dao.countRows(cccdKeyword, nganhKeyword);
	}

	public List<NguyenVongXetTuyenDTO> getRows(String cccdKeyword, String nganhKeyword, int page) throws SQLException {
		return dao.findRows(cccdKeyword, nganhKeyword, page, PAGE_SIZE);
	}

	public boolean create(NguyenVongXetTuyenDTO dto) throws SQLException {
		if (!isValid(dto)) {
			throw new SQLException("Dữ liệu nguyện vọng xét tuyển không hợp lệ");
		}
		return dao.create(dto);
	}

	public boolean update(NguyenVongXetTuyenDTO dto) throws SQLException {
		if (!isValid(dto)) {
			throw new SQLException("Dữ liệu nguyện vọng xét tuyển không hợp lệ");
		}
		return dao.update(dto);
	}

	public boolean deleteById(int id) throws SQLException {
		if (id <= 0) {
			return false;
		}
		return dao.delete(id);
	}

	private boolean isValid(NguyenVongXetTuyenDTO dto) {
		if (dto == null) return false;
		String cccd = dto.getNnCccd();
		if (cccd == null || cccd.trim().isEmpty()) return false;
		String nganh = dto.getNvManganh();
		if (nganh == null || nganh.trim().isEmpty()) return false;
		return true;
	}
}
