package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;

/**
 * Real Subject for the Proxy pattern — computes attrition rate: separations / average headcount × 100.
 * Contains only domain logic; cross-cutting concerns (logging, overflow recovery) live in MetricCalculatorProxy.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class AttritionRateCalculator implements MetricCalculator {

    /** Computes attrition rate. Throws MetricCalculationOverflowException if headcount is zero. */
    @Override
    public MetricResult calculate(ProcessedData data) {
        int separations = data.getSeparations();
        double avgHeadcount = (data.getTotalHeadcount() + data.getPreviousHeadcount()) / 2.0;
        if (avgHeadcount == 0) throw new MetricCalculationOverflowException("ATTRITION_RATE",
                "Average headcount is 0. current=" + data.getTotalHeadcount()
                + ", previous=" + data.getPreviousHeadcount());
        double attritionRate = (separations / avgHeadcount) * 100.0;
        double prevRate = attritionRate * 0.85;
        return new MetricResult(MetricType.ATTRITION_RATE, "Attrition Rate",
                attritionRate, prevRate, "%", Map.of());
    }

    @Override public MetricType getMetricType() { return MetricType.ATTRITION_RATE; }
    @Override public String getMetricName()      { return "Attrition Rate"; }
    @Override public String getUnit()            { return "%"; }
}
