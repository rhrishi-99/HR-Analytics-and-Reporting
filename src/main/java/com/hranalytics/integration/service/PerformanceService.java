package com.hranalytics.integration.service;

import com.hranalytics.integration.external.PerformanceRecord;

import java.util.List;

/**
 * Integration contract provided by the Performance Management Sub-System.
 * Supersedes the ad-hoc PerformanceManagementClient; that class now implements this interface.
 * Stub: PerformanceServiceStub. Results are mapped to domain Performance via PerformanceMapper.
 */
public interface PerformanceService {
    List<PerformanceRecord> getPerformanceByEmployee(String employeeId);
    List<PerformanceRecord> getPerformanceByCycle(String cycle);
    double getAveragePerformanceScore(String employeeId);
}
