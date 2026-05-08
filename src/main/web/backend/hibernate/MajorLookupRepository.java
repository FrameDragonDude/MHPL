package backend.hibernate;

import dal.entities.NganhEntity;
import dal.hibernate.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import backend.dto.MajorOptionDTO;

@Repository
public class MajorLookupRepository {

	public List<MajorOptionDTO> findAllMajors() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<NganhEntity> rows = session.createQuery(
					"from NganhEntity n order by n.idnganh asc",
					NganhEntity.class
				)
				.getResultList();

			List<MajorOptionDTO> results = new ArrayList<>();
			for (NganhEntity row : rows) {
				results.add(new MajorOptionDTO(row.getManganh(), row.getTennganh()));
			}
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Khong the tai danh sach nganh", ex);
		}
	}
}