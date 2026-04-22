package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

/**
 * Subject interface for the Proxy pattern.
 * Every real calculator and the proxy share this contract.
 * MetricCalculatorProxy wraps any MetricCalculator to add logging and overflow handling.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 *
 * To add a new metric: implement this interface, register in MetricsCalculationEngine — done.
 */
public interface MetricCalculator {

    /** Computes and returns the metric result for the given processed data. */
    MetricResult calculate(ProcessedData data);

    /** Returns the MetricType enum constant that identifies this calculator. */
    MetricType getMetricType();

    /** Returns the human-readable metric name shown on KPI cards. */
    String getMetricName();

    /** Returns the unit string shown alongside the value (e.g. "%", "/5.0", "$"). */
    String getUnit();
}
