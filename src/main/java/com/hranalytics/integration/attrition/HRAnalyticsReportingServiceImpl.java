package com.hranalytics.integration.attrition;

import com.hranalytics.domain.Employee;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.service.EmployeeService;
import com.hrms.service.IHRAnalyticsReportingService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implements the Attrition-Risk subsystem's integration contract.
 * Exposes HR Analytics pipeline data to external consumers without revealing internals.
 *
 * Integration 3 — Attrition-Risk Sub-System (inbound publish + outbound data pull).
 * Owner: Prem M Thakur (integration boundary).
 */
public class HRAnalyticsReportingServiceImpl implements IHRAnalyticsReportingService {

    private static final Logger LOG = Logger.getLogger(HRAnalyticsReportingServiceImpl.class.getName());

    private final HRAnalyticsFacade facade;
    private final EmployeeService employeeService;

    public HRAnalyticsReportingServiceImpl(HRAnalyticsFacade facade, EmployeeService employeeService) {
        this.facade = facade;
        this.employeeService = employeeService;
    }

    /** Delegates to the pipeline's attrition metric via the Facade. */
    @Override
    public double getOrganisationTurnoverRate() {
        DashboardSnapshot snap = facade.loadDashboard("U001", new FilterCriteria());
        return snap.getAttritionRate();
    }

    /** Counts active employees per department from the EmployeeService. */
    @Override
    public Map<String, Integer> getHeadcountByDepartment() {
        Map<String, Integer> headcount = new LinkedHashMap<>();
        for (Employee e : employeeService.getActiveEmployees()) {
            headcount.merge(e.getDepartment(), 1, Integer::sum);
        }
        return headcount;
    }

    /**
     * Derives engagement score from average performance score (0–5 scaled to 0–100).
     * Returns -1 if no employees found in the given department.
     */
    @Override
    public double getEngagementScore(String department) {
        List<Employee> deptEmployees = employeeService.getEmployeesByDepartment(department);
        if (deptEmployees.isEmpty()) return -1;
        double avgPerf = deptEmployees.stream()
                .mapToDouble(Employee::getPerformanceScore)
                .average()
                .orElse(-1.0 / 20.0); // will yield -1 after scaling if absent
        return avgPerf * 20.0; // scale 0–5 → 0–100
    }

    /** Receives an attrition-risk report and logs it into the HR Analytics dashboard store. */
    @Override
    public void publishAttritionRiskReport(AttritionRiskReport report) {
        LOG.info(String.format(
            "AttritionRisk report received [%s] generated=%s | HIGH=%d MED=%d LOW=%d | causes=%s",
            report.getReportId(), report.getGeneratedOn(),
            report.getHighRiskCount(), report.getMediumRiskCount(), report.getLowRiskCount(),
            report.getTopRootCauses()));
        LOG.info("Summary: " + report.getSummary());
        // Production: persist to dashboard widget store, trigger re-render
    }

    /**
     * Returns aggregated HR metrics over the given date window.
     * Keys: "avgTenureYears", "avgSatisfactionScore", "absenteeismRate".
     */
    @Override
    public Map<String, Double> getAggregatedMetrics(LocalDate from, LocalDate to) {
        List<Employee> all = employeeService.getAllEmployees();
        Map<String, Double> metrics = new LinkedHashMap<>();

        // Average tenure — years between joinDate and end of window
        double tenureSum = all.stream()
                .filter(e -> e.getJoinDate() != null)
                .mapToDouble(e -> ChronoUnit.DAYS.between(e.getJoinDate(), to) / 365.25)
                .sum();
        metrics.put("avgTenureYears", all.isEmpty() ? 0.0 : tenureSum / all.size());

        // Average satisfaction — performance score (0–5) scaled to 0–100
        double avgPerf = all.stream().mapToDouble(Employee::getPerformanceScore).average().orElse(0);
        metrics.put("avgSatisfactionScore", avgPerf * 20.0);

        // Absenteeism rate — derived from pipeline attrition data (stub: fixed baseline)
        metrics.put("absenteeismRate", 4.2);

        return metrics;
    }
}
