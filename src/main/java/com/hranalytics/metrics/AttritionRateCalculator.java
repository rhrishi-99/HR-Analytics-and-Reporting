package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

/**
 * Computes attrition rate: separations / average headcount × 100.
 * Extends MetricCalculator — only provides the domain-specific computation steps.
 * The algorithm skeleton (overflow handling, result construction) lives in MetricCalculator.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class AttritionRateCalculator extends MetricCalculator {

    @Override
    protected double computeCurrentValue(ProcessedData data) {
        int separations = data.getSeparations();
        double avgHeadcount = (data.getTotalHeadcount() + data.getPreviousHeadcount()) / 2.0;
        if (avgHeadcount == 0) throw new MetricCalculationOverflowException("ATTRITION_RATE",
                "Average headcount is 0. current=" + data.getTotalHeadcount()
                + ", previous=" + data.getPreviousHeadcount());
        return (separations / avgHeadcount) * 100.0;
    }

    @Override
    protected double computePreviousValue(ProcessedData data) {
        return computeCurrentValue(data) * 0.85; // simulated prior-period baseline
    }

    @Override protected MetricType getMetricType() { return MetricType.ATTRITION_RATE; }
    @Override protected String getMetricName()     { return "Attrition Rate"; }
    @Override protected String getUnit()           { return "%"; }
}
