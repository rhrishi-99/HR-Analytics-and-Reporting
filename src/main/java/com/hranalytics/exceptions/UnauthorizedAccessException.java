package com.hranalytics.exceptions;

/**
 * Raised when a user attempts an operation they are not permitted to perform.
 * Category: MAJOR — terminate request, log attempt, lock account after 5 failures.
 * Owner: Prem M Thakur.
 */
public class UnauthorizedAccessException extends HRAnalyticsException {

    public static final String ERROR_CODE = "UNAUTHORIZED_ACCESS_ATTEMPT";

    private final String userId;
    private final String attemptedAction;

    public UnauthorizedAccessException(String userId, String attemptedAction) {
        super(ERROR_CODE, Category.MAJOR,
              "Unauthorized access attempt by user '" + userId + "' for action: " + attemptedAction);
        this.userId = userId;
        this.attemptedAction = attemptedAction;
    }

    public String getUserId()          { return userId; }
    public String getAttemptedAction() { return attemptedAction; }
}
