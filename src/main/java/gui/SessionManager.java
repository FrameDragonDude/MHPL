package gui;

import dto.UserDTO;


public class SessionManager {

    private static UserDTO currentUser = null;


    public static void setCurrentUser(UserDTO user) {
        currentUser = user;
    }


    public static UserDTO getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }


    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }


    public static boolean isStaff() {
        return currentUser != null && "NHAN_VIEN".equals(currentUser.getRole());
    }

 
    public static void logout() {
        currentUser = null;
    }

    public static Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

 
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }


    public static String getCurrentUserFullname() {
        return currentUser != null ? currentUser.getFullname() : "Unknown";
    }
}
