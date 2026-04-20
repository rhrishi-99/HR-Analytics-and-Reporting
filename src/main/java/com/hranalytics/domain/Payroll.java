package com.hranalytics.domain;

import java.time.LocalDate;

/**
 * Domain class representing a payroll record for one employee in one pay period.
 * Read-only data sourced from the HRMS core.
 */
public class Payroll {

    private final String payrollId;
    private final String employeeId;
    private final double grossSalary;
    private final double deductions;
    private final double netSalary;
    private final LocalDate paymentDate;

    public Payroll(String payrollId, String employeeId,
                   double grossSalary, double deductions, LocalDate paymentDate) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.grossSalary = grossSalary;
        this.deductions = deductions;
        this.netSalary = grossSalary - deductions;
        this.paymentDate = paymentDate;
    }

    public String getPayrollId()    { return payrollId; }
    public String getEmployeeId()   { return employeeId; }
    public double getGrossSalary()  { return grossSalary; }
    public double getDeductions()   { return deductions; }
    public double getNetSalary()    { return netSalary; }
    public LocalDate getPaymentDate(){ return paymentDate; }

    @Override
    public String toString() {
        return String.format("Payroll[%s, emp=%s, gross=%.2f, net=%.2f]",
                payrollId, employeeId, grossSalary, netSalary);
    }
}
