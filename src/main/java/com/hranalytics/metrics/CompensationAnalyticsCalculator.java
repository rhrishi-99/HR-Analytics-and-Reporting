package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;

/**
 * Computes average gross compensation and per-department salary breakdown.
 * Overrides the computeBreakdown hook to expose department salary averages.
 * Extends MetricCalculator — only provides the domain-specific computation steps.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class CompensationAnalyticsCalculator extends MetricCalculator {

    @Override
    protected double computeCurrentValue(ProcessedData data) {
        int headcount = data.getTotalHeadcount();
        if (headcount == 0) throw new MetricCalculationOverflowException("COMPENSATION_ANALYTICS",
                "Headcount is 0 — cannot compute average compensation.");
        return data.getTotalGrossSalary() / headcount;
    }

    @Override
    protected double computePreviousValue(ProcessedData data) {
        return computeCurrentValue(data) * 0.97; // simulated prior-period baseline
    }

    /** Hook override — exposes per-department average salary map. */
    @Override
    protected Map<String, Double> computeBreakdown(ProcessedData data) {
        return data.getAvgSalaryByDepartment();
    }

    @Override protected MetricType getMetricType() { return MetricType.COMPENSATION_ANALYTICS; }
    @Override protected String getMetricName()     { return "Avg Compensation"; }
    @Override protected String getUnit()           { return "$"; }
}
