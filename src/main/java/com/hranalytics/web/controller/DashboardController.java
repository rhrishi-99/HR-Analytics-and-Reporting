package com.hranalytics.web.controller;

import com.hranalytics.domain.DateRange;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.DashboardSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MVC Controller for dashboard operations.
 * Receives parsed HTTP parameters (Model input), applies business rules,
 * delegates to HRAnalyticsFacade, and returns the Model (DashboardSnapshot).
 * The View (JSON serialisation) is handled separately by ApiHandler.
 *
 * Pattern: MVC — Controller layer. Owner: R G Rhrishi.
 */
public class DashboardController {

    private final HRAnalyticsFacade facade;

    public DashboardController(HRAnalyticsFacade facade) {
        this.facade = facade;
    }

    /** Handles a dashboard load request and returns the Model. */
    public DashboardSnapshot handle(Map<String, String> params) {
        String userId = params.getOrDefault("userId", "U002");
        FilterCriteria filter = buildFilter(params);
        return facade.loadDashboard(userId, filter);
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
