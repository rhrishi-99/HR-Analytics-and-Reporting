package com.hranalytics.dashboard;

import com.hranalytics.domain.FilterCriteria;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Assembled dashboard containing KPI cards, chart widgets, and applied filter metadata.
 * Returned by DashboardManager and published to the ESS Portal via HRAnalyticsFacade.
 * Owner: R G Rhrishi (dashboard layer).
 */
public class Dashboard {

    private final String dashboardId;
    private final List<KPICard> kpiCards;
    private final List<Widget> widgets;
    private final FilterCriteria appliedFilters;
    private final LocalDateTime generatedAt;
    private final List<String> insights;

    public Dashboard(String dashboardId, List<KPICard> kpiCards, List<Widget> widgets,
                     FilterCriteria appliedFilters, List<String> insights) {
        this.dashboardId = dashboardId;
        this.kpiCards = kpiCards;
        this.widgets = widgets;
        this.appliedFilters = appliedFilters;
        this.generatedAt = LocalDateTime.now();
        this.insights = insights;
    }

    public String getDashboardId()           { return dashboardId; }
    public List<KPICard> getKpiCards()       { return kpiCards; }
    public List<Widget> getWidgets()         { return widgets; }
    public FilterCriteria getAppliedFilters(){ return appliedFilters; }
    public LocalDateTime getGeneratedAt()    { return generatedAt; }
    public List<String> getInsights()        { return insights; }

    /** Renders the full dashboard to a formatted string for console/log output. */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════╗\n");
        sb.append("║         HR ANALYTICS DASHBOARD  [").append(dashboardId).append("]         ║\n");
        sb.append("║  Generated: ").append(generatedAt).append("  ║\n");
        sb.append("╚══════════════════════════════════════════════════════╝\n");

        sb.append("\n── KPI CARDS ─────────────────────────────────────────\n");
        kpiCards.forEach(k -> sb.append("  ").append(k).append("\n"));

        sb.append("\n── ANALYTICAL INSIGHTS ───────────────────────────────\n");
        insights.forEach(i -> sb.append("  • ").append(i).append("\n"));

        sb.append("\n── CHARTS ────────────────────────────────────────────\n");
        widgets.forEach(w -> sb.append(w.render()));

        return sb.toString();
    }
}
