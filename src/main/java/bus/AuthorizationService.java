package bus;

import dto.UserDTO;

/**
 * AuthorizationService - Xử lý authorization (phân quyền)
 * Kiểm tra role và permission
 */
public class AuthorizationService {

    /**
     * Kiểm tra user có phải ADMIN không
     */
    public static boolean isAdmin(UserDTO user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * Kiểm tra user có phải NHAN_VIEN không
     */
    public static boolean isStaff(UserDTO user) {
        return user != null && "NHAN_VIEN".equals(user.getRole());
    }

    /**
     * Kiểm tra user có phải active không
     */
    public static boolean isActive(UserDTO user) {
        return user != null && user.isActive();
    }

    /**
     * Kiểm tra user có quyền truy cập module không
     */
    public static boolean hasPermission(UserDTO user, String moduleName) {
        if (user == null || !isActive(user)) {
            return false;
        }

        // ADMIN có quyền truy cập tất cả modules
        if (isAdmin(user)) {
            return true;
        }

        // NHAN_VIEN có quyền truy cập các module công khai
        if (isStaff(user)) {
            return isPublicModule(moduleName);
        }

        return false;
    }

    /**
     * Kiểm tra module có public không (NHAN_VIEN có thể truy cập)
     */
    private static boolean isPublicModule(String moduleName) {
        if (moduleName == null) {
            return false;
        }

        // Các module mà NHAN_VIEN có thể truy cập
        return moduleName.equals("CANDIDATE") ||
                moduleName.equals("EXAM_SCORE") ||
                moduleName.equals("DASHBOARD");
    }

    /**
     * Kiểm tra user có quyền admin không (dùng cho admin-only features)
     */
    public static boolean isAdminOnly(UserDTO user) {
        return isAdmin(user) && isActive(user);
    }

    /**
     * Kiểm tra user có quyền thực hiện action không
     */
    public static boolean canPerformAction(UserDTO user, String action) {
        if (user == null || !isActive(user)) {
            return false;
        }

        // Các action dành cho ADMIN
        if (action.startsWith("ADMIN_")) {
            return isAdmin(user);
        }

        // Các action dành cho STAFF
        if (action.startsWith("STAFF_")) {
            return isStaff(user) || isAdmin(user);
        }

        // Action public
        return true;
    }
}