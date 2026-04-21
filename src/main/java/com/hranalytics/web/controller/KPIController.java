package com.hranalytics.web.controller;

import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.KPISnapshot;

import java.util.List;
import java.util.Map;

/**
 * MVC Controller for KPI snapshot operations.
 * Receives parsed HTTP parameters, delegates to HRAnalyticsFacade,
 * and returns the Model (List<KPISnapshot>).
 * The View (JSON serialisation) is handled separately by ApiHandler.
 *
 * Pattern: MVC — Controller layer. Owner: R G Rhrishi.
 */
public class KPIController {

    private final HRAnalyticsFacade facade;

    public KPIController(HRAnalyticsFacade facade) {
        this.facade = facade;
    }

    /** Handles a KPI snapshot request and returns the Model. */
    public List<KPISnapshot> handle(Map<String, String> params) {
        String userId = params.getOrDefault("userId", "U003");
        String dept   = params.getOrDefault("dept", "");
        return facade.getKPISnapshot(userId, dept.isBlank() ? null : dept);
    }
}
