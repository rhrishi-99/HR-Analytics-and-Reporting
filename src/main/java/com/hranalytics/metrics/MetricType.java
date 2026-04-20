package com.hranalytics.metrics;

/**
 * Enumeration of all metric types supported by MetricsCalculationEngine.
 * Each value maps 1-to-1 to a MetricStrategy implementation.
 * Owner: Raihan Naeem (Strategy pattern).
 */
public enum MetricType {
    ATTRITION_RATE,
    EMPLOYEE_GROWTH,
    AVERAGE_PERFORMANCE,
    DEPARTMENT_METRICS,
    COMPENSATION_ANALYTICS
}
