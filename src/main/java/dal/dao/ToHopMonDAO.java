package dal.dao;

import dal.entities.ToHopMon;
import dal.hibernate.HibernateUtil;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class ToHopMonDAO {
    public void Them(String matohop, String mon1,
            String mon2, String mon3, String tentohop) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        ToHopMon t = new ToHopMon();
        t.setMatohop(matohop);
        t.setMon1(mon1);
        t.setMon2(mon2);
        t.setMon3(mon3);
        t.setTentohop(tentohop);
        
        session.getTransaction().begin();
        session.persist(t);
        session.getTransaction().commit();
        
        session.close();
    }
    public void Sua(String matohop, String mon1, String mon2, String mon3, String tentohop) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        ToHopMon t = session.get(ToHopMon.class, 5);
        t.setMatohop(matohop);
        t.setMon1(mon1);
        t.setMon2(mon2);
        t.setMon3(mon3);
        t.setTentohop(tentohop);
        
        session.getTransaction().begin();
        session.persist(t);
        session.getTransaction().commit();
        
        session.close();
    }
    public void Xoa() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        ToHopMon t = session.get(ToHopMon.class, 5);
        
        session.getTransaction().begin();
        session.remove(t);
        session.getTransaction().commit();
        
        session.close();
    }
    public void LayDanhSach() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        Query q = session.createQuery("FROM ToHopMon");
        List<ToHopMon> ds = q.getResultList();
        
        ds.forEach(c -> System.out.println(c.getMatohop() + " " + c.getTentohop()));
        
        session.close();
    }
}