package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes employee growth as: (current headcount - previous headcount) / previous headcount × 100.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class EmployeeGrowthStrategy implements MetricStrategy {

    private static final Logger LOG = Logger.getLogger(EmployeeGrowthStrategy.class.getName());

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            int current  = data.getTotalHeadcount();
            int previous = data.getPreviousHeadcount();

            if (previous == 0) {
                throw new MetricCalculationOverflowException("EMPLOYEE_GROWTH",
                        "Previous headcount is 0 — cannot compute growth rate. Inputs: current=" + current);
            }

            double growthRate     = ((double)(current - previous) / previous) * 100.0;
            double prevGrowthRate = growthRate * 0.9; // simulated prior-period baseline

            LOG.info(String.format("EmployeeGrowthStrategy: current=%d, previous=%d, growth=%.2f%%",
                    current, previous, growthRate));

            return new MetricResult(MetricType.EMPLOYEE_GROWTH, "Employee Growth",
                    growthRate, prevGrowthRate, "%", Map.of());

        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW: " + ex.getMessage());
            return new MetricResult(MetricType.EMPLOYEE_GROWTH, "Employee Growth", "%");
        }
    }
}
