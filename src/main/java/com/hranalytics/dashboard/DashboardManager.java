package com.hranalytics.dashboard;

import com.hranalytics.charts.Chart;
import com.hranalytics.charts.ChartFactory;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.exceptions.DashboardWidgetRenderFailureException;
import com.hranalytics.metrics.MetricResult;
import com.hranalytics.metrics.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Assembles a Dashboard from metric results, analytics insights, and charts.
 * Depends on ChartFactory (not concrete Chart subclasses) — new chart families require
 * only a new ChartFactory implementation; this class is never modified.
 * Pattern: Abstract Factory consumer. Owner: R G Rhrishi.
 */
public class DashboardManager {

    private static final Logger LOG = Logger.getLogger(DashboardManager.class.getName());

    private final ChartFactory chartFactory;

    public DashboardManager(ChartFactory chartFactory) {
        this.chartFactory = chartFactory;
    }

    /**
     * Builds a fully assembled Dashboard from metric results and insights.
     * Each widget render is isolated — a DASHBOARD_WIDGET_RENDER_FAILURE shows a placeholder
     * without blocking other widgets from rendering.
     */
    public Dashboard buildDashboard(String userId, FilterCriteria filters,
                                    Map<MetricType, MetricResult> metrics,
                                    List<String> insights) {
        String dashboardId = "DB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        List<KPICard> kpiCards = buildKPICards(metrics);
        List<Widget> widgets   = buildWidgets(metrics);

        LOG.info("DashboardManager: built dashboard " + dashboardId
                + " with " + kpiCards.size() + " KPIs and " + widgets.size() + " widgets.");

        return new Dashboard(dashboardId, kpiCards, widgets, filters, insights);
    }

    /** Converts each MetricResult into a KPI summary card. */
    private List<KPICard> buildKPICards(Map<MetricType, MetricResult> metrics) {
        List<KPICard> cards = new ArrayList<>();
        for (MetricResult result : metrics.values()) {
            cards.add(new KPICard(
                    result.getMetricName(),
                    result.getCurrentValue(),
                    result.getPreviousValue(),
                    result.getTrend(),
                    result.getUnit(),
                    result.isOverflowed()));
        }
        return cards;
    }

    /** Creates chart widgets using the injected factory; isolates per-widget failures. */
    private List<Widget> buildWidgets(Map<MetricType, MetricResult> metrics) {
        List<Widget> widgets = new ArrayList<>();

        // Growth trend line chart
        widgets.add(safeCreateWidget("W-EG-LINE", () -> {
            MetricResult egr = metrics.get(MetricType.EMPLOYEE_GROWTH);
            return egr != null ? chartFactory.createLineChart(egr) : null;
        }));

        // Attrition trend line chart
        widgets.add(safeCreateWidget("W-AT-LINE", () -> {
            MetricResult atr = metrics.get(MetricType.ATTRITION_RATE);
            return atr != null ? chartFactory.createLineChart(atr) : null;
        }));

        // Department bar chart
        widgets.add(safeCreateWidget("W-DM-BAR", () -> {
            MetricResult dmr = metrics.get(MetricType.DEPARTMENT_METRICS);
            return dmr != null ? chartFactory.createBarChart(dmr) : null;
        }));

        // Compensation pie chart
        widgets.add(safeCreateWidget("W-CP-PIE", () -> {
            MetricResult cpr = metrics.get(MetricType.COMPENSATION_ANALYTICS);
            return cpr != null ? chartFactory.createPieChart(cpr) : null;
        }));

        return widgets;
    }

    /** Wraps chart creation in error isolation — returns a placeholder widget on failure. */
    private Widget safeCreateWidget(String widgetId, java.util.function.Supplier<Chart> supplier) {
        try {
            Chart chart = supplier.get();
            if (chart == null) {
                return new Widget(widgetId, "No data available for this widget.");
            }
            return new Widget(widgetId, chart);
        } catch (Exception ex) {
            DashboardWidgetRenderFailureException failure =
                    new DashboardWidgetRenderFailureException(widgetId,
                            "Widget " + widgetId + " failed to render: " + ex.getMessage(), ex);
            LOG.warning(failure.toString());
            return new Widget(widgetId, "Data Unavailable");
        }
    }
}
