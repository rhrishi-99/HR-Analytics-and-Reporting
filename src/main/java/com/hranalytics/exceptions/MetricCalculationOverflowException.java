package com.hranalytics.exceptions;

/**
 * Raised when a metric calculation produces an overflow or undefined result.
 * Category: MINOR — substitute 0.0, flag the metric card, log inputs.
 * Owner: Raihan Naeem.
 */
public class MetricCalculationOverflowException extends HRAnalyticsException {

    public static final String ERROR_CODE = "METRIC_CALCULATION_OVERFLOW";

    private final String metricName;

    public MetricCalculationOverflowException(String metricName, String message) {
        super(ERROR_CODE, Category.MINOR, message);
        this.metricName = metricName;
    }

    /** Returns the name of the metric that overflowed. */
    public String getMetricName() { return metricName; }
}
