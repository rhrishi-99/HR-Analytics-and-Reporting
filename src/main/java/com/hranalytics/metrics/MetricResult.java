package com.hranalytics.metrics;

import java.util.Map;

/**
 * Immutable value object returned by every MetricStrategy.calculate() call.
 * Carries the computed metric value along with trend and display metadata.
 * Owner: Raihan Naeem (Strategy pattern).
 */
public class MetricResult {

    private final MetricType type;
    private final String metricName;
    private final double currentValue;
    private final double previousValue;
    private final String trend;         // "UP", "DOWN", "STABLE"
    private final String unit;          // "%", "count", "$", etc.
    private final boolean overflowed;   // true if METRIC_CALCULATION_OVERFLOW was caught
    private final Map<String, Double> breakdown; // optional per-dept or per-category breakdown

    public MetricResult(MetricType type, String metricName,
                        double currentValue, double previousValue,
                        String unit, Map<String, Double> breakdown) {
        this.type = type;
        this.metricName = metricName;
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.unit = unit;
        this.breakdown = breakdown;
        this.overflowed = false;
        this.trend = computeTrend(currentValue, previousValue);
    }

    /** Constructor used when METRIC_CALCULATION_OVERFLOW is caught — substitutes 0.0 and flags the card. */
    public MetricResult(MetricType type, String metricName, String unit) {
        this.type = type;
        this.metricName = metricName;
        this.currentValue = 0.0;
        this.previousValue = 0.0;
        this.unit = unit;
        this.breakdown = Map.of();
        this.overflowed = true;
        this.trend = "STABLE";
    }

    private static String computeTrend(double current, double previous) {
        if (current > previous) return "UP";
        if (current < previous) return "DOWN";
        return "STABLE";
    }

    public MetricType getType()          { return type; }
    public String getMetricName()        { return metricName; }
    public double getCurrentValue()      { return currentValue; }
    public double getPreviousValue()     { return previousValue; }
    public String getTrend()             { return trend; }
    public String getUnit()              { return unit; }
    public boolean isOverflowed()        { return overflowed; }
    public Map<String, Double> getBreakdown() { return breakdown; }

    @Override
    public String toString() {
        return String.format("MetricResult[%s: %.2f%s (prev=%.2f, trend=%s%s)]",
                metricName, currentValue, unit, previousValue, trend,
                overflowed ? ", FLAGGED" : "");
    }
}
