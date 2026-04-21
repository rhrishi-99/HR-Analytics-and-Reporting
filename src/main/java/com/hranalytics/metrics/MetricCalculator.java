package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Abstract base class for all metric calculations.
 * Defines the algorithm skeleton via the Template Method pattern:
 *   computeCurrentValue() → computePreviousValue() → computeBreakdown() → build MetricResult
 * Overflow handling and result construction live here once — subclasses never duplicate them.
 *
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 *
 * To add a new metric:
 *   1. Extend MetricCalculator
 *   2. Implement the four abstract methods
 *   3. Register in MetricsCalculationEngine — done. Never edit this class.
 */
public abstract class MetricCalculator {

    private static final Logger LOG = Logger.getLogger(MetricCalculator.class.getName());

    /**
     * Template method — defines the fixed algorithm skeleton.
     * Subclasses provide the domain-specific steps via abstract methods.
     * Sealed with `final` so the skeleton cannot be overridden.
     */
    public final MetricResult calculate(ProcessedData data) {
        try {
            double current   = computeCurrentValue(data);   // subclass fills this step
            double previous  = computePreviousValue(data);  // subclass fills this step
            Map<String, Double> breakdown = computeBreakdown(data); // hook — default: empty

            LOG.info(String.format("[%s] current=%.2f, previous=%.2f, unit=%s",
                    getClass().getSimpleName(), current, previous, getUnit()));

            // Common result construction — lives here, not repeated in every subclass
            return new MetricResult(getMetricType(), getMetricName(),
                    current, previous, getUnit(), breakdown);

        } catch (MetricCalculationOverflowException ex) {
            // Common overflow handling — flagged result returned, never thrown to caller
            LOG.warning("METRIC_CALCULATION_OVERFLOW [" + getMetricType() + "]: " + ex.getMessage());
            return new MetricResult(getMetricType(), getMetricName(), getUnit()); // flagged
        }
    }

    // ── Abstract primitive operations — subclasses MUST implement ────────────

    /** Computes the metric value for the current period. Throw MetricCalculationOverflowException on bad data. */
    protected abstract double computeCurrentValue(ProcessedData data);

    /** Computes the baseline/previous-period value for trend comparison. */
    protected abstract double computePreviousValue(ProcessedData data);

    /** Returns the MetricType enum constant for this calculator. */
    protected abstract MetricType getMetricType();

    /** Returns the human-readable metric name shown on KPI cards. */
    protected abstract String getMetricName();

    /** Returns the unit string shown alongside the value (e.g. "%", "/5.0", "$"). */
    protected abstract String getUnit();

    // ── Hook operation — subclasses MAY override ─────────────────────────────

    /**
     * Returns a per-category breakdown map (e.g. department → share %).
     * Default returns an empty map. Override in calculators that produce breakdowns.
     */
    protected Map<String, Double> computeBreakdown(ProcessedData data) {
        return Map.of();
    }
}
