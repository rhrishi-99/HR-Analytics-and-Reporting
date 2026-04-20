package com.hranalytics.metrics;

import com.hranalytics.domain.Performance;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes the average performance score across all employees with a review in the dataset.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class AveragePerformanceStrategy implements MetricStrategy {

    private static final Logger LOG = Logger.getLogger(AveragePerformanceStrategy.class.getName());

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            List<Performance> records = data.getPerformanceRecords();
            if (records.isEmpty()) {
                throw new MetricCalculationOverflowException("AVERAGE_PERFORMANCE",
                        "No performance records available. Inputs: recordCount=0");
            }

            double avg = records.stream()
                    .mapToDouble(Performance::getScore)
                    .average()
                    .orElseThrow(() -> new MetricCalculationOverflowException(
                            "AVERAGE_PERFORMANCE", "Stream average returned empty."));

            double prevAvg = avg * 0.95; // simulated prior-period baseline

            LOG.info(String.format("AveragePerformanceStrategy: records=%d, avg=%.2f",
                    records.size(), avg));

            return new MetricResult(MetricType.AVERAGE_PERFORMANCE, "Avg Performance Score",
                    avg, prevAvg, "/5.0", Map.of());

        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW: " + ex.getMessage());
            return new MetricResult(MetricType.AVERAGE_PERFORMANCE, "Avg Performance Score", "/5.0");
        }
    }
}
