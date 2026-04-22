package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.logging.Logger;

/**
 * Proxy for MetricCalculator — intercepts calculate() calls to add cross-cutting concerns:
 *   1. Pre/post logging
 *   2. METRIC_CALCULATION_OVERFLOW handling (substitutes 0.0 and flags the result)
 * Real calculators throw freely; this class handles recovery in one place.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class MetricCalculatorProxy implements MetricCalculator {

    private static final Logger LOG = Logger.getLogger(MetricCalculatorProxy.class.getName());

    private final MetricCalculator real;

    /** Wraps any MetricCalculator real subject. */
    public MetricCalculatorProxy(MetricCalculator real) {
        this.real = real;
    }

    /**
     * Delegates to the real calculator after logging the request.
     * Catches MetricCalculationOverflowException and returns a flagged MetricResult instead of propagating.
     */
    @Override
    public MetricResult calculate(ProcessedData data) {
        LOG.info("Proxy: delegating calculate() for " + real.getMetricType());
        try {
            MetricResult result = real.calculate(data);
            LOG.info("Proxy: " + real.getMetricType() + " → " + result);
            return result;
        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW [" + real.getMetricType() + "]: " + ex.getMessage());
            return new MetricResult(real.getMetricType(), real.getMetricName(), real.getUnit());
        }
    }

    @Override public MetricType getMetricType() { return real.getMetricType(); }
    @Override public String getMetricName()      { return real.getMetricName(); }
    @Override public String getUnit()            { return real.getUnit(); }
}
