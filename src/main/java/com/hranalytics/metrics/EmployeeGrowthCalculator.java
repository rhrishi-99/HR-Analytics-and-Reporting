package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

/**
 * Computes employee growth: (current - previous) / previous × 100.
 * Extends MetricCalculator — only provides the domain-specific computation steps.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class EmployeeGrowthCalculator extends MetricCalculator {

    @Override
    protected double computeCurrentValue(ProcessedData data) {
        int current  = data.getTotalHeadcount();
        int previous = data.getPreviousHeadcount();
        if (previous == 0) throw new MetricCalculationOverflowException("EMPLOYEE_GROWTH",
                "Previous headcount is 0. current=" + current);
        return ((double)(current - previous) / previous) * 100.0;
    }

    @Override
    protected double computePreviousValue(ProcessedData data) {
        return computeCurrentValue(data) * 0.9; // simulated prior-period baseline
    }

    @Override protected MetricType getMetricType() { return MetricType.EMPLOYEE_GROWTH; }
    @Override protected String getMetricName()     { return "Employee Growth"; }
    @Override protected String getUnit()           { return "%"; }
}
