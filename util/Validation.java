package util;

public class Validation {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 1 || atIndex != email.lastIndexOf('@')) {
            return false;
        }
        int dotIndex = email.indexOf('.', atIndex);
        if (dotIndex == -1 || dotIndex == email.length() - 1) {
            return false;
        }
        return email.matches("[a-zA-Z0-9._%+-@]+");
    }
}
