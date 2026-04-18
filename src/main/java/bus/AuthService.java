package bus;

import dal.dao.UserDAO;
import dal.entities.UserEntity;
import dto.UserDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserDTO login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }

        UserEntity user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null; 
        }

        if (!user.isActive()) {
            throw new SQLException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null; // Mật khẩu sai
        }

        userDAO.updateLastLogin(user.getId());

        return UserDTO.fromEntity(user);
    }


    public boolean changePassword(Integer userId, String oldPassword, String newPassword)
            throws SQLException {
        if (userId == null || userId <= 0 ||
                oldPassword == null || oldPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        UserEntity user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new SQLException("Mật khẩu cũ không chính xác");
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);

        return userDAO.updateUser(user);
    }

 
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            return null;
        }
        return passwordEncoder.encode(plainPassword);
    }


    public boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean resetPassword(Integer userId, String newPassword) throws SQLException {
        if (userId == null || userId <= 0 ||
                !isPasswordValid(newPassword)) {
            return false;
        }

        UserEntity user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);

        return userDAO.updateUser(user);
    }
}