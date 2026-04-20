package com.hranalytics.metrics;

import com.hranalytics.domain.Employee;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes per-department headcount as a percentage of total workforce.
 * Returns the largest department share as the primary value, with a full breakdown in the result.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class DepartmentMetricsStrategy implements MetricStrategy {

    private static final Logger LOG = Logger.getLogger(DepartmentMetricsStrategy.class.getName());

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            Map<String, List<Employee>> byDept = data.getEmployeesByDepartment();
            int total = data.getTotalHeadcount();

            if (total == 0) {
                throw new MetricCalculationOverflowException("DEPARTMENT_METRICS",
                        "Total headcount is 0 — cannot compute department distribution.");
            }

            Map<String, Double> breakdown = new HashMap<>();
            for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
                double share = ((double) entry.getValue().size() / total) * 100.0;
                breakdown.put(entry.getKey(), share);
            }

            double maxShare = breakdown.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double prevMaxShare = maxShare * 1.05; // simulated baseline

            LOG.info("DepartmentMetricsStrategy: breakdown=" + breakdown);

            return new MetricResult(MetricType.DEPARTMENT_METRICS, "Dept Headcount Distribution",
                    maxShare, prevMaxShare, "%", breakdown);

        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW: " + ex.getMessage());
            return new MetricResult(MetricType.DEPARTMENT_METRICS, "Dept Headcount Distribution", "%");
        }
    }
}
