package com.hranalytics.pipeline;

import com.hranalytics.domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data-transfer object that carries fully validated and enriched HR data
 * through the analytics pipeline from DataProcessingEngine onwards.
 * This is the primary input to MetricsCalculationEngine and AnalyticsEngine.
 */
public class ProcessedData {

    // --- Raw domain records (post-validation) ---
    private List<Employee> employees            = new ArrayList<>();
    private List<Payroll> payrollRecords        = new ArrayList<>();
    private List<Attendance> attendanceRecords  = new ArrayList<>();
    private List<Performance> performanceRecords= new ArrayList<>();
    private List<Department> departments        = new ArrayList<>();

    /** Filter criteria that were applied to produce this dataset. */
    private FilterCriteria appliedFilters;

    // --- Computed statistics (set by DataProcessingEngine) ---
    private int totalHeadcount;
    private int previousHeadcount;   // headcount at start of date-range period
    private int newHires;            // employees whose joinDate falls within the date range
    private int separations;         // employees who left (INACTIVE) within the date range
    private double totalGrossSalary;
    private double totalNetSalary;

    /** Employees bucketed by department name for fast per-dept lookups. */
    private Map<String, List<Employee>> employeesByDepartment = new HashMap<>();

    /** Average gross salary per department. */
    private Map<String, Double> avgSalaryByDepartment = new HashMap<>();

    // --- Getters ---
    public List<Employee> getEmployees()                        { return employees; }
    public List<Payroll> getPayrollRecords()                    { return payrollRecords; }
    public List<Attendance> getAttendanceRecords()              { return attendanceRecords; }
    public List<Performance> getPerformanceRecords()            { return performanceRecords; }
    public List<Department> getDepartments()                    { return departments; }
    public FilterCriteria getAppliedFilters()                   { return appliedFilters; }
    public int getTotalHeadcount()                              { return totalHeadcount; }
    public int getPreviousHeadcount()                           { return previousHeadcount; }
    public int getNewHires()                                    { return newHires; }
    public int getSeparations()                                 { return separations; }
    public double getTotalGrossSalary()                         { return totalGrossSalary; }
    public double getTotalNetSalary()                           { return totalNetSalary; }
    public Map<String, List<Employee>> getEmployeesByDepartment(){ return employeesByDepartment; }
    public Map<String, Double> getAvgSalaryByDepartment()       { return avgSalaryByDepartment; }

    // --- Setters (used by DataProcessingEngine) ---
    public void setEmployees(List<Employee> employees)                        { this.employees = employees; }
    public void setPayrollRecords(List<Payroll> payrollRecords)               { this.payrollRecords = payrollRecords; }
    public void setAttendanceRecords(List<Attendance> attendanceRecords)      { this.attendanceRecords = attendanceRecords; }
    public void setPerformanceRecords(List<Performance> performanceRecords)   { this.performanceRecords = performanceRecords; }
    public void setDepartments(List<Department> departments)                  { this.departments = departments; }
    public void setAppliedFilters(FilterCriteria filters)                     { this.appliedFilters = filters; }
    public void setTotalHeadcount(int totalHeadcount)                         { this.totalHeadcount = totalHeadcount; }
    public void setPreviousHeadcount(int previousHeadcount)                   { this.previousHeadcount = previousHeadcount; }
    public void setNewHires(int newHires)                                     { this.newHires = newHires; }
    public void setSeparations(int separations)                               { this.separations = separations; }
    public void setTotalGrossSalary(double totalGrossSalary)                  { this.totalGrossSalary = totalGrossSalary; }
    public void setTotalNetSalary(double totalNetSalary)                      { this.totalNetSalary = totalNetSalary; }
    public void setEmployeesByDepartment(Map<String, List<Employee>> map)     { this.employeesByDepartment = map; }
    public void setAvgSalaryByDepartment(Map<String, Double> map)            { this.avgSalaryByDepartment = map; }

    @Override
    public String toString() {
        return String.format("ProcessedData[employees=%d, headcount=%d, newHires=%d, separations=%d]",
                employees.size(), totalHeadcount, newHires, separations);
    }
}
