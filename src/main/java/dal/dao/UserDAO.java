package dal.dao;

import dal.entities.UserEntity;
import dal.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UserDAO - Data Access Object cho User
 * Thực thi các câu query với bảng xt_staff_accounts
 */
public class UserDAO {

    /**
     * Tìm user theo username
     */
    public UserEntity findByUsername(String username) throws SQLException {
        String value = safeNullable(username);
        if (value == null) {
            return null;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserEntity u where u.username = :username",
                    UserEntity.class)
                    .setParameter("username", value)
                    .uniqueResult();
        } catch (Exception ex) {
            throw asSqlException("tìm user theo username", ex);
        }
    }

    /**
     * Tìm user theo ID
     */
    public UserEntity findById(Integer userId) throws SQLException {
        if (userId == null || userId <= 0) {
            return null;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(UserEntity.class, userId);
        } catch (Exception ex) {
            throw asSqlException("tìm user theo id", ex);
        }
    }

    /**
     * Lấy danh sách tất cả user (để admin quản lý)
     */
    public List<UserEntity> findAllUsers() throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserEntity u order by u.id asc",
                    UserEntity.class).list();
        } catch (Exception ex) {
            throw asSqlException("lấy danh sách user", ex);
        }
    }

    /**
     * Lấy danh sách user với phân trang (cho admin panel)
     */
    public List<UserEntity> findUsersPaginated(int page, int pageSize) throws SQLException {
        int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserEntity u order by u.id asc",
                    UserEntity.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
        } catch (Exception ex) {
            throw asSqlException("lấy user phân trang", ex);
        }
    }

    /**
     * Đếm tổng số user
     */
    public int countUsers() throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "select count(u.id) from UserEntity u",
                    Long.class).uniqueResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception ex) {
            throw asSqlException("đếm user", ex);
        }
    }

    /**
     * Tạo user mới
     */
    public boolean createUser(UserEntity user) throws SQLException {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw asSqlException("tạo user", ex);
        }
    }

    /**
     * Cập nhật thông tin user
     */
    public boolean updateUser(UserEntity user) throws SQLException {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw asSqlException("cập nhật user", ex);
        }
    }

    /**
     * Khóa / mở khóa user
     */
    public boolean toggleUserStatus(Integer userId, int status) throws SQLException {
        if (userId == null || userId <= 0 || (status != 0 && status != 1)) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, userId);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            user.setStatus(status);
            session.merge(user);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw asSqlException("thay đổi trạng thái user", ex);
        }
    }

    /**
     * Cập nhật thời gian đăng nhập cuối cùng
     */
    public boolean updateLastLogin(Integer userId) throws SQLException {
        if (userId == null || userId <= 0) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, userId);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            user.setLastLogin(LocalDateTime.now());
            session.merge(user);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw asSqlException("cập nhật last login", ex);
        }
    }

    /**
     * Xóa user theo ID
     */
    public boolean deleteUserById(Integer userId) throws SQLException {
        if (userId == null || userId <= 0) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, userId);
            if (user == null) {
                transaction.rollback();
                return false;
            }
            session.remove(user);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw asSqlException("xóa user", ex);
        }
    }

    /**
     * Tìm kiếm user theo username hoặc tên (LIKE)
     */
    public List<UserEntity> searchUsers(String keyword) throws SQLException {
        String searchTerm = safeNullable(keyword);
        if (searchTerm == null) {
            return findAllUsers();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserEntity u where u.username like :keyword or u.fullname like :keyword order by u.id asc",
                    UserEntity.class)
                    .setParameter("keyword", "%" + searchTerm + "%")
                    .list();
        } catch (Exception ex) {
            throw asSqlException("tìm kiếm user", ex);
        }
    }

    /**
     * Lấy danh sách user theo trang (giống findUsersPaginated)
     */
    public List<UserEntity> getUsersByPage(int pageNumber, int pageSize) throws SQLException {
        return findUsersPaginated(pageNumber, pageSize);
    }

    // ==================== Helper Methods ====================
    private static String safeNullable(String input) {
        return input == null || input.trim().isEmpty() ? null : input.trim();
    }

    private static SQLException asSqlException(String action, Exception ex) {
        String message = "Lỗi khi " + action;
        if (ex != null && ex.getMessage() != null) {
            message += ": " + ex.getMessage();
        }
        return new SQLException(message, ex);
    }
}