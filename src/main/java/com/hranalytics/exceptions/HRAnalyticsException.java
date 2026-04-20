package com.hranalytics.exceptions;

/**
 * Base exception for all HR Analytics sub-system exceptions.
 * Every canonical exception in the course exception table extends this class.
 */
public class HRAnalyticsException extends RuntimeException {

    public enum Category { MAJOR, MINOR, WARNING }

    private final String errorCode;
    private final Category category;

    public HRAnalyticsException(String errorCode, Category category, String message) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
    }

    public HRAnalyticsException(String errorCode, Category category, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
    }

    public String getErrorCode() { return errorCode; }
    public Category getCategory(){ return category; }

    @Override
    public String toString() {
        return String.format("[%s][%s] %s", category, errorCode, getMessage());
    }
}
