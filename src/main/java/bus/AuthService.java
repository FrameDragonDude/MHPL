package bus;

import dal.dao.UserDAO;
import dal.entities.UserEntity;
import dto.UserDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

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

        if (!isPasswordMatch(password, user.getPassword())) {
            return null; // Mật khẩu sai
        }

        // Tự động nâng cấp mật khẩu cũ dạng plain text lên bcrypt sau đăng nhập thành công.
        if (!isBcryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userDAO.updateUser(user);
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

        if (!isPasswordMatch(oldPassword, user.getPassword())) {
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

    private boolean isPasswordMatch(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        String normalizedStored = storedPassword.trim();
        if (isBcryptHash(normalizedStored)) {
            return passwordEncoder.matches(rawPassword, normalizedStored);
        }

        return rawPassword.equals(normalizedStored);
    }

    private boolean isBcryptHash(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value.trim()).matches();
    }
}