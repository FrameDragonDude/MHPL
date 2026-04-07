package dal;

import dal.hibernate.HibernateUtil;

public final class DBConnection {

    private DBConnection() {
    }

    public static boolean isHibernateReady() {
        try {
            return HibernateUtil.getSessionFactory() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}