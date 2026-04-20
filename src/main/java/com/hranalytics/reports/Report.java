package com.hranalytics.reports;

import com.hranalytics.dashboard.KPICard;
import com.hranalytics.domain.FilterCriteria;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable value object representing a generated analytics report.
 * Produced by ReportGenerator and passed to ExportSharingModule for export.
 * Owner: R G Rhrishi (reports layer).
 */
public class Report {

    private final String reportId;
    private final String reportTitle;
    private final ReportType type;
    private final List<String> reportSections;  // ordered narrative/data sections
    private final List<KPICard> kpiSnapshot;
    private final FilterCriteria filters;
    private final LocalDateTime generatedDate;

    public Report(String reportId, String reportTitle, ReportType type,
                  List<String> reportSections, List<KPICard> kpiSnapshot,
                  FilterCriteria filters) {
        this.reportId = reportId;
        this.reportTitle = reportTitle;
        this.type = type;
        this.reportSections = reportSections;
        this.kpiSnapshot = kpiSnapshot;
        this.filters = filters;
        this.generatedDate = LocalDateTime.now();
    }

    public String getReportId()           { return reportId; }
    public String getReportTitle()        { return reportTitle; }
    public ReportType getType()           { return type; }
    public List<String> getReportSections(){ return reportSections; }
    public List<KPICard> getKpiSnapshot() { return kpiSnapshot; }
    public FilterCriteria getFilters()    { return filters; }
    public LocalDateTime getGeneratedDate(){ return generatedDate; }

    @Override
    public String toString() {
        return String.format("Report[%s, type=%s, sections=%d, generated=%s]",
                reportId, type, reportSections.size(), generatedDate);
    }
}
