package com.hranalytics.integration.db;

import com.hranalytics.integration.external.PerformanceRecord;
import com.hranalytics.integration.service.PerformanceService;
import com.hrms.db.repositories.hranalytics.EmployeeServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that bridges the Database Sub-System's performance data with our PerformanceService.
 * Wraps DB performance entities/models from the hrms-database JAR.
 */
public class DBPerformanceServiceAdapter implements PerformanceService {

    private final EmployeeServiceImpl dbService;

    public DBPerformanceServiceAdapter() {
        this.dbService = new EmployeeServiceImpl();
    }

    @Override
    public List<PerformanceRecord> getPerformanceByEmployee(String employeeId) {
        // Since direct access to Appraisal list from Employee entity is unclear via javap,
        // we use a placeholder or best-effort mapping if the JAR has a performance repository.
        // For the demo, we will return an empty list or a single mapped record if found.
        return Collections.emptyList();
    }

    @Override
    public List<PerformanceRecord> getPerformanceByCycle(String cycle) {
        // Placeholder for cycle-based lookup
        return Collections.emptyList();
    }

    @Override
    public double getAveragePerformanceScore(String employeeId) {
        com.hrms.db.entities.Employee emp = dbService.getEmployeeById(employeeId);
        return (emp != null && emp.getPerformanceScore() != null) ? emp.getPerformanceScore() : 0.0;
    }
}
