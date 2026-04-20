package com.hranalytics.reports;

/**
 * Enumeration of supported report types that can be generated via HRAnalyticsFacade.generateReport().
 * Each type maps to a dedicated section set built by ReportGenerator.
 * Owner: R G Rhrishi (reports layer).
 */
public enum ReportType {
    ATTRITION,
    EMPLOYEE_GROWTH,
    COMPENSATION,
    DEPARTMENT_METRICS,
    PERFORMANCE,
    FULL_HR_SUMMARY
}
