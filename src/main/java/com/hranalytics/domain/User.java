package com.hranalytics.domain;

import java.util.List;

/**
 * Domain class representing an HRMS system user with role-based access.
 * Used by AccessControlModule to authorize operations.
 */
public class User {

    public enum UserRole { ADMIN, HR_MANAGER, DEPARTMENT_HEAD, EMPLOYEE }

    private final String userId;
    private final String username;
    private final UserRole userRole;
    private final List<String> permissions;

    public User(String userId, String username, UserRole userRole, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
        this.permissions = permissions;
    }

    public String getUserId()         { return userId; }
    public String getUsername()       { return username; }
    public UserRole getUserRole()     { return userRole; }
    public List<String> getPermissions() { return permissions; }

    /** Returns true if the user holds the requested permission. */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    @Override
    public String toString() {
        return String.format("User[%s, %s, role=%s]", userId, username, userRole);
    }
}
