package com.hranalytics.integration.mapper;

import com.hranalytics.domain.Payroll;
import com.hranalytics.integration.external.PayrollRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts PayrollRecord (Payroll Subsystem schema) to domain Payroll objects.
 * Isolates our pipeline from external schema changes — only this class needs updating
 * if the Payroll team changes their field names or types.
 */
public class PayrollMapper {

    /** Converts a single PayrollRecord to a domain Payroll. */
    public static Payroll toDomain(PayrollRecord record) {
        return new Payroll(
                record.getPayrollId(),
                record.getEmployeeId(),
                record.getGrossSalary(),
                record.getDeductions(),
                record.getPaymentDate()
        );
    }

    /** Converts a list of PayrollRecords to domain Payroll objects. */
    public static List<Payroll> toDomainList(List<PayrollRecord> records) {
        return records.stream()
                .map(PayrollMapper::toDomain)
                .collect(Collectors.toList());
    }
}
