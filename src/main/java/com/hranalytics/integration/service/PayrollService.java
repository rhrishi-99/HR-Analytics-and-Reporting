package com.hranalytics.integration.service;

import com.hranalytics.integration.external.PayrollRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Integration contract provided by the Payroll Subsystem team.
 * DataCollectionModule depends on this interface — never on a concrete implementation.
 * Stub: PayrollServiceStub. Results are mapped to domain Payroll via PayrollMapper.
 */
public interface PayrollService {
    List<PayrollRecord> getAllPayrollRecords();
    List<PayrollRecord> getPayrollByEmployee(String employeeId);
    List<PayrollRecord> getPayrollByDateRange(LocalDate start, LocalDate end);
}
