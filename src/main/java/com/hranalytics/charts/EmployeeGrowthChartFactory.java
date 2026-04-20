package com.hranalytics.charts;

import com.hranalytics.metrics.MetricResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete factory that creates charts themed for employee growth analytics.
 * Produces line/bar/pie charts seeded with EMPLOYEE_GROWTH metric data.
 * Pattern: Abstract Factory (Creational). Owner: R G Rhrishi.
 */
public class EmployeeGrowthChartFactory implements ChartFactory {

    @Override
    public LineChart createLineChart(MetricResult result) {
        // Quarter-by-quarter simulated growth trend
        List<String> quarters = List.of("Q1 2024", "Q2 2024", "Q3 2024", "Q4 2024", "Q1 2025");
        double base = 100 + result.getPreviousValue();
        List<Double> values = List.of(base, base * 1.03, base * 1.06, base * 1.09, base * 1.12);
        return new LineChart(
                "EG-LINE-" + shortId(),
                "Employee Growth Trend",
                quarters, values, "#4CAF50");
    }

    @Override
    public BarChart createBarChart(MetricResult result) {
        List<String> depts = List.of("Engineering", "HR", "Finance");
        List<Double> headcounts = List.of(8.0, 4.0, 4.0);
        return new BarChart(
                "EG-BAR-" + shortId(),
                "Headcount by Department",
                depts, headcounts, "#2196F3");
    }

    @Override
    public PieChart createPieChart(MetricResult result) {
        // Use breakdown if available; fall back to defaults
        Map<String, Double> breakdown = result.getBreakdown();
        List<String> labels;
        List<Double> values;
        if (breakdown.isEmpty()) {
            labels = List.of("Engineering", "HR", "Finance");
            values = List.of(53.3, 26.7, 20.0);
        } else {
            labels = List.copyOf(breakdown.keySet());
            values = List.copyOf(breakdown.values());
        }
        return new PieChart("EG-PIE-" + shortId(), "Headcount Distribution", labels, values);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
