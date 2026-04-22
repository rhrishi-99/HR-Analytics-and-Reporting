package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;

/**
 * Real Subject for the Proxy pattern — computes employee growth: (current - previous) / previous × 100.
 * Contains only domain logic; cross-cutting concerns (logging, overflow recovery) live in MetricCalculatorProxy.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class EmployeeGrowthCalculator implements MetricCalculator {

    /** Computes employee growth rate. Throws MetricCalculationOverflowException if previous headcount is zero. */
    @Override
    public MetricResult calculate(ProcessedData data) {
        int current  = data.getTotalHeadcount();
        int previous = data.getPreviousHeadcount();
        if (previous == 0) throw new MetricCalculationOverflowException("EMPLOYEE_GROWTH",
                "Previous headcount is 0. current=" + current);
        double growth     = ((double)(current - previous) / previous) * 100.0;
        double prevGrowth = growth * 0.9;
        return new MetricResult(MetricType.EMPLOYEE_GROWTH, "Employee Growth",
                growth, prevGrowth, "%", Map.of());
    }

    @Override public MetricType getMetricType() { return MetricType.EMPLOYEE_GROWTH; }
    @Override public String getMetricName()      { return "Employee Growth"; }
    @Override public String getUnit()            { return "%"; }
}
