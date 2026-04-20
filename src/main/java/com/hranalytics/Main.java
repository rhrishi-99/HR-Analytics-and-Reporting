package com.hranalytics;

import com.hranalytics.access.AccessControlModule;
import com.hranalytics.charts.AttritionChartFactory;
import com.hranalytics.charts.ChartFactory;
import com.hranalytics.charts.EmployeeGrowthChartFactory;
import com.hranalytics.domain.DateRange;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.exceptions.ExportFormatUnsupportedException;
import com.hranalytics.exceptions.UnauthorizedAccessException;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.ESSPortalPublisher;
import com.hranalytics.integration.PerformanceManagementClient;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;
import com.hranalytics.integration.stub.AttendanceServiceStub;
import com.hranalytics.integration.stub.EmployeeServiceStub;
import com.hranalytics.integration.stub.PayrollServiceStub;
import com.hranalytics.integration.stub.PerformanceServiceStub;
import com.hranalytics.integration.attrition.HRAnalyticsReportingServiceImpl;
import com.hranalytics.reports.Report;
import com.hranalytics.reports.ReportType;
import com.hrms.service.IHRAnalyticsReportingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Entry point and end-to-end demonstration of the HR Analytics & Reporting Sub-System.
 *
 * Integration wiring:
 *   EmployeeService   → EmployeeServiceStub   (swap for DB team's impl when ready)
 *   PayrollService    → PayrollServiceStub    (swap for Payroll team's impl when ready)
 *   AttendanceService → AttendanceServiceStub (swap for Attendance team's impl when ready)
 *   PerformanceService→ PerformanceManagementClient wrapping PerformanceServiceStub
 *
 * Demonstrates all 8 pipeline steps plus exception scenarios.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("   HR ANALYTICS & REPORTING SUB-SYSTEM  —  DEMO          ");
        System.out.println("══════════════════════════════════════════════════════════\n");

        // ── 1. Bootstrap — wire service interfaces to stubs ─────────────────────
        AccessControlModule accessControl = new AccessControlModule();
        accessControl.loadDefaultUsers();

        // Each stub can be replaced with the real implementation without touching any other class
        EmployeeServiceStub   employeeStub    = new EmployeeServiceStub();
        PayrollServiceStub    payrollStub     = new PayrollServiceStub();
        AttendanceServiceStub attendanceStub  = new AttendanceServiceStub();
        PerformanceManagementClient perfClient =
                new PerformanceManagementClient(new PerformanceServiceStub());

        ESSPortalPublisher essPublisher = new ESSPortalPublisher();
        ChartFactory chartFactory = new EmployeeGrowthChartFactory();

        HRAnalyticsFacade facade = new HRAnalyticsFacade(
                accessControl,
                chartFactory,
                employeeStub,
                payrollStub,
                attendanceStub,
                perfClient,
                essPublisher);

        // ── 2. Filter criteria ──────────────────────────────────────────────────
        FilterCriteria filters = new FilterCriteria();
        filters.setDateRange(new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)));
        filters.setFilterStatus("ACTIVE");

        // ── 3. Load dashboard (returns DashboardSnapshot DTO) ───────────────────
        banner("STEP 1: LOAD DASHBOARD");
        DashboardSnapshot snapshot = facade.loadDashboard("U002", filters);
        System.out.println("  Dashboard ID  : " + snapshot.getDashboardId());
        System.out.println("  Attrition Rate: " + String.format("%.2f%%", snapshot.getAttritionRate()));
        System.out.println("  Employee Growth: " + String.format("%.2f%%", snapshot.getEmployeeGrowth()));
        System.out.println("  Avg Performance: " + String.format("%.2f/5.0", snapshot.getAvgPerformance()));
        System.out.println("  KPI Cards (" + snapshot.getKpiCards().size() + "):");
        snapshot.getKpiCards().forEach(k -> System.out.println("    " + k));
        System.out.println("  Insights:");
        snapshot.getInsights().forEach(i -> System.out.println("    • " + i));

        // ── 4. Full HR Summary report (returns ReportSummary DTO) ───────────────
        banner("STEP 2: GENERATE REPORT (FULL HR SUMMARY)");
        ReportSummary summary = facade.generateReport("FULL_HR_SUMMARY", filters);
        System.out.println("  " + summary);

        // ── 5. Export all three formats ─────────────────────────────────────────
        banner("STEP 3: EXPORT REPORT");
        for (String fmt : List.of("csv", "pdf", "xlsx")) {
            String path = facade.exportReport(summary.getReportId(), fmt);
            System.out.println("  Exported [" + fmt.toUpperCase() + "] → " + path);
        }

        // ── 6. Attrition report (using internal typed enum) ─────────────────────
        banner("STEP 4: ATTRITION REPORT + CSV EXPORT");
        Report attritionReport = facade.generateReportInternal(ReportType.ATTRITION, filters);
        System.out.println("  " + attritionReport);
        System.out.println("  CSV: " + facade.exportReport(attritionReport.getReportId(), "csv"));

        // ── 7. KPI Snapshot for ESS Portal (returns List<KPISnapshot> DTO) ──────
        banner("STEP 5: KPI SNAPSHOT (ESS PORTAL — INTEGRATION 2)");
        List<KPISnapshot> kpis = facade.getKPISnapshot("U003", "Engineering");
        System.out.println("  Returned " + kpis.size() + " KPISnapshot DTOs to ESS Portal:");
        kpis.forEach(k -> System.out.println("    " + k));

        // ── 8. Swap to AttritionChartFactory, filter by HR dept ─────────────────
        banner("STEP 6: SWITCH TO ATTRITION CHART FACTORY (HR DEPT)");
        HRAnalyticsFacade attritionFacade = new HRAnalyticsFacade(
                accessControl,
                new AttritionChartFactory(),
                employeeStub, payrollStub, attendanceStub, perfClient,
                essPublisher);
        FilterCriteria hrFilter = new FilterCriteria();
        hrFilter.setFilterDepartment(List.of("HR"));
        DashboardSnapshot hrSnapshot = attritionFacade.loadDashboard("U002", hrFilter);
        System.out.println("  HR Dept Dashboard ID: " + hrSnapshot.getDashboardId());
        System.out.println("  KPI Count: " + hrSnapshot.getKpiCards().size());

        // ── 9. Exception: unsupported export format ─────────────────────────────
        banner("STEP 7: EXCEPTION — EXPORT_FORMAT_UNSUPPORTED");
        try {
            facade.exportReport(summary.getReportId(), "pptx");
        } catch (ExportFormatUnsupportedException ex) {
            System.out.println("  [WARNING] " + ex.getErrorCode());
            System.out.println("  Requested: " + ex.getRequestedFormat());
            System.out.println("  Supported: " + ex.getSupportedFormats());
        }

        // ── 10. Exception: unauthorized access ──────────────────────────────────
        banner("STEP 8: EXCEPTION — UNAUTHORIZED_ACCESS_ATTEMPT");
        try {
            accessControl.authorize("U004", "GENERATE_REPORT");
        } catch (UnauthorizedAccessException ex) {
            System.out.println("  [MAJOR] " + ex.getErrorCode());
            System.out.println("  User: " + ex.getUserId() + " | Action: " + ex.getAttemptedAction());
        }

        // ── 11. Integration 3 — Attrition-Risk Sub-System ──────────────────────
        banner("STEP 9: ATTRITION-RISK INTEGRATION (IHRAnalyticsReportingService)");
        IHRAnalyticsReportingService attritionApi =
                new HRAnalyticsReportingServiceImpl(facade, employeeStub);

        System.out.println("  Organisation Turnover Rate: "
                + String.format("%.2f%%", attritionApi.getOrganisationTurnoverRate()));

        System.out.println("  Headcount by Department:");
        attritionApi.getHeadcountByDepartment()
                .forEach((dept, count) -> System.out.println("    " + dept + ": " + count));

        System.out.println("  Engagement Score (Engineering): "
                + String.format("%.1f/100", attritionApi.getEngagementScore("Engineering")));

        System.out.println("  Aggregated Metrics (Q1 2025):");
        Map<String, Double> agg = attritionApi.getAggregatedMetrics(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        agg.forEach((k, v) -> System.out.printf("    %-25s %.2f%n", k + ":", v));

        // Simulate the Attrition-Risk team pushing a report back to us
        IHRAnalyticsReportingService.AttritionRiskReport inboundReport =
                new IHRAnalyticsReportingService.AttritionRiskReport() {
                    public String getReportId()         { return "ARR-2025-Q1"; }
                    public LocalDate getGeneratedOn()   { return LocalDate.of(2025, 4, 1); }
                    public int getHighRiskCount()        { return 3; }
                    public int getMediumRiskCount()      { return 8; }
                    public int getLowRiskCount()         { return 24; }
                    public List<String> getTopRootCauses(){ return List.of("attendance", "tenure", "compensation"); }
                    public String getSummary()           { return "3 high-risk employees require immediate HR intervention."; }
                };
        attritionApi.publishAttritionRiskReport(inboundReport);
        System.out.println("  Attrition-Risk report published: " + inboundReport.getReportId()
                + " | HIGH=" + inboundReport.getHighRiskCount()
                + " MED=" + inboundReport.getMediumRiskCount()
                + " LOW=" + inboundReport.getLowRiskCount());
        System.out.println("  Summary: " + inboundReport.getSummary());

        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("   DEMO COMPLETE — check output/exports/ for generated files");
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private static void banner(String title) {
        System.out.println("\n──────────────────────────────────────────────────────────");
        System.out.println("  " + title);
        System.out.println("──────────────────────────────────────────────────────────");
    }
}
