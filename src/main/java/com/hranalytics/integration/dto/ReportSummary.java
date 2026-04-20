package com.hranalytics.integration.dto;

import java.time.LocalDateTime;

/**
 * Lightweight DTO returned to external consumers after report generation.
 * Carries the reportId (needed to call exportReport()), title, type, and timestamp.
 *
 * External teams import only this class — no internal Report type is exposed.
 */
public class ReportSummary {

    private final String reportId;
    private final String reportTitle;
    private final String reportType;
    private final LocalDateTime generatedDate;
    private final int sectionCount;

    public ReportSummary(String reportId, String reportTitle,
                         String reportType, LocalDateTime generatedDate, int sectionCount) {
        this.reportId = reportId;
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.generatedDate = generatedDate;
        this.sectionCount = sectionCount;
    }

    public String getReportId()          { return reportId; }
    public String getReportTitle()       { return reportTitle; }
    public String getReportType()        { return reportType; }
    public LocalDateTime getGeneratedDate(){ return generatedDate; }
    public int getSectionCount()         { return sectionCount; }

    @Override
    public String toString() {
        return String.format("ReportSummary[id=%s, type=%s, sections=%d, generated=%s]",
                reportId, reportType, sectionCount, generatedDate);
    }
}
