
import dal.entities.NganhEntity;
import dal.hibernate.HibernateUtil;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author KIET
 */
public class NganhDAO {
    public List<NganhEntity> LayDanhSach() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        Query q = session.createQuery("FROM Nganh");
        List<NganhEntity> ds = q.getResultList();
        
        ds.forEach(c -> System.out.println(c.getManganh() + " " + c.getTennganh()));
        
        session.close();
        
        return ds;
    }
}
