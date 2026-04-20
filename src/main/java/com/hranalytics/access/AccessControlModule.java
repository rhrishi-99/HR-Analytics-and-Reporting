package com.hranalytics.access;

import com.hranalytics.domain.User;
import com.hranalytics.exceptions.UnauthorizedAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Governs access to every layer of the analytics pipeline.
 * Checks user permissions before any operation is executed.
 * Tracks failed access attempts and locks the account after 5 consecutive failures.
 * Owner: Prem M Thakur (Facade / access control boundary).
 */
public class AccessControlModule {

    private static final Logger LOG = Logger.getLogger(AccessControlModule.class.getName());
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final Map<String, User> userRegistry = new HashMap<>();

    /** Tracks consecutive failed access attempts per userId. */
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    /** Locked accounts — further requests are immediately rejected. */
    private final Map<String, Boolean> lockedAccounts = new HashMap<>();

    /**
     * Registers a user in the access control registry.
     * Called during system initialisation.
     */
    public void registerUser(User user) {
        userRegistry.put(user.getUserId(), user);
    }

    /**
     * Verifies that the given userId holds the required permission.
     * Throws UNAUTHORIZED_ACCESS_ATTEMPT on failure; locks account after 5 failures.
     */
    public void authorize(String userId, String requiredPermission) {
        // Immediately reject locked accounts
        if (Boolean.TRUE.equals(lockedAccounts.get(userId))) {
            throw new UnauthorizedAccessException(userId,
                    requiredPermission + " [account locked]");
        }

        User user = userRegistry.get(userId);
        if (user == null || !user.hasPermission(requiredPermission)) {
            recordFailure(userId, requiredPermission);
            throw new UnauthorizedAccessException(userId, requiredPermission);
        }

        // Success — reset failure counter
        failedAttempts.remove(userId);
        LOG.info("Access granted: user=" + userId + " action=" + requiredPermission);
    }

    /** Returns the User object for an authenticated session (after authorization). */
    public User getUser(String userId) {
        return userRegistry.get(userId);
    }

    /** Returns true if the given userId is registered and not locked. */
    public boolean isActive(String userId) {
        return userRegistry.containsKey(userId)
                && !Boolean.TRUE.equals(lockedAccounts.get(userId));
    }

    /** Increments the failure counter and locks the account if the threshold is reached. */
    private void recordFailure(String userId, String action) {
        int failures = failedAttempts.getOrDefault(userId, 0) + 1;
        failedAttempts.put(userId, failures);
        LOG.warning("Unauthorized attempt #" + failures + " by user=" + userId + " for action=" + action);

        if (failures >= MAX_FAILED_ATTEMPTS) {
            lockedAccounts.put(userId, true);
            LOG.severe("Account LOCKED after " + MAX_FAILED_ATTEMPTS
                    + " failed attempts: userId=" + userId);
        }
    }

    /** Manually unlocks an account (admin action). */
    public void unlockAccount(String userId) {
        lockedAccounts.remove(userId);
        failedAttempts.remove(userId);
        LOG.info("Account unlocked by admin: userId=" + userId);
    }

    /** Pre-populates the registry with the default set of system users. */
    public void loadDefaultUsers() {
        registerUser(new User("U001", "admin",       User.UserRole.ADMIN,
                List.of("VIEW_DASHBOARD", "GENERATE_REPORT", "EXPORT_REPORT", "VIEW_KPI", "ADMIN")));
        registerUser(new User("U002", "hr_manager",  User.UserRole.HR_MANAGER,
                List.of("VIEW_DASHBOARD", "GENERATE_REPORT", "EXPORT_REPORT", "VIEW_KPI")));
        registerUser(new User("U003", "dept_head",   User.UserRole.DEPARTMENT_HEAD,
                List.of("VIEW_DASHBOARD", "VIEW_KPI")));
        registerUser(new User("U004", "employee",    User.UserRole.EMPLOYEE,
                List.of("VIEW_DASHBOARD")));
    }
}
