package com.hranalytics.exceptions;

/**
 * Raised when an incoming record fails schema validation.
 * Category: MAJOR — skip invalid records, log per-field, produce end-of-batch summary.
 * Owner: Prem M Thakur.
 */
public class DataSchemaValidationException extends HRAnalyticsException {

    public static final String ERROR_CODE = "DATA_SCHEMA_VALIDATION_FAILURE";

    private final String fieldName;

    public DataSchemaValidationException(String fieldName, String message) {
        super(ERROR_CODE, Category.MAJOR, message);
        this.fieldName = fieldName;
    }

    /** Returns the name of the field that failed validation. */
    public String getFieldName() { return fieldName; }
}
