package bus;

import dal.dao.UserDAO;
import dal.entities.UserEntity;
import dto.UserDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.SQLException;
import dto.UserDTO;
import java.util.List;
import dal.entities.UserEntity;


public class UserService {

    public static final int PAGE_SIZE = 10;

    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.authService = new AuthService();
    }


    public List<UserDTO> getAllUsers() throws SQLException {
        List<UserEntity> entities = userDAO.findAllUsers();
        List<UserDTO> dtos = new ArrayList<>();
        for (UserEntity entity : entities) {
            dtos.add(UserDTO.fromEntity(entity));
        }
        return dtos;
    }

    public List<UserDTO> getUsersPaginated(int page) throws SQLException {
        int safePage = Math.max(page, 1);
        List<UserEntity> entities = userDAO.findUsersPaginated(safePage, PAGE_SIZE);
        List<UserDTO> dtos = new ArrayList<>();
        for (UserEntity entity : entities) {
            dtos.add(UserDTO.fromEntity(entity));
        }
        return dtos;
    }


    public int countPages() throws SQLException {
        int totalRows = userDAO.countUsers();
        if (totalRows == 0) {
            return 1;
        }
        return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    /**
     * Äáº¿m tá»•ng sá»‘ user
     */
    public int countUsers() throws SQLException {
        return userDAO.countUsers();
    }

    /**
     * Táº¡o user má»›i
     */
    public boolean createUser(UserDTO userDTO) throws SQLException {
        if (userDTO == null ||
                userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty() ||
                userDTO.getPassword() == null || !authService.isPasswordValid(userDTO.getPassword())) {
            return false;
        }

        // Kiá»ƒm tra username khÃ´ng trÃ¹ng
        UserEntity existing = userDAO.findByUsername(userDTO.getUsername());
        if (existing != null) {
            throw new SQLException("TÃªn Ä‘Äƒng nháº­p '" + userDTO.getUsername() + "' Ä‘Ã£ tá»“n táº¡i");
        }

        // Táº¡o entity vÃ  hash password
        UserEntity entity = new UserEntity();
        entity.setUsername(userDTO.getUsername().trim());
        entity.setPassword(authService.hashPassword(userDTO.getPassword()));
        entity.setFullname(userDTO.getFullname());
        entity.setRole(UserEntity.UserRole.valueOf(userDTO.getRole() != null ? userDTO.getRole() : "NHAN_VIEN"));
        entity.setStatus(1); // ACTIVE by default

        return userDAO.createUser(entity);
    }

    /**
     * Cáº­p nháº­t thÃ´ng tin user (khÃ´ng cáº­p nháº­t password á»Ÿ Ä‘Ã¢y)
     */
    public boolean updateUser(UserDTO userDTO) throws SQLException {
        if (userDTO == null || userDTO.getId() == null || userDTO.getId() <= 0) {
            return false;
        }

        UserEntity entity = userDAO.findById(userDTO.getId());
        if (entity == null) {
            return false;
        }

        // Update cÃ¡c field Ä‘Æ°á»£c phÃ©p
        entity.setFullname(userDTO.getFullname());
        if (userDTO.getRole() != null) {
            entity.setRole(UserEntity.UserRole.valueOf(userDTO.getRole()));
        }

        return userDAO.updateUser(entity);
    }

    /**
     * KhÃ³a user
     */
    public boolean disableUser(Integer userId) throws SQLException {
        return userDAO.toggleUserStatus(userId, 0);
    }

    /**
     * Má»Ÿ khÃ³a user
     */
    public boolean enableUser(Integer userId) throws SQLException {
        return userDAO.toggleUserStatus(userId, 1);
    }

    /**
     * Thay Ä‘á»•i tráº¡ng thÃ¡i user (active/disabled)
     */
    public boolean toggleUserStatus(Integer userId) throws SQLException {
        UserEntity user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        int newStatus = (user.getStatus() != null && user.getStatus() == 1) ? 0 : 1;
        return userDAO.toggleUserStatus(userId, newStatus);
    }

    /**
     * Láº¥y thÃ´ng tin user theo ID
     */
    public UserDTO getUserById(Integer userId) throws SQLException {
        UserEntity entity = userDAO.findById(userId);
        return UserDTO.fromEntity(entity);
    }

    /**
     * Láº¥y thÃ´ng tin user theo username
     */
    public UserDTO getUserByUsername(String username) throws SQLException {
        UserEntity entity = userDAO.findByUsername(username);
        return UserDTO.fromEntity(entity);
    }

    /**
     * Reset password cho user (admin feature)
     */
    public boolean resetUserPassword(Integer userId, String newPassword) throws SQLException {
        if (userId == null || userId <= 0 ||
                newPassword == null || !authService.isPasswordValid(newPassword)) {
            return false;
        }

        return authService.resetPassword(userId, newPassword);
    }

    /**
     * Äá»•i role cho user (admin feature)
     */
    public boolean changeUserRole(Integer userId, String newRole) throws SQLException {
        if (userId == null || userId <= 0 || newRole == null) {
            return false;
        }

        try {
            UserEntity.UserRole.valueOf(newRole);
        } catch (IllegalArgumentException e) {
            return false;
        }

        UserEntity user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setRole(UserEntity.UserRole.valueOf(newRole));
        return userDAO.updateUser(user);
    }

 
    public List<String> getAvailableRoles() {
        List<String> roles = new ArrayList<>();
        for (UserEntity.UserRole role : UserEntity.UserRole.values()) {
            roles.add(role.name());
        }
        return roles;
    }

    /**
     * XÃ³a user
     */
    public boolean deleteUser(Integer userId) throws SQLException {
        if (userId == null || userId <= 0) {
            return false;
        }
        return userDAO.deleteUserById(userId);
    }

    /**
     * TÃ¬m kiáº¿m user theo username hoáº·c tÃªn
     */
    public List<UserDTO> searchUsers(String keyword) throws SQLException {
        List<UserEntity> entities = userDAO.searchUsers(keyword);
        List<UserDTO> dtos = new ArrayList<>();
        for (UserEntity entity : entities) {
            dtos.add(UserDTO.fromEntity(entity));
        }
        return dtos;
    }


    public List<UserDTO> getUsersByPage(int pageNumber, int pageSize) throws SQLException {
        List<UserEntity> entities = userDAO.getUsersByPage(pageNumber, pageSize);
        List<UserDTO> dtos = new ArrayList<>();
        for (UserEntity entity : entities) {
            dtos.add(UserDTO.fromEntity(entity));
        }
        return dtos;
    }


    public String getRoleDisplay(String roleName) {
        try {
            return UserEntity.UserRole.valueOf(roleName).getDisplay();
        } catch (IllegalArgumentException e) {
            return roleName;
        }
    }
}
