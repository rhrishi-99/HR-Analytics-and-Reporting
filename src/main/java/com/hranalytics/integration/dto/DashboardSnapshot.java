package com.hranalytics.integration.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight DTO representing a rendered dashboard, published to external consumers.
 * Contains the fields specified in Integration 2:
 * dashboardId, widgetId references, kpiCards, attritionRate, employeeGrowth, avgPerformance.
 *
 * External teams import only this class — no internal Dashboard or Chart types are exposed.
 */
public class DashboardSnapshot {

    private final String dashboardId;
    private final List<String> widgetIds;          // IDs of rendered chart widgets
    private final List<KPISnapshot> kpiCards;
    private final double attritionRate;            // convenience shortcut for ESS Portal
    private final double employeeGrowth;           // convenience shortcut for ESS Portal
    private final double avgPerformance;           // convenience shortcut for ESS Portal
    private final List<String> insights;           // human-readable analytical summaries
    private final LocalDateTime generatedAt;

    public DashboardSnapshot(String dashboardId, List<String> widgetIds,
                             List<KPISnapshot> kpiCards,
                             double attritionRate, double employeeGrowth, double avgPerformance,
                             List<String> insights) {
        this.dashboardId = dashboardId;
        this.widgetIds = widgetIds;
        this.kpiCards = kpiCards;
        this.attritionRate = attritionRate;
        this.employeeGrowth = employeeGrowth;
        this.avgPerformance = avgPerformance;
        this.insights = insights;
        this.generatedAt = LocalDateTime.now();
    }

    public String getDashboardId()       { return dashboardId; }
    public List<String> getWidgetIds()   { return widgetIds; }
    public List<KPISnapshot> getKpiCards(){ return kpiCards; }
    public double getAttritionRate()     { return attritionRate; }
    public double getEmployeeGrowth()    { return employeeGrowth; }
    public double getAvgPerformance()    { return avgPerformance; }
    public List<String> getInsights()    { return insights; }
    public LocalDateTime getGeneratedAt(){ return generatedAt; }

    @Override
    public String toString() {
        return String.format("DashboardSnapshot[id=%s, kpis=%d, widgets=%d, generated=%s]",
                dashboardId, kpiCards.size(), widgetIds.size(), generatedAt);
    }
}
