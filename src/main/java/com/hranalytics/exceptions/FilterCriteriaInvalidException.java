package com.hranalytics.exceptions;

import java.util.List;

/**
 * Raised when submitted filter criteria contain invalid field values.
 * Category: WARNING — highlight invalid fields to user, reset to defaults, block the query.
 * Owner: R G Rhrishi.
 */
public class FilterCriteriaInvalidException extends HRAnalyticsException {

    public static final String ERROR_CODE = "FILTER_CRITERIA_INVALID";

    private final List<String> invalidFields;

    public FilterCriteriaInvalidException(List<String> invalidFields, String message) {
        super(ERROR_CODE, Category.WARNING, message);
        this.invalidFields = invalidFields;
    }

    /** Returns the list of field names that failed validation. */
    public List<String> getInvalidFields() { return invalidFields; }
}
