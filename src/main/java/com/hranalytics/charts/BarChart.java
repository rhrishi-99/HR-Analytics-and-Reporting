package com.hranalytics.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete chart that displays categorical data as horizontal bars.
 * Created exclusively by ChartFactory implementations — never instantiated directly by DashboardManager.
 * Pattern: Abstract Factory product. Owner: R G Rhrishi.
 */
public class BarChart extends Chart {

    private List<String> categories;
    private List<Double> values;
    private String color;

    public BarChart(String chartId, String title, List<String> categories, List<Double> values, String color) {
        super(chartId, title, "BAR");
        this.categories = categories;
        this.values = values;
        this.color = color;
        this.dataPoints = buildDataPoints();
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n┌─ ").append(title).append(" (Bar Chart) ──────────────────────┐\n");
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        for (int i = 0; i < categories.size(); i++) {
            int bars = (int) ((values.get(i) / max) * 28);
            sb.append(String.format("│ %-12s │ %s %.2f%n",
                    categories.get(i), "█".repeat(Math.max(1, bars)), values.get(i)));
        }
        sb.append("└────────────────────────────────────────────────┘");
        return sb.toString();
    }

    @Override
    public void update(List<String> labels, List<Double> newValues) {
        this.categories = labels;
        this.values = newValues;
        this.dataPoints = buildDataPoints();
    }

    private List<String> buildDataPoints() {
        List<String> pts = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            pts.add(categories.get(i) + "=" + values.get(i));
        }
        return pts;
    }

    public String getColor() { return color; }
}
