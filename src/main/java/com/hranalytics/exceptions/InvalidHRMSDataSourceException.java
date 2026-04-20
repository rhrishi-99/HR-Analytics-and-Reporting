package com.hranalytics.exceptions;

/**
 * Raised when the HRMS data source is unreachable or returns corrupt data.
 * Category: MAJOR — retry 3× with backoff, alert admin, serve cached data.
 * Owner: Prem M Thakur.
 */
public class InvalidHRMSDataSourceException extends HRAnalyticsException {

    public static final String ERROR_CODE = "INVALID_HRMS_DATA_SOURCE";

    public InvalidHRMSDataSourceException(String message) {
        super(ERROR_CODE, Category.MAJOR, message);
    }

    public InvalidHRMSDataSourceException(String message, Throwable cause) {
        super(ERROR_CODE, Category.MAJOR, message, cause);
    }
}
