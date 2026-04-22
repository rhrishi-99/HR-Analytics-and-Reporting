package com.hranalytics.integration.db;

import com.hranalytics.integration.external.PayrollRecord;
import com.hranalytics.integration.service.PayrollService;
import com.hrms.db.repositories.hranalytics.EmployeeServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that bridges the Database Sub-System's payroll data with our PayrollService.
 * Reuses EmployeeServiceImpl to traverse Employee -> PayrollResult relationships.
 */
public class DBPayrollServiceAdapter implements PayrollService {

    private final EmployeeServiceImpl dbService;

    public DBPayrollServiceAdapter() {
        this.dbService = new EmployeeServiceImpl();
    }

    @Override
    public List<PayrollRecord> getAllPayrollRecords() {
        List<com.hrms.db.entities.Employee> employees = dbService.getAllEmployees();
        if (employees == null) return Collections.emptyList();

        List<PayrollRecord> records = new ArrayList<>();
        for (com.hrms.db.entities.Employee emp : employees) {
            records.addAll(mapPayrollResults(emp));
        }
        return records;
    }

    @Override
    public List<PayrollRecord> getPayrollByEmployee(String employeeId) {
        com.hrms.db.entities.Employee emp = dbService.getEmployeeById(employeeId);
        if (emp == null) return Collections.emptyList();
        return mapPayrollResults(emp);
    }

    @Override
    public List<PayrollRecord> getPayrollByDateRange(LocalDate start, LocalDate end) {
        return getAllPayrollRecords().stream()
                .filter(r -> !r.getPaymentDate().isBefore(start) && !r.getPaymentDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private List<PayrollRecord> mapPayrollResults(com.hrms.db.entities.Employee e) {
        List<com.hrms.db.entities.PayrollResult> results = e.getPayrollResults();
        if (results == null) return Collections.emptyList();

        return results.stream().map(r -> new PayrollRecord(
                r.getRecordId() != null ? r.getRecordId() : "PAY-" + r.hashCode(),
                e.getEmpId(),
                r.getFinalGrossPay() != null ? r.getFinalGrossPay() : 0.0,
                (r.getFinalGrossPay() != null && r.getFinalNetPay() != null) 
                        ? (r.getFinalGrossPay() - r.getFinalNetPay()) : 0.0,
                r.getProcessedAt() != null ? r.getProcessedAt().toLocalDate() : LocalDate.now()
        )).collect(Collectors.toList());
    }
}
