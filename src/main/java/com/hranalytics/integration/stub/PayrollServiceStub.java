package com.hranalytics.integration.stub;

import com.hranalytics.integration.external.PayrollRecord;
import com.hranalytics.integration.service.PayrollService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stub implementation of PayrollService backed by hardcoded simulation data.
 * Swap for the Payroll team's real implementation in HRAnalyticsFacade with no other changes.
 */
public class PayrollServiceStub implements PayrollService {

    private final List<PayrollRecord> records = buildSimulatedPayroll();

    @Override
    public List<PayrollRecord> getAllPayrollRecords() {
        return new ArrayList<>(records);
    }

    @Override
    public List<PayrollRecord> getPayrollByEmployee(String employeeId) {
        return records.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PayrollRecord> getPayrollByDateRange(LocalDate start, LocalDate end) {
        return records.stream()
                .filter(r -> !r.getPaymentDate().isBefore(start) && !r.getPaymentDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private static List<PayrollRecord> buildSimulatedPayroll() {
        LocalDate payDate = LocalDate.of(2025, 3, 31);
        List<PayrollRecord> list = new ArrayList<>();
        list.add(new PayrollRecord("P001", "E001", 95000,  9500,  payDate));
        list.add(new PayrollRecord("P002", "E002", 65000,  6500,  payDate));
        list.add(new PayrollRecord("P003", "E003", 120000, 12000, payDate));
        list.add(new PayrollRecord("P004", "E004", 85000,  8500,  payDate));
        list.add(new PayrollRecord("P005", "E005", 55000,  5500,  payDate));
        list.add(new PayrollRecord("P006", "E006", 75000,  7500,  payDate));
        list.add(new PayrollRecord("P007", "E007", 150000, 30000, payDate));
        list.add(new PayrollRecord("P008", "E008", 88000,  8800,  payDate));
        list.add(new PayrollRecord("P009", "E009", 72000,  7200,  payDate));
        list.add(new PayrollRecord("P010", "E010", 52000,  5200,  payDate));
        list.add(new PayrollRecord("P011", "E011", 68000,  6800,  payDate));
        list.add(new PayrollRecord("P012", "E012", 70000,  7000,  payDate));
        list.add(new PayrollRecord("P013", "E013", 92000,  9200,  payDate));
        list.add(new PayrollRecord("P014", "E014", 78000,  7800,  payDate));
        return list;
    }
}
