package com.hranalytics.domain;

import java.time.LocalDate;

/**
 * Domain class representing an HRMS employee record.
 * Read-only data sourced from the HRMS core; consumed by the analytics pipeline.
 */
public class Employee {

    public enum Status { ACTIVE, INACTIVE, ON_LEAVE }

    private final String employeeId;
    private final String name;
    private final String department;
    private final String designation;
    private final LocalDate joinDate;
    private final double baseSalary;
    private double performanceScore;
    private Status status;

    public Employee(String employeeId, String name, String department, String designation,
                    LocalDate joinDate, double baseSalary, double performanceScore, Status status) {
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.designation = designation;
        this.joinDate = joinDate;
        this.baseSalary = baseSalary;
        this.performanceScore = performanceScore;
        this.status = status;
    }

    public String getEmployeeId()      { return employeeId; }
    public String getName()            { return name; }
    public String getDepartment()      { return department; }
    public String getDesignation()     { return designation; }
    public LocalDate getJoinDate()     { return joinDate; }
    public double getBaseSalary()      { return baseSalary; }
    public double getPerformanceScore(){ return performanceScore; }
    public Status getStatus()          { return status; }

    public void setPerformanceScore(double score) { this.performanceScore = score; }
    public void setStatus(Status status)           { this.status = status; }

    @Override
    public String toString() {
        return String.format("Employee[%s, %s, %s, %s, %.1f]",
                employeeId, name, department, status, performanceScore);
    }
}
