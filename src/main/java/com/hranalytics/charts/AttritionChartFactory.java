package com.hranalytics.charts;

import com.hranalytics.metrics.MetricResult;

import java.util.List;
import java.util.UUID;

/**
 * Concrete factory that creates charts themed for attrition analytics.
 * Produces line/bar/pie charts seeded with ATTRITION_RATE metric data.
 * Pattern: Abstract Factory (Creational). Owner: R G Rhrishi.
 */
public class AttritionChartFactory implements ChartFactory {

    @Override
    public LineChart createLineChart(MetricResult result) {
        List<String> months = List.of("Oct", "Nov", "Dec", "Jan", "Feb", "Mar");
        double rate = result.getCurrentValue();
        List<Double> values = List.of(rate * 1.2, rate * 1.1, rate * 0.95, rate, rate * 1.05, rate);
        return new LineChart(
                "AT-LINE-" + shortId(),
                "Monthly Attrition Rate (%)",
                months, values, "#F44336");
    }

    @Override
    public BarChart createBarChart(MetricResult result) {
        List<String> depts = List.of("Engineering", "HR", "Finance", "Overall");
        double overall = result.getCurrentValue();
        List<Double> rates = List.of(overall * 0.8, overall * 1.4, overall * 0.6, overall);
        return new BarChart(
                "AT-BAR-" + shortId(),
                "Attrition Rate by Department (%)",
                depts, rates, "#FF5722");
    }

    @Override
    public PieChart createPieChart(MetricResult result) {
        double attrition = result.getCurrentValue();
        double retention = 100.0 - attrition;
        return new PieChart(
                "AT-PIE-" + shortId(),
                "Retention vs Attrition",
                List.of("Retained", "Separated"),
                List.of(retention, attrition));
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
