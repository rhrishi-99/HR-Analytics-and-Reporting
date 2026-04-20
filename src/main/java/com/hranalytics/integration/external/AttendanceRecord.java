package com.hranalytics.integration.external;

import java.time.LocalDate;

/**
 * External data type returned by the Attendance Subsystem via AttendanceService.
 * AttendanceMapper converts this to our domain Attendance.
 */
public class AttendanceRecord {

    private final String attendanceId;
    private final String employeeId;
    private final LocalDate date;
    private final String status;      // "PRESENT", "ABSENT", "HALF_DAY", "ON_LEAVE"
    private final double hoursWorked;

    public AttendanceRecord(String attendanceId, String employeeId,
                            LocalDate date, String status, double hoursWorked) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
        this.hoursWorked = hoursWorked;
    }

    public String getAttendanceId() { return attendanceId; }
    public String getEmployeeId()   { return employeeId; }
    public LocalDate getDate()      { return date; }
    public String getStatus()       { return status; }
    public double getHoursWorked()  { return hoursWorked; }
}
