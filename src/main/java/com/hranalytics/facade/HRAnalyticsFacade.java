package com.hranalytics.facade;

import com.hranalytics.access.AccessControlModule;
import com.hranalytics.analytics.AnalyticsEngine;
import com.hranalytics.charts.ChartFactory;
import com.hranalytics.dashboard.Dashboard;
import com.hranalytics.dashboard.DashboardManager;
import com.hranalytics.dashboard.KPICard;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.export.ExportSharingModule;
import com.hranalytics.integration.ESSPortalPublisher;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;
import com.hranalytics.integration.service.AttendanceService;
import com.hranalytics.integration.service.EmployeeService;
import com.hranalytics.integration.service.HRAnalyticsService;
import com.hranalytics.integration.service.PayrollService;
import com.hranalytics.integration.service.PerformanceService;
import com.hranalytics.metrics.MetricResult;
import com.hranalytics.metrics.MetricType;
import com.hranalytics.metrics.MetricsCalculationEngine;
import com.hranalytics.pipeline.*;
import com.hranalytics.reports.Report;
import com.hranalytics.reports.ReportGenerator;
import com.hranalytics.reports.ReportType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The single external entry point for the HR Analytics & Reporting Sub-System.
 * All external subsystems (ESS Portal, Manager Dashboard) call only this class.
 * Internally orchestrates the full pipeline without exposing any internal module.
 * Pattern: Facade (Structural). Owner: Prem M Thakur.
 *
 * Implements HRAnalyticsService — the interface published to external subsystems.
 * External teams (ESS Portal, Manager Dashboard) program to HRAnalyticsService only.
 *
 * Public API (via HRAnalyticsService):
 *   loadDashboard(userId, filters)         → DashboardSnapshot
 *   generateReport(reportType, filters)    → ReportSummary
 *   exportReport(reportId, format)         → String (file path)
 *   getKPISnapshot(userId, deptId)         → List<KPISnapshot>
 */
public class HRAnalyticsFacade implements HRAnalyticsService {

    private static final Logger LOG = Logger.getLogger(HRAnalyticsFacade.class.getName());

    // --- Pipeline components (all internal; never exposed) ---
    private final AccessControlModule accessControl;
    private final DataCollectionModule dataCollection;
    private final DataIntegrationLayer dataIntegration;
    private final DataProcessingEngine dataProcessing;
    private final MetricsCalculationEngine metricsEngine;
    private final AnalyticsEngine analyticsEngine;
    private final DashboardManager dashboardManager;
    private final ReportGenerator reportGenerator;
    private final ExportSharingModule exportModule;
    private final ESSPortalPublisher essPublisher;

    /** Session cache: dashboardId → Dashboard (supports exportReport lookup by report ID). */
    private final Map<String, Dashboard> dashboardCache = new HashMap<>();

    /** Session cache: reportId → Report (supports exportReport). */
    private final Map<String, Report> reportCache = new HashMap<>();

    /** Shared processed data — set on first loadDashboard call; reused by generateReport. */
    private ProcessedData lastProcessedData;
    private Map<MetricType, MetricResult> lastMetrics;

    public HRAnalyticsFacade(AccessControlModule accessControl,
                              ChartFactory chartFactory,
                              EmployeeService employeeService,
                              PayrollService payrollService,
                              AttendanceService attendanceService,
                              PerformanceService performanceService,
                              ESSPortalPublisher essPublisher) {
        this.accessControl   = accessControl;
        this.dataCollection  = new DataCollectionModule(
                employeeService, payrollService, attendanceService, performanceService);
        this.dataIntegration = new DataIntegrationLayer();
        this.dataProcessing  = new DataProcessingEngine();
        this.metricsEngine   = new MetricsCalculationEngine();
        this.analyticsEngine = new AnalyticsEngine();
        this.dashboardManager= new DashboardManager(chartFactory);
        this.reportGenerator = new ReportGenerator();
        this.exportModule    = new ExportSharingModule();
        this.essPublisher    = essPublisher;
    }

    // -------------------------------------------------------------------------
    // Public API — the only methods external subsystems may call
    // -------------------------------------------------------------------------

    // =========================================================================
    // HRAnalyticsService implementation — DTO-returning methods for external teams
    // =========================================================================

    /**
     * Orchestrates the full analytics pipeline and returns a DashboardSnapshot DTO.
     * Authorization is checked first; result is also published to the ESS Portal.
     */
    @Override
    public DashboardSnapshot loadDashboard(String userId, FilterCriteria filters) {
        LOG.info("HRAnalyticsFacade.loadDashboard: userId=" + userId);

        accessControl.authorize(userId, "VIEW_DASHBOARD");

        RawHRData raw           = dataCollection.collectAll(filters);
        RawHRData clean         = dataIntegration.integrate(raw);
        ProcessedData processed = dataProcessing.process(clean, filters);
        lastProcessedData = processed;

        Map<MetricType, MetricResult> metrics = metricsEngine.calculateAll(processed);
        lastMetrics = metrics;

        List<String> insights = analyticsEngine.generateInsights(metrics, processed);

        Dashboard dashboard = dashboardManager.buildDashboard(userId, filters, metrics, insights);
        dashboardCache.put(dashboard.getDashboardId(), dashboard);

        essPublisher.publishDashboard(dashboard);

        DashboardSnapshot snapshot = toDashboardSnapshot(dashboard, metrics);
        LOG.info("HRAnalyticsFacade.loadDashboard: complete → " + snapshot.getDashboardId());
        return snapshot;
    }

