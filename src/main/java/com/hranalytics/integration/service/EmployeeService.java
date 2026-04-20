package com.hranalytics.integration.service;

import com.hranalytics.domain.Employee;

import java.util.List;

/**
 * Integration contract provided by the Database Team.
 * DataCollectionModule depends on this interface — never on a concrete implementation.
 * Stub: EmployeeServiceStub. Production: wired by the DB team at runtime.
 */
public interface EmployeeService {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(String employeeId);
    List<Employee> getEmployeesByDepartment(String department);
    List<Employee> getActiveEmployees();
}
