package com.hranalytics.integration;

import com.hranalytics.dashboard.Dashboard;
import com.hranalytics.dashboard.KPICard;

import java.util.List;
import java.util.logging.Logger;

/**
 * Integration 2 — Outbound adapter for the ESS Portal / Manager Dashboard.
 * Publishes dashboard snapshots and KPI data to the Employee Self-Service portal.
 * Called internally by HRAnalyticsFacade; ESS Portal partners must never reach pipeline internals.
 * Owner: Prem M Thakur (integration boundary).
 */
public class ESSPortalPublisher {

    private static final Logger LOG = Logger.getLogger(ESSPortalPublisher.class.getName());

    /**
     * Publishes the full dashboard snapshot to the ESS Portal.
     * In production this would POST to the ESS Portal REST endpoint.
     */
    public void publishDashboard(Dashboard dashboard) {
        LOG.info("ESSPortalPublisher: publishing dashboard " + dashboard.getDashboardId()
                + " to ESS Portal.");
        // Simulated outbound call
        System.out.println("  [ESS Portal] Dashboard published → ID: " + dashboard.getDashboardId()
                + ", KPIs: " + dashboard.getKpiCards().size()
                + ", Widgets: " + dashboard.getWidgets().size());
    }

    /**
     * Publishes a lightweight KPI snapshot for a specific department to the Manager Dashboard.
     * Fields published: dashboardId, widgetId, kpiCards (metricName, currentValue,
     * previousValue, trend), attritionRate, employeeGrowth, avgPerformance.
     */
    public void publishKPISnapshot(String dashboardId, String deptId, List<KPICard> kpiCards) {
        LOG.info("ESSPortalPublisher: publishing KPI snapshot for dept=" + deptId);
        System.out.println("  [ESS Portal] KPI Snapshot → dashboardId=" + dashboardId
                + ", dept=" + deptId);
        for (KPICard card : kpiCards) {
            System.out.println("    • " + card.getMetricName()
                    + ": " + String.format("%.2f%s", card.getCurrentValue(), card.getUnit())
                    + " [" + card.getTrend() + "]"
                    + (card.isFlagged() ? " ⚠" : ""));
        }
    }
}
