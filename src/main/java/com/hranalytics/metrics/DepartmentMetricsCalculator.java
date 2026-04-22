package com.hranalytics.metrics;

import com.hranalytics.domain.Employee;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Subject for the Proxy pattern — computes per-department headcount as a percentage of total workforce.
 * Contains only domain logic; cross-cutting concerns (logging, overflow recovery) live in MetricCalculatorProxy.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class DepartmentMetricsCalculator implements MetricCalculator {

    /** Computes dept distribution. Throws MetricCalculationOverflowException if total headcount is zero. */
    @Override
    public MetricResult calculate(ProcessedData data) {
        int total = data.getTotalHeadcount();
        if (total == 0) throw new MetricCalculationOverflowException("DEPARTMENT_METRICS",
                "Total headcount is 0 — cannot compute department distribution.");
        Map<String, List<Employee>> byDept = data.getEmployeesByDepartment();
        Map<String, Double> breakdown = new HashMap<>();
        for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
            breakdown.put(entry.getKey(), ((double) entry.getValue().size() / total) * 100.0);
        }
        double maxShare  = breakdown.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double prevShare = maxShare * 1.05;
        return new MetricResult(MetricType.DEPARTMENT_METRICS, "Dept Headcount Distribution",
                maxShare, prevShare, "%", breakdown);
    }

    @Override public MetricType getMetricType() { return MetricType.DEPARTMENT_METRICS; }
    @Override public String getMetricName()      { return "Dept Headcount Distribution"; }
    @Override public String getUnit()            { return "%"; }
}
