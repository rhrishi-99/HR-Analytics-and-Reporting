package com.hranalytics.integration.db;

import com.hranalytics.integration.external.AttendanceRecord;
import com.hranalytics.integration.service.AttendanceService;
import com.hrms.db.repositories.hranalytics.EmployeeServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that bridges the Database Sub-System's time tracking data with our AttendanceService.
 * Maps daily TimeEntry entities to AttendanceRecord objects.
 */
public class DBAttendanceServiceAdapter implements AttendanceService {

    private final EmployeeServiceImpl dbService;

    public DBAttendanceServiceAdapter() {
        this.dbService = new EmployeeServiceImpl();
    }

    @Override
    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) {
        com.hrms.db.entities.Employee emp = dbService.getEmployeeById(employeeId);
        if (emp == null) return Collections.emptyList();
        return mapTimeEntries(emp);
    }

    @Override
    public List<AttendanceRecord> getAttendanceByDateRange(LocalDate start, LocalDate end) {
        List<com.hrms.db.entities.Employee> employees = dbService.getAllEmployees();
        if (employees == null) return Collections.emptyList();

        List<AttendanceRecord> allRecords = new ArrayList<>();
        for (com.hrms.db.entities.Employee emp : employees) {
            allRecords.addAll(mapTimeEntries(emp));
        }

        return allRecords.stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end) {
        return getAttendanceByEmployee(employeeId).stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .mapToDouble(AttendanceRecord::getHoursWorked)
                .sum();
    }

    private List<AttendanceRecord> mapTimeEntries(com.hrms.db.entities.Employee e) {
        List<com.hrms.db.entities.TimeEntry> entries = e.getTimeEntries();
        if (entries == null) return Collections.emptyList();

        return entries.stream().map(entry -> new AttendanceRecord(
                "ATT-" + (entry.getEntryId() != null ? entry.getEntryId() : entry.hashCode()),
                e.getEmpId(),
                entry.getDate(),
                entry.getStatus() != null ? entry.getStatus().toUpperCase() : "PRESENT",
                entry.getTotalHours() != null ? entry.getTotalHours() : 0.0
        )).collect(Collectors.toList());
    }
}
