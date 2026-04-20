package com.hranalytics.reports;

import com.hranalytics.dashboard.Dashboard;
import com.hranalytics.dashboard.KPICard;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.exceptions.ReportGenerationTimeoutException;
import com.hranalytics.metrics.MetricResult;
import com.hranalytics.metrics.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Generates Report objects from dashboard data and metric results.
 * Enforces a simulated timeout budget — raises REPORT_GENERATION_TIMEOUT for overly complex
 * reports and suggests lighter alternatives.
 * Owner: R G Rhrishi (reports layer).
 */
public class ReportGenerator {

    private static final Logger LOG = Logger.getLogger(ReportGenerator.class.getName());
    private static final long TIMEOUT_MS = 5000; // 5-second simulated budget

    /**
     * Generates a report of the requested type using data from the provided dashboard.
     * Raises REPORT_GENERATION_TIMEOUT (MINOR) if generation would exceed the time budget.
     */
    public Report generate(ReportType type, Dashboard dashboard,
                           Map<MetricType, MetricResult> metrics, FilterCriteria filters) {
        long start = System.currentTimeMillis();

        String reportId    = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String reportTitle = buildTitle(type, filters);
        List<String> sections = buildSections(type, dashboard, metrics);

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > TIMEOUT_MS) {
            throw new ReportGenerationTimeoutException(type.name(), elapsed);
        }

        Report report = new Report(reportId, reportTitle, type, sections,
                dashboard.getKpiCards(), filters);
        LOG.info("ReportGenerator: " + report);
        return report;
    }

    private String buildTitle(ReportType type, FilterCriteria filters) {
        String base = switch (type) {
            case ATTRITION         -> "Attrition Analysis Report";
            case EMPLOYEE_GROWTH   -> "Employee Growth Report";
            case COMPENSATION      -> "Compensation Analytics Report";
            case DEPARTMENT_METRICS-> "Department Metrics Report";
            case PERFORMANCE       -> "Performance Overview Report";
            case FULL_HR_SUMMARY   -> "Full HR Analytics Summary";
        };
        String deptSuffix = (filters != null && !filters.getFilterDepartment().isEmpty())
                ? " — " + String.join(", ", filters.getFilterDepartment())
                : "";
        return base + deptSuffix;
    }

    /** Builds the ordered narrative sections appropriate to the requested report type. */
    private List<String> buildSections(ReportType type, Dashboard dashboard,
                                       Map<MetricType, MetricResult> metrics) {
        List<String> sections = new ArrayList<>();

        sections.add("=== EXECUTIVE SUMMARY ===");
        sections.add("Report Type: " + type);
        sections.add("Generated: " + java.time.LocalDateTime.now());

        sections.add("\n=== KEY PERFORMANCE INDICATORS ===");
        for (KPICard card : dashboard.getKpiCards()) {
            sections.add("  " + card);
        }

        sections.add("\n=== ANALYTICAL INSIGHTS ===");
        dashboard.getInsights().forEach(i -> sections.add("  • " + i));

        // Type-specific metric detail
        sections.add("\n=== METRIC DETAIL ===");
        List<MetricType> relevant = getRelevantMetrics(type);
        for (MetricType mt : relevant) {
            MetricResult r = metrics.get(mt);
            if (r != null) {
                sections.add("  " + r);
                if (!r.getBreakdown().isEmpty()) {
                    r.getBreakdown().forEach((k, v) ->
                            sections.add("    - " + k + ": " + String.format("%.2f", v) + r.getUnit()));
                }
            }
        }

        sections.add("\n=== FILTER CRITERIA APPLIED ===");
        sections.add("  " + (dashboard.getAppliedFilters() != null
                ? dashboard.getAppliedFilters().toString() : "None (all data)"));

        return sections;
    }

    /** Returns the subset of MetricTypes relevant to the requested report type. */
    private List<MetricType> getRelevantMetrics(ReportType type) {
        return switch (type) {
            case ATTRITION          -> List.of(MetricType.ATTRITION_RATE);
            case EMPLOYEE_GROWTH    -> List.of(MetricType.EMPLOYEE_GROWTH, MetricType.DEPARTMENT_METRICS);
            case COMPENSATION       -> List.of(MetricType.COMPENSATION_ANALYTICS);
            case DEPARTMENT_METRICS -> List.of(MetricType.DEPARTMENT_METRICS, MetricType.AVERAGE_PERFORMANCE);
            case PERFORMANCE        -> List.of(MetricType.AVERAGE_PERFORMANCE);
            case FULL_HR_SUMMARY    -> List.of(MetricType.values());
        };
    }
}
