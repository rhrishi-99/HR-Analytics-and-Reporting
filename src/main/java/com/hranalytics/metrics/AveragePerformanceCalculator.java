package com.hranalytics.metrics;

import com.hranalytics.domain.Performance;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.List;
import java.util.Map;

/**
 * Real Subject for the Proxy pattern — computes average performance score across all reviewed employees.
 * Contains only domain logic; cross-cutting concerns (logging, overflow recovery) live in MetricCalculatorProxy.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class AveragePerformanceCalculator implements MetricCalculator {

    /** Computes average performance. Throws MetricCalculationOverflowException if no records exist. */
    @Override
    public MetricResult calculate(ProcessedData data) {
        List<Performance> records = data.getPerformanceRecords();
        if (records.isEmpty()) throw new MetricCalculationOverflowException(
                "AVERAGE_PERFORMANCE", "No performance records available. recordCount=0");
        double avg = records.stream().mapToDouble(Performance::getScore).average()
                .orElseThrow(() -> new MetricCalculationOverflowException(
                        "AVERAGE_PERFORMANCE", "Stream average returned empty."));
        double prevAvg = avg * 0.95;
        return new MetricResult(MetricType.AVERAGE_PERFORMANCE, "Avg Performance Score",
                avg, prevAvg, "/5.0", Map.of());
    }

    @Override public MetricType getMetricType() { return MetricType.AVERAGE_PERFORMANCE; }
    @Override public String getMetricName()      { return "Avg Performance Score"; }
    @Override public String getUnit()            { return "/5.0"; }
}
