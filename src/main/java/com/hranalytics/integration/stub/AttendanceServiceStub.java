package com.hranalytics.integration.stub;

import com.hranalytics.integration.external.AttendanceRecord;
import com.hranalytics.integration.service.AttendanceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stub implementation of AttendanceService backed by hardcoded simulation data.
 * Swap for the Attendance team's real implementation in HRAnalyticsFacade with no other changes.
 */
public class AttendanceServiceStub implements AttendanceService {

    private final List<AttendanceRecord> records = buildSimulatedAttendance();

    @Override
    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) {
        return records.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecord> getAttendanceByDateRange(LocalDate start, LocalDate end) {
        return records.stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end) {
        return records.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId)
                        && !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .mapToDouble(AttendanceRecord::getHoursWorked)
                .sum();
    }

    private static List<AttendanceRecord> buildSimulatedAttendance() {
        LocalDate date = LocalDate.of(2025, 3, 31);
        String[] empIds = {"E001","E002","E003","E004","E005","E006",
                           "E007","E008","E009","E010","E011","E012","E013","E014"};
        double[] hours  = {160, 152, 168, 156, 148, 160, 168, 162, 155, 150, 158, 145, 163, 157};
        List<AttendanceRecord> list = new ArrayList<>();
        for (int i = 0; i < empIds.length; i++) {
            list.add(new AttendanceRecord(
                    "A" + String.format("%03d", i + 1),
                    empIds[i], date, "PRESENT", hours[i]));
        }
        return list;
    }
}
