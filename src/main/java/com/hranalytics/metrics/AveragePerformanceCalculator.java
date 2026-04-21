package com.hranalytics.metrics;

import com.hranalytics.domain.Performance;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.List;

/**
 * Computes average performance score across all employees with a review in the dataset.
 * Extends MetricCalculator — only provides the domain-specific computation steps.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class AveragePerformanceCalculator extends MetricCalculator {

    @Override
    protected double computeCurrentValue(ProcessedData data) {
        List<Performance> records = data.getPerformanceRecords();
        if (records.isEmpty()) throw new MetricCalculationOverflowException(
                "AVERAGE_PERFORMANCE", "No performance records available. recordCount=0");
        return records.stream().mapToDouble(Performance::getScore).average()
                .orElseThrow(() -> new MetricCalculationOverflowException(
                        "AVERAGE_PERFORMANCE", "Stream average returned empty."));
    }

    @Override
    protected double computePreviousValue(ProcessedData data) {
        return computeCurrentValue(data) * 0.95; // simulated prior-period baseline
    }

    @Override protected MetricType getMetricType() { return MetricType.AVERAGE_PERFORMANCE; }
    @Override protected String getMetricName()     { return "Avg Performance Score"; }
    @Override protected String getUnit()           { return "/5.0"; }
}
