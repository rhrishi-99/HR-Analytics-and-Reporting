package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

/**
 * Real Subject for the Proxy pattern — computes average gross compensation and per-department salary breakdown.
 * Contains only domain logic; cross-cutting concerns (logging, overflow recovery) live in MetricCalculatorProxy.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class CompensationAnalyticsCalculator implements MetricCalculator {

    /** Computes average compensation. Throws MetricCalculationOverflowException if headcount is zero. */
    @Override
    public MetricResult calculate(ProcessedData data) {
        int headcount = data.getTotalHeadcount();
        if (headcount == 0) throw new MetricCalculationOverflowException("COMPENSATION_ANALYTICS",
                "Headcount is 0 — cannot compute average compensation.");
        double avgComp  = data.getTotalGrossSalary() / headcount;
        double prevComp = avgComp * 0.97;
        return new MetricResult(MetricType.COMPENSATION_ANALYTICS, "Avg Compensation",
                avgComp, prevComp, "$", data.getAvgSalaryByDepartment());
    }

    @Override public MetricType getMetricType() { return MetricType.COMPENSATION_ANALYTICS; }
    @Override public String getMetricName()      { return "Avg Compensation"; }
    @Override public String getUnit()            { return "$"; }
}
