package com.hranalytics.charts;

import java.util.List;

/**
 * Abstract base class for all chart types in the HR Analytics dashboard.
 * Participates in the Abstract Factory pattern as the product hierarchy.
 * Concrete subclasses: LineChart, BarChart, PieChart.
 * Pattern: Abstract Factory (Creational). Owner: R G Rhrishi.
 */
public abstract class Chart {

    protected String chartId;
    protected String title;
    protected String chartType;
    protected List<String> dataPoints; // serialisable snapshot of rendered data

    protected Chart(String chartId, String title, String chartType) {
        this.chartId = chartId;
        this.title = title;
        this.chartType = chartType;
    }

    /**
     * Renders the chart as a text representation (simulates visual rendering).
     * Returns the rendered output as a String.
     */
    public abstract String render();

    /**
     * Updates the chart's underlying data and re-renders.
     *
     * @param labels new category labels / x-axis values
     * @param values new numeric values
     */
    public abstract void update(List<String> labels, List<Double> values);

    /**
     * Simulates exporting the chart as an image file.
     *
     * @return the file path of the exported image
     */
    public String exportAsImage() {
        String path = "output/charts/" + chartId + ".png";
        System.out.println("  [Chart] Exported '" + title + "' to " + path);
        return path;
    }

    public String getChartId()   { return chartId; }
    public String getTitle()     { return title; }
    public String getChartType() { return chartType; }
    public List<String> getDataPoints() { return dataPoints; }
}
