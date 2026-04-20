package com.hranalytics.charts;

import com.hranalytics.metrics.MetricResult;

/**
 * Abstract Factory interface for creating families of related charts.
 * Each concrete factory produces charts pre-configured for one analytics context
 * (employee growth, attrition, compensation). DashboardManager depends only on this interface.
 * To add a new chart theme: implement a new ChartFactory — do not modify DashboardManager.
 * Pattern: Abstract Factory (Creational). Owner: R G Rhrishi.
 */
public interface ChartFactory {

    /**
     * Creates a line chart suitable for trend analysis in this factory's analytics context.
     *
     * @param result the metric result whose data will seed the chart
     * @return a configured LineChart
     */
    LineChart createLineChart(MetricResult result);

    /**
     * Creates a bar chart for categorical comparison in this factory's analytics context.
     *
     * @param result the metric result whose breakdown data will seed the chart
     * @return a configured BarChart
     */
    BarChart createBarChart(MetricResult result);

    /**
     * Creates a pie chart for proportional distribution in this factory's analytics context.
     *
     * @param result the metric result whose breakdown data will seed the chart
     * @return a configured PieChart
     */
    PieChart createPieChart(MetricResult result);
}
