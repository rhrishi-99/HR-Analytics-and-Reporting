package com.hranalytics.exceptions;

/**
 * Raised when two data sources supply conflicting values for the same record.
 * Category: MINOR — last-write-wins resolution, flag conflict, include in daily admin report.
 * Owner: Prem M Thakur.
 */
public class DataIntegrationMergeConflictException extends HRAnalyticsException {

    public static final String ERROR_CODE = "DATA_INTEGRATION_MERGE_CONFLICT";

    private final String recordId;
    private final String fieldName;

    public DataIntegrationMergeConflictException(String recordId, String fieldName, String message) {
        super(ERROR_CODE, Category.MINOR, message);
        this.recordId = recordId;
        this.fieldName = fieldName;
    }

    public String getRecordId()  { return recordId; }
    public String getFieldName() { return fieldName; }
}
