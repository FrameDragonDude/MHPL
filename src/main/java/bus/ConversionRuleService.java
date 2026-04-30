package bus;

import dal.dao.ConversionRuleDAO;
import dto.ConversionRuleDTO;
import java.sql.SQLException;
import java.util.List;

public class ConversionRuleService {
	private static final int PAGE_SIZE = 20;
	private final ConversionRuleDAO dao = new ConversionRuleDAO();

	public int countPages(String phuongThucKeyword, String toHopKeyword) throws SQLException {
		int totalRows = dao.countRows(phuongThucKeyword, toHopKeyword);
		return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
	}

	public int countRows(String phuongThucKeyword, String toHopKeyword) throws SQLException {
		return dao.countRows(phuongThucKeyword, toHopKeyword);
	}

	public List<ConversionRuleDTO> getRows(String phuongThucKeyword, String toHopKeyword, int page) throws SQLException {
		return dao.findRows(phuongThucKeyword, toHopKeyword, page, PAGE_SIZE);
	}
}