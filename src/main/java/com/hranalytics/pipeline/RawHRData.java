package com.hranalytics.pipeline;

import com.hranalytics.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate container holding unvalidated raw data collected from all HRMS modules.
 * Passed from DataCollectionModule to DataIntegrationLayer for validation and merging.
 */
public class RawHRData {

    private List<Employee> employees        = new ArrayList<>();
    private List<Payroll> payrollRecords    = new ArrayList<>();
    private List<Attendance> attendanceRecords = new ArrayList<>();
    private List<Performance> performanceRecords = new ArrayList<>();
    private List<Department> departments    = new ArrayList<>();

    public List<Employee> getEmployees()             { return employees; }
    public List<Payroll> getPayrollRecords()         { return payrollRecords; }
    public List<Attendance> getAttendanceRecords()   { return attendanceRecords; }
    public List<Performance> getPerformanceRecords() { return performanceRecords; }
    public List<Department> getDepartments()         { return departments; }

    public void setEmployees(List<Employee> employees)                     { this.employees = employees; }
    public void setPayrollRecords(List<Payroll> payrollRecords)            { this.payrollRecords = payrollRecords; }
    public void setAttendanceRecords(List<Attendance> attendanceRecords)   { this.attendanceRecords = attendanceRecords; }
    public void setPerformanceRecords(List<Performance> performanceRecords){ this.performanceRecords = performanceRecords; }
    public void setDepartments(List<Department> departments)               { this.departments = departments; }
}
