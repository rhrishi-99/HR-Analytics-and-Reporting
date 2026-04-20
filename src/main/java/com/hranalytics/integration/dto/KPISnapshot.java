package com.hranalytics.integration.dto;

/**
 * Lightweight DTO published to external consumers (ESS Portal, Manager Dashboard).
 * Carries exactly the KPI fields listed in Integration 2 of the data contract:
 * metricName, currentValue, previousValue, trend.
 *
 * External teams import only this class — no internal domain types are exposed.
 */
public class KPISnapshot {

    private final String metricName;
    private final double currentValue;
    private final double previousValue;
    private final String trend;   // "UP", "DOWN", "STABLE"
    private final String unit;    // "%", "/5.0", "$", etc.
    private final boolean flagged; // true if data was unavailable for this metric

    public KPISnapshot(String metricName, double currentValue, double previousValue,
                       String trend, String unit, boolean flagged) {
        this.metricName = metricName;
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.trend = trend;
        this.unit = unit;
        this.flagged = flagged;
    }

    public String getMetricName()    { return metricName; }
    public double getCurrentValue()  { return currentValue; }
    public double getPreviousValue() { return previousValue; }
    public String getTrend()         { return trend; }
    public String getUnit()          { return unit; }
    public boolean isFlagged()       { return flagged; }

    @Override
    public String toString() {
        return String.format("KPISnapshot[%s: %.2f%s | prev=%.2f | %s%s]",
                metricName, currentValue, unit, previousValue, trend,
                flagged ? " | UNAVAILABLE" : "");
    }
}