    /**
     * Generates a report from a string report-type name and returns a ReportSummary DTO.
     * Accepts the string names from ReportType enum (e.g. "ATTRITION", "FULL_HR_SUMMARY").
     */
    @Override
    public ReportSummary generateReport(String reportType, FilterCriteria filters) {
        ReportType type = ReportType.valueOf(reportType.toUpperCase());
        return toReportSummary(generateReportInternal(type, filters));
    }

    /** Exports a previously generated report; delegates to the internal implementation. */
    @Override
    public String exportReport(String reportId, String formatType) {
        LOG.info("HRAnalyticsFacade.exportReport: reportId=" + reportId + ", format=" + formatType);
        Report report = reportCache.get(reportId);
        if (report == null) {
            throw new IllegalArgumentException("Report not found: " + reportId
                    + ". Call generateReport() first.");
        }
        String path = exportModule.export(report, formatType);
        LOG.info("HRAnalyticsFacade.exportReport: exported to " + path);
        return path;
    }

    /**
     * Returns a KPI snapshot as a list of KPISnapshot DTOs for external consumption.
     * Also publishes to the ESS Portal.
     */
    @Override
    public List<KPISnapshot> getKPISnapshot(String userId, String deptId) {
        LOG.info("HRAnalyticsFacade.getKPISnapshot: userId=" + userId + ", dept=" + deptId);
        accessControl.authorize(userId, "VIEW_KPI");

        // Run a fresh pipeline pass scoped to the requested department
        FilterCriteria filter = new FilterCriteria();
        if (deptId != null && !deptId.isBlank()) {
            filter.setFilterDepartment(List.of(deptId));
        }

        RawHRData raw           = dataCollection.collectAll(filter);
        RawHRData clean         = dataIntegration.integrate(raw);
        ProcessedData processed = dataProcessing.process(clean, filter);
        Map<MetricType, MetricResult> metrics = metricsEngine.calculateAll(processed);
        List<String> insights   = analyticsEngine.generateInsights(metrics, processed);
        Dashboard dashboard     = dashboardManager.buildDashboard(userId, filter, metrics, insights);

        List<KPISnapshot> snapshots = dashboard.getKpiCards().stream()
                .map(c -> new KPISnapshot(c.getMetricName(), c.getCurrentValue(),
                        c.getPreviousValue(), c.getTrend(), c.getUnit(), c.isFlagged()))
                .toList();

        essPublisher.publishKPISnapshot(dashboard.getDashboardId(), deptId, dashboard.getKpiCards());
        return snapshots;
    }

    // =========================================================================
    // Internal helpers — kept package-private for use by Main / tests
    // =========================================================================

    /**
     * Internal report generation that accepts the typed ReportType enum.
     * Used by Main.java and by the string-based generateReport() override above.
     */
    public Report generateReportInternal(ReportType reportType, FilterCriteria filters) {
        LOG.info("HRAnalyticsFacade.generateReport: type=" + reportType);

        if (lastProcessedData == null || lastMetrics == null) {
            loadDashboard("U001", filters != null ? filters : new FilterCriteria());
        }

        Dashboard latestDashboard = dashboardCache.values().stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No dashboard available."));

        Report report = reportGenerator.generate(reportType, latestDashboard, lastMetrics, filters);
        reportCache.put(report.getReportId(), report);
        LOG.info("HRAnalyticsFacade.generateReport: complete → " + report.getReportId());
        return report;
    }

    // =========================================================================
    // DTO conversion helpers
    // =========================================================================

    private DashboardSnapshot toDashboardSnapshot(Dashboard d, Map<MetricType, MetricResult> metrics) {
        List<KPISnapshot> kpis = d.getKpiCards().stream()
                .map(c -> new KPISnapshot(c.getMetricName(), c.getCurrentValue(),
                        c.getPreviousValue(), c.getTrend(), c.getUnit(), c.isFlagged()))
                .toList();

        List<String> widgetIds = d.getWidgets().stream()
                .map(w -> w.getWidgetId()).toList();

        double attrition   = extractValue(metrics, MetricType.ATTRITION_RATE);
        double growth      = extractValue(metrics, MetricType.EMPLOYEE_GROWTH);
        double performance = extractValue(metrics, MetricType.AVERAGE_PERFORMANCE);

        return new DashboardSnapshot(d.getDashboardId(), widgetIds, kpis,
                attrition, growth, performance, d.getInsights());
    }

    private ReportSummary toReportSummary(Report r) {
        return new ReportSummary(r.getReportId(), r.getReportTitle(),
                r.getType().name(), r.getGeneratedDate(), r.getReportSections().size());
    }

    private double extractValue(Map<MetricType, MetricResult> metrics, MetricType type) {
        MetricResult r = metrics.get(type);
        return (r != null) ? r.getCurrentValue() : 0.0;
    }
}
