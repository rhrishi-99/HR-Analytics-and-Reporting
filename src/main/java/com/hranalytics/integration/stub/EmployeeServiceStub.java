package com.hranalytics.integration.stub;

import com.hranalytics.domain.Employee;
import com.hranalytics.integration.service.EmployeeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stub implementation of EmployeeService backed by hardcoded simulation data.
 * Used until the Database Team delivers their production implementation.
 * Swap this for the real implementation in HRAnalyticsFacade with no other changes required.
 */
public class EmployeeServiceStub implements EmployeeService {

    private final List<Employee> employees = buildSimulatedRoster();

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    @Override
    public Employee getEmployeeById(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Employee> getEmployeesByDepartment(String department) {
        return employees.stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getActiveEmployees() {
        return employees.stream()
                .filter(e -> e.getStatus() == Employee.Status.ACTIVE)
                .collect(Collectors.toList());
    }

    private static List<Employee> buildSimulatedRoster() {
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("E001", "Alice Johnson",   "Engineering", "Senior Engineer",   LocalDate.of(2020, 3, 15), 95000, 4.5, Employee.Status.ACTIVE));
        list.add(new Employee("E002", "Bob Smith",       "Engineering", "Junior Engineer",   LocalDate.of(2022, 7, 1),  65000, 3.8, Employee.Status.ACTIVE));
        list.add(new Employee("E003", "Carol White",     "Engineering", "Tech Lead",         LocalDate.of(2019, 1, 10), 120000, 4.8, Employee.Status.ACTIVE));
        list.add(new Employee("E004", "David Lee",       "HR",          "HR Manager",        LocalDate.of(2018, 6, 20), 85000, 4.2, Employee.Status.ACTIVE));
        list.add(new Employee("E005", "Eve Martinez",    "HR",          "HR Coordinator",    LocalDate.of(2021, 9, 5),  55000, 3.9, Employee.Status.ACTIVE));
        list.add(new Employee("E006", "Frank Brown",     "Finance",     "Finance Analyst",   LocalDate.of(2020, 11, 30),75000, 4.0, Employee.Status.ACTIVE));
        list.add(new Employee("E007", "Grace Kim",       "Finance",     "CFO",               LocalDate.of(2017, 4, 1),  150000, 4.9, Employee.Status.ACTIVE));
        list.add(new Employee("E008", "Henry Davis",     "Engineering", "DevOps Engineer",   LocalDate.of(2021, 2, 14), 88000, 4.1, Employee.Status.ACTIVE));
        list.add(new Employee("E009", "Iris Patel",      "Engineering", "Data Engineer",     LocalDate.of(2023, 1, 3),  72000, 3.6, Employee.Status.ACTIVE));
        list.add(new Employee("E010", "Jack Wilson",     "HR",          "Recruiter",         LocalDate.of(2022, 3, 22), 52000, 3.7, Employee.Status.ACTIVE));
        list.add(new Employee("E011", "Karen Zhang",     "Finance",     "Accountant",        LocalDate.of(2019, 8, 15), 68000, 4.3, Employee.Status.ACTIVE));
        list.add(new Employee("E012", "Leo Nguyen",      "Engineering", "QA Engineer",       LocalDate.of(2021, 5, 10), 70000, 3.5, Employee.Status.ACTIVE));
        list.add(new Employee("E013", "Mia Thompson",    "Finance",     "Finance Manager",   LocalDate.of(2020, 7, 7),  92000, 4.4, Employee.Status.ACTIVE));
        list.add(new Employee("E014", "Nathan Clark",    "Engineering", "Software Engineer", LocalDate.of(2023, 4, 1),  78000, 3.9, Employee.Status.ACTIVE));
        list.add(new Employee("E015", "Olivia Reed",     "HR",          "HR Analyst",        LocalDate.of(2020, 2, 1),  58000, 3.2, Employee.Status.INACTIVE));
        return list;
    }
}
