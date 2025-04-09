package khanhtypo.librarycmd.util;

public record ActiveUser(String username, UserRole userRole) {
    public static final ActiveUser NO_ACTIVE_USER = new ActiveUser(null, null);

    public boolean isAdmin() {
        return userRole == UserRole.ADMIN;
    }

    public boolean isLibrarian() {
        return userRole == UserRole.LIBRARIAN;
    }

    public boolean isReader() {
        return userRole == UserRole.READER;
    }

    public boolean hasLoggedIn() {
        return this != NO_ACTIVE_USER;
    }
}