package com.hranalytics.exceptions;

/**
 * Raised when report generation exceeds the allowed time budget.
 * Category: MINOR — cancel generation, notify user, offer a lighter report variant.
 * Owner: R G Rhrishi.
 */
public class ReportGenerationTimeoutException extends HRAnalyticsException {

    public static final String ERROR_CODE = "REPORT_GENERATION_TIMEOUT";

    private final String reportType;
    private final long elapsedMs;

    public ReportGenerationTimeoutException(String reportType, long elapsedMs) {
        super(ERROR_CODE, Category.MINOR,
              "Report generation timed out for type '" + reportType + "' after " + elapsedMs + " ms");
        this.reportType = reportType;
        this.elapsedMs = elapsedMs;
    }

    public String getReportType() { return reportType; }
    public long getElapsedMs()    { return elapsedMs; }
}
