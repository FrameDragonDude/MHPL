import org.hibernate.Session;
import dal.hibernate.HibernateUtil;

public class QuanLyTuyenSinh {

    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Integer ping = session.createNativeQuery("select 1", Integer.class).getSingleResult();
            System.out.println("Hibernate ket noi thanh cong. Test query = " + ping);
        } catch (Exception ex) {
            System.out.println("Khong the ket noi database bang Hibernate. Kiem tra file hibernate.cfg.xml.");
            ex.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
