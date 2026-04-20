package com.hranalytics.integration.mapper;

import com.hranalytics.domain.Attendance;
import com.hranalytics.integration.external.AttendanceRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts AttendanceRecord (Attendance Subsystem schema) to domain Attendance objects.
 * Translates the external string status field to the domain AttendanceStatus enum.
 * Only this class needs updating if the Attendance team changes their status vocabulary.
 */
public class AttendanceMapper {

    /** Converts a single AttendanceRecord to a domain Attendance. */
    public static Attendance toDomain(AttendanceRecord record) {
        return new Attendance(
                record.getAttendanceId(),
                record.getEmployeeId(),
                record.getDate(),
                parseStatus(record.getStatus()),
                record.getHoursWorked()
        );
    }

    /** Converts a list of AttendanceRecords to domain Attendance objects. */
    public static List<Attendance> toDomainList(List<AttendanceRecord> records) {
        return records.stream()
                .map(AttendanceMapper::toDomain)
                .collect(Collectors.toList());
    }

    /** Maps the external subsystem's status string to the domain enum value. */
    private static Attendance.AttendanceStatus parseStatus(String status) {
        if (status == null) return Attendance.AttendanceStatus.ABSENT;
        return switch (status.toUpperCase()) {
            case "PRESENT"   -> Attendance.AttendanceStatus.PRESENT;
            case "HALF_DAY"  -> Attendance.AttendanceStatus.HALF_DAY;
            case "ON_LEAVE"  -> Attendance.AttendanceStatus.ON_LEAVE;
            default          -> Attendance.AttendanceStatus.ABSENT;
        };
    }
}
