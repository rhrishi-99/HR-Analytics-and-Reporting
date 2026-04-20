package com.hranalytics.integration.service;

import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;

import java.util.List;

/**
 * Public integration interface for the HR Analytics & Reporting Sub-System.
 *
 * External subsystems (ESS Portal, Manager Dashboard, etc.) depend ONLY on this interface.
 * They must never reference HRAnalyticsFacade or any internal class directly.
 *
 * Implemented by: HRAnalyticsFacade
 *
 * Integration 2 — Outbound API (we publish).
 * Owner: Prem M Thakur (Facade / integration boundary).
 */
public interface HRAnalyticsService {

    /**
     * Loads a dashboard for the given user, scoped by the supplied filter criteria.
     * Returns a lightweight snapshot safe for external consumption.
     *
     * @param userId  authenticated user ID (must hold VIEW_DASHBOARD permission)
     * @param filters department, date range, and status filters; pass new FilterCriteria() for all data
     * @return DashboardSnapshot containing KPI cards, chart data, and analytical insights
     * @throws com.hranalytics.exceptions.UnauthorizedAccessException if userId lacks permission
     */
    DashboardSnapshot loadDashboard(String userId, FilterCriteria filters);

    /**
     * Generates a named report and returns its summary metadata.
     * The returned reportId can be passed to exportReport() to download the file.
     *
     * @param reportType one of: "ATTRITION", "EMPLOYEE_GROWTH", "COMPENSATION",
     *                   "DEPARTMENT_METRICS", "PERFORMANCE", "FULL_HR_SUMMARY"
     * @param filters    scope of the report; pass new FilterCriteria() for all data
     * @return ReportSummary with reportId, title, type, and generation timestamp
     */
    ReportSummary generateReport(String reportType, FilterCriteria filters);

    /**
     * Exports a previously generated report in the requested file format.
     *
     * @param reportId   ID returned by generateReport()
     * @param format     one of: "csv", "pdf", "xlsx"
     * @return file path of the exported output
     * @throws com.hranalytics.exceptions.ExportFormatUnsupportedException if format is unknown
     */
    String exportReport(String reportId, String format);

    /**
     * Returns a lightweight KPI snapshot for one department, suitable for embedding
     * in the ESS Portal or a Manager Dashboard widget.
     *
     * @param userId authenticated user ID (must hold VIEW_KPI permission)
     * @param deptId department to scope (null = all departments)
     * @return list of KPISnapshot objects, one per computed metric
     * @throws com.hranalytics.exceptions.UnauthorizedAccessException if userId lacks permission
     */
    List<KPISnapshot> getKPISnapshot(String userId, String deptId);
}
