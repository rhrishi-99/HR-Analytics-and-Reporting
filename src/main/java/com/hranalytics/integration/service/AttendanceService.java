package com.hranalytics.integration.service;

import com.hranalytics.integration.external.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Integration contract provided by the Attendance Subsystem team.
 * DataCollectionModule depends on this interface — never on a concrete implementation.
 * Stub: AttendanceServiceStub. Results are mapped to domain Attendance via AttendanceMapper.
 */
public interface AttendanceService {
    List<AttendanceRecord> getAttendanceByEmployee(String employeeId);
    List<AttendanceRecord> getAttendanceByDateRange(LocalDate start, LocalDate end);
    double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end);
}
