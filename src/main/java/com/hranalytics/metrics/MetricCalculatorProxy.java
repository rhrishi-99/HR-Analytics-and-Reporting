package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

/**
 * Proxy for MetricCalculator — intercepts calculate() calls to add:
 *   1. Simple console logging (tracing)
 *   2. Safety handling for METRIC_CALCULATION_OVERFLOW
 * Real calculators contain only domain logic; this proxy handles the "plumbing".
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class MetricCalculatorProxy implements MetricCalculator {

    private final MetricCalculator real;

    public MetricCalculatorProxy(MetricCalculator real) {
        this.real = real;
    }

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            // Proxy logic: execute and return
            MetricResult result = real.calculate(data);
            System.out.println("[Proxy] " + real.getMetricName() + " calculated successfully.");
            return result;
        } catch (MetricCalculationOverflowException ex) {
            // Proxy logic: catch known failure and return a safe recovery result (flagged)
            System.out.println("[Proxy] Warning: Overflow in " + real.getMetricName() + ". Returning safe default.");
            return new MetricResult(real.getMetricType(), real.getMetricName(), real.getUnit());
        }
    }

    // Direct delegation to real subject for metadata
    @Override public MetricType getMetricType() { return real.getMetricType(); }
    @Override public String getMetricName()      { return real.getMetricName(); }
    @Override public String getUnit()            { return real.getUnit(); }
}
