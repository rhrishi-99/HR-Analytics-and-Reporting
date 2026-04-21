package com.hranalytics.web.controller;

import com.hranalytics.domain.DateRange;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.ReportSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MVC Controller for report generation and export operations.
 * Receives parsed HTTP parameters, delegates to HRAnalyticsFacade,
 * and returns the Model (ReportSummary or export file path).
 * The View (JSON serialisation) is handled separately by ApiHandler.
 *
 * Pattern: MVC — Controller layer. Owner: R G Rhrishi.
 */
public class ReportController {

    private final HRAnalyticsFacade facade;

    public ReportController(HRAnalyticsFacade facade) {
        this.facade = facade;
    }

    /** Handles a report generation request and returns the Model. */
    public ReportSummary handleGenerate(Map<String, String> params) {
        String reportType = params.getOrDefault("type", "FULL_HR_SUMMARY");
        FilterCriteria filter = buildFilter(params);
        return facade.generateReport(reportType, filter);
    }

    /** Handles a report export request and returns the file path. */
    public String handleExport(Map<String, String> params) {
        String reportId = params.getOrDefault("reportId", "");
        String format   = params.getOrDefault("format", "csv");
        return facade.exportReport(reportId, format);
    }

    private FilterCriteria buildFilter(Map<String, String> params) {
        FilterCriteria f = new FilterCriteria();
        f.setFilterStatus(params.getOrDefault("status", "ALL"));
        String dept = params.getOrDefault("dept", "");
        if (!dept.isBlank()) f.setFilterDepartment(List.of(dept.split(",")));
        String start = params.get("start"), end = params.get("end");
        if (start != null && end != null && !start.isBlank() && !end.isBlank())
            f.setDateRange(new DateRange(LocalDate.parse(start), LocalDate.parse(end)));
        return f;
    }
}
