package com.hranalytics.integration.db;

import com.hranalytics.domain.Employee;
import com.hranalytics.integration.service.EmployeeService;
import com.hrms.db.repositories.hranalytics.EmployeeServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that bridges the Database Sub-System's Employee repository with our EmployeeService.
 * Wraps EmployeeServiceImpl from the hrms-database JAR.
 * Pattern: Adapter (Structural).
 */
public class DBEmployeeServiceAdapter implements EmployeeService {

    private final EmployeeServiceImpl dbService;

    public DBEmployeeServiceAdapter() {
        this.dbService = new EmployeeServiceImpl();
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<com.hrms.db.entities.Employee> entities = dbService.getAllEmployees();
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public Employee getEmployeeById(String employeeId) {
        com.hrms.db.entities.Employee entity = dbService.getEmployeeById(employeeId);
        return (entity != null) ? mapToDomain(entity) : null;
    }

    @Override
    public List<Employee> getEmployeesByDepartment(String department) {
        List<com.hrms.db.entities.Employee> entities = dbService.getEmployeesByDepartment(department);
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Employee> getActiveEmployees() {
        List<com.hrms.db.entities.Employee> entities = dbService.getActiveEmployees();
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    /** Maps a DB entity from the JAR to our domain Employee class. */
    private Employee mapToDomain(com.hrms.db.entities.Employee e) {
        Employee.Status status = Employee.Status.ACTIVE;
        try {
            if (e.getEmploymentStatus() != null) {
                String s = e.getEmploymentStatus().toUpperCase();
                if (s.contains("LEAVE")) status = Employee.Status.ON_LEAVE;
                else if (s.contains("INACTIVE")) status = Employee.Status.INACTIVE;
            }
        } catch (Exception ex) {
            // Default to ACTIVE
        }

        return new Employee(
                e.getEmpId(),
                e.getName(),
                e.getDepartment(),
                e.getDesignation(),
                e.getDateOfJoining(),
                e.getBasicPay() != null ? e.getBasicPay() : 0.0,
                e.getPerformanceScore() != null ? e.getPerformanceScore() : 0.0,
                status
        );
    }
}
