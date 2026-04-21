package com.hranalytics.metrics;

import com.hranalytics.domain.Employee;
import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes per-department headcount as a percentage of total workforce.
 * Overrides the computeBreakdown hook to produce the department distribution map.
 * Extends MetricCalculator — only provides the domain-specific computation steps.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class DepartmentMetricsCalculator extends MetricCalculator {

    @Override
    protected double computeCurrentValue(ProcessedData data) {
        int total = data.getTotalHeadcount();
        if (total == 0) throw new MetricCalculationOverflowException("DEPARTMENT_METRICS",
                "Total headcount is 0 — cannot compute department distribution.");
        return computeBreakdown(data).values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    @Override
    protected double computePreviousValue(ProcessedData data) {
        return computeCurrentValue(data) * 1.05; // simulated baseline
    }

    /** Hook override — produces the full department → share % breakdown. */
    @Override
    protected Map<String, Double> computeBreakdown(ProcessedData data) {
        Map<String, List<Employee>> byDept = data.getEmployeesByDepartment();
        int total = data.getTotalHeadcount();
        Map<String, Double> breakdown = new HashMap<>();
        for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
            breakdown.put(entry.getKey(), ((double) entry.getValue().size() / total) * 100.0);
        }
        return breakdown;
    }

    @Override protected MetricType getMetricType() { return MetricType.DEPARTMENT_METRICS; }
    @Override protected String getMetricName()     { return "Dept Headcount Distribution"; }
    @Override protected String getUnit()           { return "%"; }
}
