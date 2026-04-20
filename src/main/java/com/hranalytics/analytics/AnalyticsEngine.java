package com.hranalytics.analytics;

import com.hranalytics.metrics.MetricResult;
import com.hranalytics.metrics.MetricType;
import com.hranalytics.pipeline.ProcessedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Consumes computed MetricResults and generates human-readable analytical insights,
 * trend summaries, and comparative observations for DashboardManager and ReportGenerator.
 * Owner: Raihan Naeem (analytics layer).
 */
public class AnalyticsEngine {

    private static final Logger LOG = Logger.getLogger(AnalyticsEngine.class.getName());

    /**
     * Generates a list of insight strings from the metric results and processed data.
     * These insights appear in the dashboard summary panel and report narrative sections.
     */
    public List<String> generateInsights(Map<MetricType, MetricResult> metrics, ProcessedData data) {
        List<String> insights = new ArrayList<>();

        insights.add(getAttritionInsight(metrics));
        insights.add(getGrowthInsight(metrics));
        insights.add(getPerformanceInsight(metrics));
        insights.add(getCompensationInsight(metrics));
        insights.add(getDeptInsight(metrics, data));

        LOG.info("AnalyticsEngine: generated " + insights.size() + " insights.");
        return insights;
    }

    /** Returns a trend summary string for use in KPI card tooltips. */
    public String getTrendSummary(MetricResult result) {
        String direction = switch (result.getTrend()) {
            case "UP"   -> "increased";
            case "DOWN" -> "decreased";
            default     -> "remained stable";
        };
        return String.format("%s has %s from %.2f%s to %.2f%s.",
                result.getMetricName(), direction,
                result.getPreviousValue(), result.getUnit(),
                result.getCurrentValue(), result.getUnit());
    }

    private String getAttritionInsight(Map<MetricType, MetricResult> metrics) {
        MetricResult r = metrics.get(MetricType.ATTRITION_RATE);
        if (r == null) return "Attrition data unavailable.";
        if (r.isOverflowed()) return "Attrition rate could not be computed — flagged for review.";
        String level = r.getCurrentValue() > 15 ? "HIGH — immediate HR attention required"
                     : r.getCurrentValue() > 8  ? "MODERATE — monitor closely"
                     : "HEALTHY";
        return String.format("Attrition Rate is %.2f%% (%s). Trend: %s.",
                r.getCurrentValue(), level, r.getTrend());
    }

    private String getGrowthInsight(Map<MetricType, MetricResult> metrics) {
        MetricResult r = metrics.get(MetricType.EMPLOYEE_GROWTH);
        if (r == null) return "Growth data unavailable.";
        if (r.isOverflowed()) return "Employee growth could not be computed — flagged for review.";
        String dir = r.getCurrentValue() >= 0 ? "expanded" : "contracted";
        return String.format("Workforce has %s by %.2f%% this period. Trend: %s.",
                dir, Math.abs(r.getCurrentValue()), r.getTrend());
    }

    private String getPerformanceInsight(Map<MetricType, MetricResult> metrics) {
        MetricResult r = metrics.get(MetricType.AVERAGE_PERFORMANCE);
        if (r == null) return "Performance data unavailable.";
        if (r.isOverflowed()) return "Performance score could not be computed — flagged for review.";
        String quality = r.getCurrentValue() >= 4.0 ? "excellent" : r.getCurrentValue() >= 3.0 ? "good" : "needs improvement";
        return String.format("Average performance score is %.2f/5.0 (%s). Trend: %s.",
                r.getCurrentValue(), quality, r.getTrend());
    }

    private String getCompensationInsight(Map<MetricType, MetricResult> metrics) {
        MetricResult r = metrics.get(MetricType.COMPENSATION_ANALYTICS);
        if (r == null) return "Compensation data unavailable.";
        if (r.isOverflowed()) return "Compensation analytics could not be computed — flagged for review.";
        return String.format("Average gross compensation is $%.2f. Trend: %s.", r.getCurrentValue(), r.getTrend());
    }

    private String getDeptInsight(Map<MetricType, MetricResult> metrics, ProcessedData data) {
        MetricResult r = metrics.get(MetricType.DEPARTMENT_METRICS);
        if (r == null || r.getBreakdown().isEmpty()) return "Department distribution data unavailable.";
        String largest = r.getBreakdown().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + String.format("%.1f", e.getValue()) + "%)")
                .orElse("unknown");
        return "Largest department by headcount share: " + largest + ".";
    }
}
