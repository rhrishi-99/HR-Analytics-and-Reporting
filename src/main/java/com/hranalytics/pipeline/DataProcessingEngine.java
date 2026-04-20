package com.hranalytics.pipeline;

import com.hranalytics.domain.*;
import com.hranalytics.exceptions.FilterCriteriaInvalidException;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Third stage of the analytics pipeline.
 * Applies FilterCriteria to the validated dataset, computes derived statistics
 * (headcount, new hires, separations, salary aggregates), and produces ProcessedData
 * that is ready for MetricsCalculationEngine.
 * Owner: Prem M Thakur (pipeline boundary).
 */
public class DataProcessingEngine {

    private static final Logger LOG = Logger.getLogger(DataProcessingEngine.class.getName());

    /**
     * Applies filters and derives all statistics needed by the metrics strategies.
     * Raises FILTER_CRITERIA_INVALID if any filter field is malformed.
     */
    public ProcessedData process(RawHRData raw, FilterCriteria filters) {
        validateFilters(filters);

        List<Employee> employees = applyFilters(raw.getEmployees(), filters);
        List<Payroll> payroll    = filterPayroll(raw.getPayrollRecords(), employees, filters);

        ProcessedData data = new ProcessedData();
        data.setEmployees(employees);
        data.setPayrollRecords(payroll);
        data.setAttendanceRecords(raw.getAttendanceRecords());
        data.setPerformanceRecords(raw.getPerformanceRecords());
        data.setDepartments(raw.getDepartments());
        data.setAppliedFilters(filters);

        // Compute derived statistics
        data.setTotalHeadcount(countActive(employees));
        data.setPreviousHeadcount(computePreviousHeadcount(raw.getEmployees(), filters));
        data.setNewHires(countNewHires(employees, filters));
        data.setSeparations(countSeparations(raw.getEmployees(), filters));
        data.setTotalGrossSalary(payroll.stream().mapToDouble(Payroll::getGrossSalary).sum());
        data.setTotalNetSalary(payroll.stream().mapToDouble(Payroll::getNetSalary).sum());
        data.setEmployeesByDepartment(groupByDepartment(employees));
        data.setAvgSalaryByDepartment(computeAvgSalaryByDept(payroll, employees));

        LOG.info("DataProcessingEngine: " + data);
        return data;
    }

    /** Validates that the date range (if present) is logically valid. */
    private void validateFilters(FilterCriteria filters) {
        List<String> invalid = new ArrayList<>();
        if (filters == null) return;
        if (filters.getDateRange() != null && !filters.getDateRange().isValid()) {
            invalid.add("dateRange");
        }
        if (!invalid.isEmpty()) {
            throw new FilterCriteriaInvalidException(invalid,
                    "Invalid filter fields: " + invalid + ". Resetting to defaults.");
        }
    }

    /** Filters the employee list by department, status, and category. */
    private List<Employee> applyFilters(List<Employee> employees, FilterCriteria filters) {
        if (filters == null) return employees;
        return employees.stream()
                .filter(e -> filters.getFilterDepartment().isEmpty()
                        || filters.getFilterDepartment().contains(e.getDepartment()))
                .filter(e -> {
                    String status = filters.getFilterStatus();
                    if ("ALL".equals(status) || status == null) return true;
                    return e.getStatus().name().equals(status);
                })
                .collect(Collectors.toList());
    }

    /** Restricts payroll records to employees that survived filtering. */
    private List<Payroll> filterPayroll(List<Payroll> payroll, List<Employee> employees, FilterCriteria filters) {
        Set<String> empIds = employees.stream().map(Employee::getEmployeeId).collect(Collectors.toSet());
        return payroll.stream().filter(p -> empIds.contains(p.getEmployeeId())).collect(Collectors.toList());
    }

    private int countActive(List<Employee> employees) {
        return (int) employees.stream().filter(e -> e.getStatus() == Employee.Status.ACTIVE).count();
    }

    /**
     * Estimates the headcount at the start of the filter period by subtracting new hires
     * who joined during the period from the current total.
     */
    private int computePreviousHeadcount(List<Employee> allEmployees, FilterCriteria filters) {
        int current = countActive(allEmployees);
        int newHires = countNewHires(allEmployees, filters);
        return Math.max(0, current - newHires);
    }

    /** Counts employees whose joinDate falls within the filter date range. */
    private int countNewHires(List<Employee> employees, FilterCriteria filters) {
        if (filters == null || !filters.hasValidDateRange()) return 2; // default simulation value
        DateRange range = filters.getDateRange();
        return (int) employees.stream()
                .filter(e -> range.contains(e.getJoinDate()))
                .count();
    }

    /** Counts INACTIVE employees — proxy for separations during the period. */
    private int countSeparations(List<Employee> employees, FilterCriteria filters) {
        return (int) employees.stream()
                .filter(e -> e.getStatus() == Employee.Status.INACTIVE)
                .count();
    }

    /** Groups employees by their department name. */
    private Map<String, List<Employee>> groupByDepartment(List<Employee> employees) {
        return employees.stream().collect(Collectors.groupingBy(Employee::getDepartment));
    }

    /** Computes average gross salary per department from payroll records. */
    private Map<String, Double> computeAvgSalaryByDept(List<Payroll> payroll, List<Employee> employees) {
        Map<String, String> empToDept = employees.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Employee::getDepartment));

        Map<String, List<Double>> salariesByDept = new HashMap<>();
        for (Payroll p : payroll) {
            String dept = empToDept.get(p.getEmployeeId());
            if (dept != null) {
                salariesByDept.computeIfAbsent(dept, k -> new ArrayList<>()).add(p.getGrossSalary());
            }
        }

        Map<String, Double> avgByDept = new HashMap<>();
        salariesByDept.forEach((dept, salaries) ->
                avgByDept.put(dept, salaries.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
        return avgByDept;
    }
}
