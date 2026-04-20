package com.hranalytics.integration;

import com.hranalytics.integration.external.PerformanceRecord;
import com.hranalytics.integration.service.PerformanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Integration 1 — Inbound adapter for the Performance Management Sub-System.
 * Implements PerformanceService so it can be injected wherever that interface is required.
 * In production this class calls the Performance Management REST API; until then it delegates
 * to PerformanceServiceStub via constructor injection.
 * Owner: Prem M Thakur (integration boundary).
 */
public class PerformanceManagementClient implements PerformanceService {

    private static final Logger LOG = Logger.getLogger(PerformanceManagementClient.class.getName());

    /** The backing service — either a stub or the real Production adapter. */
    private final PerformanceService delegate;

    public PerformanceManagementClient(PerformanceService delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns all performance records for one employee by delegating to the injected service.
     * Logs the call for pipeline traceability.
     */
    @Override
    public List<PerformanceRecord> getPerformanceByEmployee(String employeeId) {
        LOG.info("PerformanceManagementClient: getPerformanceByEmployee(" + employeeId + ")");
        return delegate.getPerformanceByEmployee(employeeId);
    }

    /**
     * Returns all performance records for a given review cycle (e.g. "Q1-2025").
     */
    @Override
    public List<PerformanceRecord> getPerformanceByCycle(String cycle) {
        LOG.info("PerformanceManagementClient: getPerformanceByCycle(" + cycle + ")");
        return delegate.getPerformanceByCycle(cycle);
    }

    /**
     * Returns the average performance score for one employee across all their reviews.
     */
    @Override
    public double getAveragePerformanceScore(String employeeId) {
        return delegate.getAveragePerformanceScore(employeeId);
    }

    /**
     * Convenience method used by DataCollectionModule to bulk-fetch performance records
     * for a list of employees within a review cycle.
     *
     * @param cycle e.g. "Q1-2025"
     * @return all PerformanceRecords for that cycle from the Performance Management system
     */
    public List<PerformanceRecord> getPerformanceRecordsForCycle(String cycle) {
        LOG.info("PerformanceManagementClient: fetching all records for cycle=" + cycle);
        return delegate.getPerformanceByCycle(cycle);
    }
}
