package com.hranalytics.exceptions;

/**
 * Raised when a scheduled report fails to be dispatched to its recipient.
 * Category: MINOR — retry 3× at 5-min intervals, then store for manual re-dispatch.
 * Owner: R G Rhrishi.
 */
public class ScheduledReportDispatchFailureException extends HRAnalyticsException {

    public static final String ERROR_CODE = "SCHEDULED_REPORT_DISPATCH_FAILURE";

    private final String reportId;
    private final int attemptNumber;

    public ScheduledReportDispatchFailureException(String reportId, int attemptNumber, String message) {
        super(ERROR_CODE, Category.MINOR, message);
        this.reportId = reportId;
        this.attemptNumber = attemptNumber;
    }

    public String getReportId()     { return reportId; }
    public int getAttemptNumber()   { return attemptNumber; }
}
