package com.hranalytics.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete chart that displays data as a line/trend series.
 * Created exclusively by ChartFactory implementations — never instantiated directly by DashboardManager.
 * Pattern: Abstract Factory product. Owner: R G Rhrishi.
 */
public class LineChart extends Chart {

    private List<String> xData;   // x-axis labels (e.g. quarters)
    private List<Double> yData;   // y-axis values
    private String color;

    public LineChart(String chartId, String title, List<String> xData, List<Double> yData, String color) {
        super(chartId, title, "LINE");
        this.xData = xData;
        this.yData = yData;
        this.color = color;
        this.dataPoints = buildDataPoints();
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n┌─ ").append(title).append(" (Line Chart) ─────────────────────┐\n");
        double max = yData.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        for (int i = 0; i < xData.size(); i++) {
            int bars = (int) ((yData.get(i) / max) * 30);
            sb.append(String.format("│ %-8s │ %s %.1f%n",
                    xData.get(i), "─".repeat(bars) + "●", yData.get(i)));
        }
        sb.append("└────────────────────────────────────────────────┘");
        return sb.toString();
    }

    @Override
    public void update(List<String> labels, List<Double> values) {
        this.xData = labels;
        this.yData = values;
        this.dataPoints = buildDataPoints();
    }

    private List<String> buildDataPoints() {
        List<String> pts = new ArrayList<>();
        for (int i = 0; i < xData.size(); i++) {
            pts.add(xData.get(i) + "=" + yData.get(i));
        }
        return pts;
    }

    public String getColor() { return color; }
}
