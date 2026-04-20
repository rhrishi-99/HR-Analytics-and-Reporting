package com.hranalytics.charts;

import com.hranalytics.metrics.MetricResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concrete factory that creates charts themed for compensation analytics.
 * Produces line/bar/pie charts seeded with COMPENSATION_ANALYTICS metric data.
 * Pattern: Abstract Factory (Creational). Owner: R G Rhrishi.
 */
public class CompensationChartFactory implements ChartFactory {

    @Override
    public LineChart createLineChart(MetricResult result) {
        List<String> quarters = List.of("Q1 2024", "Q2 2024", "Q3 2024", "Q4 2024", "Q1 2025");
        double avg = result.getCurrentValue();
        List<Double> values = List.of(avg * 0.92, avg * 0.95, avg * 0.97, avg * 0.99, avg);
        return new LineChart(
                "CP-LINE-" + shortId(),
                "Avg Compensation Trend ($)",
                quarters, values, "#9C27B0");
    }

    @Override
    public BarChart createBarChart(MetricResult result) {
        // Use per-department breakdown if available
        Map<String, Double> breakdown = result.getBreakdown();
        List<String> labels;
        List<Double> values;
        if (breakdown.isEmpty()) {
            labels = List.of("Engineering", "HR", "Finance");
            values = List.of(86000.0, 62500.0, 96250.0);
        } else {
            labels = List.copyOf(breakdown.keySet());
            values = List.copyOf(breakdown.values());
        }
        return new BarChart("CP-BAR-" + shortId(), "Avg Salary by Department ($)", labels, values, "#673AB7");
    }

    @Override
    public PieChart createPieChart(MetricResult result) {
        Map<String, Double> breakdown = result.getBreakdown();
        List<String> labels;
        List<Double> values;
        if (breakdown.isEmpty()) {
            labels = List.of("Engineering", "HR", "Finance");
            values = List.of(46.0, 17.0, 37.0);
        } else {
            labels = List.copyOf(breakdown.keySet());
            values = List.copyOf(breakdown.values());
        }
        return new PieChart("CP-PIE-" + shortId(), "Salary Budget Distribution", labels, values);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
