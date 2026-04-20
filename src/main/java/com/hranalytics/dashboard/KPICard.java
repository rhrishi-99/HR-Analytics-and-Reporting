package com.hranalytics.dashboard;

/**
 * Value object representing a single KPI summary card on the dashboard.
 * Published to the ESS Portal via Integration 2.
 * Owner: R G Rhrishi (dashboard layer).
 */
public class KPICard {

    private final String metricName;
    private final double currentValue;
    private final double previousValue;
    private final String trend;     // "UP", "DOWN", "STABLE"
    private final String unit;
    private final boolean flagged;  // true if metric overflowed

    public KPICard(String metricName, double currentValue, double previousValue,
                   String trend, String unit, boolean flagged) {
        this.metricName = metricName;
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.trend = trend;
        this.unit = unit;
        this.flagged = flagged;
    }

    public String getMetricName()   { return metricName; }
    public double getCurrentValue() { return currentValue; }
    public double getPreviousValue(){ return previousValue; }
    public String getTrend()        { return trend; }
    public String getUnit()         { return unit; }
    public boolean isFlagged()      { return flagged; }

    @Override
    public String toString() {
        String flag = flagged ? " [DATA UNAVAILABLE]" : "";
        return String.format("KPI[%s: %.2f%s | prev=%.2f | trend=%s%s]",
                metricName, currentValue, unit, previousValue, trend, flag);
    }
}
