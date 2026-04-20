package com.hranalytics.domain;

import java.time.LocalDate;

/**
 * Domain class representing an attendance record for one employee on one day.
 * Read-only data sourced from the HRMS core.
 */
public class Attendance {

    public enum AttendanceStatus { PRESENT, ABSENT, HALF_DAY, ON_LEAVE }

    private final String attendanceId;
    private final String employeeId;
    private final LocalDate date;
    private final AttendanceStatus attendanceStatus;
    private final double hoursWorked;

    public Attendance(String attendanceId, String employeeId, LocalDate date,
                      AttendanceStatus attendanceStatus, double hoursWorked) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.date = date;
        this.attendanceStatus = attendanceStatus;
        this.hoursWorked = hoursWorked;
    }

    public String getAttendanceId()              { return attendanceId; }
    public String getEmployeeId()                { return employeeId; }
    public LocalDate getDate()                   { return date; }
    public AttendanceStatus getAttendanceStatus(){ return attendanceStatus; }
    public double getHoursWorked()               { return hoursWorked; }

    @Override
    public String toString() {
        return String.format("Attendance[%s, emp=%s, %s, %.1fh]",
                attendanceId, employeeId, attendanceStatus, hoursWorked);
    }
}
