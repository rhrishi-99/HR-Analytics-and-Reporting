package com.hranalytics.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete chart that displays proportional data as a pie/donut breakdown.
 * Created exclusively by ChartFactory implementations — never instantiated directly by DashboardManager.
 * Pattern: Abstract Factory product. Owner: R G Rhrishi.
 */
public class PieChart extends Chart {

    private List<String> labels;
    private List<Double> values;   // raw values; percentages are computed on render

    public PieChart(String chartId, String title, List<String> labels, List<Double> values) {
        super(chartId, title, "PIE");
        this.labels = labels;
        this.values = values;
        this.dataPoints = buildDataPoints();
    }

    @Override
    public String render() {
        double total = values.stream().mapToDouble(Double::doubleValue).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("\n┌─ ").append(title).append(" (Pie Chart) ──────────────────────┐\n");
        String[] slices = {"◼", "◻", "▪", "▫", "●", "○"};
        for (int i = 0; i < labels.size(); i++) {
            double pct = (total > 0) ? (values.get(i) / total) * 100.0 : 0.0;
            String symbol = slices[i % slices.length];
            sb.append(String.format("│ %s %-14s %5.1f%%%n", symbol, labels.get(i), pct));
        }
        sb.append("└────────────────────────────────────────────────┘");
        return sb.toString();
    }

    @Override
    public void update(List<String> newLabels, List<Double> newValues) {
        this.labels = newLabels;
        this.values = newValues;
        this.dataPoints = buildDataPoints();
    }

    private List<String> buildDataPoints() {
        List<String> pts = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            pts.add(labels.get(i) + "=" + values.get(i));
        }
        return pts;
    }
}
