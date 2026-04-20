package com.hranalytics.pipeline;

import com.hranalytics.domain.*;
import com.hranalytics.exceptions.InvalidHRMSDataSourceException;
import com.hranalytics.integration.mapper.AttendanceMapper;
import com.hranalytics.integration.mapper.PayrollMapper;
import com.hranalytics.integration.mapper.PerformanceMapper;
import com.hranalytics.integration.service.AttendanceService;
import com.hranalytics.integration.service.EmployeeService;
import com.hranalytics.integration.service.PayrollService;
import com.hranalytics.integration.service.PerformanceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * First stage of the analytics pipeline.
 * Collects raw HR data by calling the four subsystem service interfaces:
 *   - EmployeeService  (Database Team)
 *   - PayrollService   (Payroll Subsystem)
 *   - AttendanceService(Attendance Subsystem)
 *   - PerformanceService (Performance Management Sub-System — Integration 1)
 *
 * Depends only on interfaces; concrete implementations (stubs or production) are injected.
 * Retries transient failures up to MAX_RETRIES times before raising INVALID_HRMS_DATA_SOURCE.
 * Owner: Prem M Thakur (pipeline boundary).
 */
public class DataCollectionModule {

    private static final Logger LOG = Logger.getLogger(DataCollectionModule.class.getName());
    private static final int MAX_RETRIES = 3;

    private final EmployeeService   employeeService;
    private final PayrollService    payrollService;
    private final AttendanceService attendanceService;
    private final PerformanceService performanceService;

    public DataCollectionModule(EmployeeService employeeService,
                                PayrollService payrollService,
                                AttendanceService attendanceService,
                                PerformanceService performanceService) {
        this.employeeService    = employeeService;
        this.payrollService     = payrollService;
        this.attendanceService  = attendanceService;
        this.performanceService = performanceService;
    }

    /**
     * Collects all HR data needed for the analytics pipeline.
     * Each subsystem call is retried up to MAX_RETRIES times on transient failure.
     */
    public RawHRData collectAll(FilterCriteria filters) {
        RawHRData raw = new RawHRData();

        raw.setEmployees(fetchEmployees(filters));
        raw.setDepartments(deriveDepartments(raw.getEmployees()));
        raw.setPayrollRecords(fetchPayroll(filters));
        raw.setAttendanceRecords(fetchAttendance(filters));
        raw.setPerformanceRecords(fetchPerformance(filters));

        LOG.info(String.format(
                "DataCollectionModule: collected %d employees, %d payroll, %d attendance, %d performance records.",
                raw.getEmployees().size(), raw.getPayrollRecords().size(),
                raw.getAttendanceRecords().size(), raw.getPerformanceRecords().size()));
        return raw;
    }

    /** Fetches employees from the Database Team's EmployeeService. */
    private List<Employee> fetchEmployees(FilterCriteria filters) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                List<String> deptFilter = (filters != null) ? filters.getFilterDepartment() : List.of();
                if (!deptFilter.isEmpty()) {
                    // Fetch per-department and merge (EmployeeService supports this natively)
                    List<Employee> result = new ArrayList<>();
                    for (String dept : deptFilter) {
                        result.addAll(employeeService.getEmployeesByDepartment(dept));
                    }
                    return result;
                }
                return employeeService.getAllEmployees();
            } catch (InvalidHRMSDataSourceException ex) {
                throw ex; // already the right exception type — re-raise immediately
            } catch (Exception ex) {
                LOG.warning("EmployeeService attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new InvalidHRMSDataSourceException(
                            "EmployeeService unavailable after " + MAX_RETRIES + " retries.", ex);
                }
            }
        }
        return List.of();
    }

    /**
     * Derives a department list from the employee roster.
     * Departments are owned by the Database Team; we reconstruct summary objects locally
     * rather than calling a separate DepartmentService (none provided by the DB team).
     */
    private List<Department> deriveDepartments(List<Employee> employees) {
        java.util.Map<String, Long> counts = employees.stream()
                .filter(e -> e.getStatus() == Employee.Status.ACTIVE)
                .collect(java.util.stream.Collectors.groupingBy(
                        Employee::getDepartment, java.util.stream.Collectors.counting()));

        // Manager name comes from HR knowledge; hardcoded pending a DepartmentService interface
        java.util.Map<String, String> managers = java.util.Map.of(
                "Engineering", "Carol White",
                "HR",          "David Lee",
                "Finance",     "Grace Kim");

        List<Department> depts = new ArrayList<>();
        counts.forEach((name, count) -> {
            String prefix = name.substring(0, Math.min(3, name.length())).toUpperCase();
            depts.add(new Department("D-" + prefix,
                    name, managers.getOrDefault(name, "TBD"), count.intValue()));
        });
        return depts;
    }

    /** Fetches payroll records from the Payroll Subsystem and maps to domain objects. */
    private List<Payroll> fetchPayroll(FilterCriteria filters) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (filters != null && filters.hasValidDateRange()) {
                    DateRange r = filters.getDateRange();
                    return PayrollMapper.toDomainList(
                            payrollService.getPayrollByDateRange(r.getStartDate(), r.getEndDate()));
                }
                return PayrollMapper.toDomainList(payrollService.getAllPayrollRecords());
            } catch (Exception ex) {
                LOG.warning("PayrollService attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new InvalidHRMSDataSourceException(
                            "PayrollService unavailable after " + MAX_RETRIES + " retries.", ex);
                }
            }
        }
        return List.of();
    }

    /** Fetches attendance records from the Attendance Subsystem and maps to domain objects. */
    private List<Attendance> fetchAttendance(FilterCriteria filters) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                LocalDate start = LocalDate.of(2025, 1, 1);
                LocalDate end   = LocalDate.of(2025, 3, 31);
                if (filters != null && filters.hasValidDateRange()) {
                    start = filters.getDateRange().getStartDate();
                    end   = filters.getDateRange().getEndDate();
                }
                return AttendanceMapper.toDomainList(
                        attendanceService.getAttendanceByDateRange(start, end));
            } catch (Exception ex) {
                LOG.warning("AttendanceService attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new InvalidHRMSDataSourceException(
                            "AttendanceService unavailable after " + MAX_RETRIES + " retries.", ex);
                }
            }
        }
        return List.of();
    }

    /**
     * Fetches performance records from the Performance Management Sub-System (Integration 1).
     * Uses getPerformanceByCycle() — the cycle is derived from the filter date range.
     */
    private List<Performance> fetchPerformance(FilterCriteria filters) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Derive the review cycle label from the filter range; default to Q1-2025
                String cycle = deriveCycleLabel(filters);
                LOG.info("DataCollectionModule: fetching performance records for cycle=" + cycle);
                return PerformanceMapper.toDomainList(
                        performanceService.getPerformanceByCycle(cycle));
            } catch (InvalidHRMSDataSourceException ex) {
                throw ex;
            } catch (Exception ex) {
                LOG.warning("PerformanceService attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new InvalidHRMSDataSourceException(
                            "Performance Management Sub-System unavailable after " + MAX_RETRIES + " retries.", ex);
                }
            }
        }
        return List.of();
    }

    /** Converts a date-range filter to a cycle label such as "Q1-2025". */
    private String deriveCycleLabel(FilterCriteria filters) {
        if (filters == null || !filters.hasValidDateRange()) return "Q1-2025";
        LocalDate start = filters.getDateRange().getStartDate();
        int quarter = (start.getMonthValue() - 1) / 3 + 1;
        return "Q" + quarter + "-" + start.getYear();
    }
}
