package com.hranalytics.integration.external;

import java.time.LocalDate;

/**
 * External data type returned by the Payroll Subsystem via PayrollService.
 * This is the Payroll team's schema; PayrollMapper converts it to our domain Payroll.
 */
public class PayrollRecord {

    private final String payrollId;
    private final String employeeId;
    private final double grossSalary;
    private final double deductions;
    private final double netSalary;
    private final LocalDate paymentDate;

    public PayrollRecord(String payrollId, String employeeId,
                         double grossSalary, double deductions, LocalDate paymentDate) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.grossSalary = grossSalary;
        this.deductions = deductions;
        this.netSalary = grossSalary - deductions;
        this.paymentDate = paymentDate;
    }

    public String getPayrollId()     { return payrollId; }
    public String getEmployeeId()    { return employeeId; }
    public double getGrossSalary()   { return grossSalary; }
    public double getDeductions()    { return deductions; }
    public double getNetSalary()     { return netSalary; }
    public LocalDate getPaymentDate(){ return paymentDate; }
}
