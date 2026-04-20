# HR Analytics & Reporting Sub-System — Database Team Integration Contract

**Version:** 1.0  
**Date:** 2026-04-19  
**From:** HR Analytics Team — R G Rhrishi (PES1UG23AM222), Prem M Thakur (PES1UG23AM214)  
**For:** Database Sub-System Team

---

## Overview

The HR Analytics sub-system depends on your team for all raw data reads.
We have defined **4 service interfaces** — one per data domain. Your team
implements these interfaces against the real database. We wire them in at
startup and the rest of our pipeline runs unchanged.

**You never call us. We call you.**

---

## The Golden Rule

We depend on interfaces, never on concrete classes. You return the exact
record types defined in this document. Do not change field names, field
types, or method signatures — our mappers depend on them exactly.

---

## 1. EmployeeService

```java
package com.hranalytics.integration.service;

import com.hranalytics.domain.Employee;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(String employeeId);
    List<Employee> getEmployeesByDepartment(String department);
    List<Employee> getActiveEmployees();
}
```

**What each method must return:**

| Method | Returns | Notes |
|--------|---------|-------|
| `getAllEmployees()` | All employees regardless of status | Never return null — return empty list |
| `getEmployeeById(id)` | Single employee or `null` | Return null if not found |
| `getEmployeesByDepartment(dept)` | Employees in that department | Case-sensitive match on `department` field |
| `getActiveEmployees()` | Only `Status.ACTIVE` employees | Used for headcount calculations |

**The `Employee` domain class (do not modify):**

```java
package com.hranalytics.domain;

import java.time.LocalDate;

public class Employee {

    public enum Status { ACTIVE, INACTIVE, ON_LEAVE }

    private final String employeeId;   // e.g. "E001"
    private final String name;         // full name
    private final String department;   // e.g. "Engineering", "HR", "Finance"
    private final String designation;  // e.g. "Software Engineer"
    private final LocalDate joinDate;
    private final double baseSalary;
    private double performanceScore;   // 0.0 – 5.0
    private Status status;

    // Constructor
    public Employee(String employeeId, String name, String department,
                    String designation, LocalDate joinDate,
                    double baseSalary, double performanceScore, Status status) { ... }

    // Getters
    public String getEmployeeId()       { return employeeId; }
    public String getName()             { return name; }
    public String getDepartment()       { return department; }
    public String getDesignation()      { return designation; }
    public LocalDate getJoinDate()      { return joinDate; }
    public double getBaseSalary()       { return baseSalary; }
    public double getPerformanceScore() { return performanceScore; }
    public Status getStatus()           { return status; }
}
```

**Example implementation skeleton:**

```java
public class EmployeeRepository implements EmployeeService {

    @Override
    public List<Employee> getAllEmployees() {
        // query: SELECT * FROM employees
        // map each row to new Employee(...)
    }

    @Override
    public Employee getEmployeeById(String employeeId) {
        // query: SELECT * FROM employees WHERE employee_id = ?
    }

    @Override
    public List<Employee> getEmployeesByDepartment(String department) {
        // query: SELECT * FROM employees WHERE department = ?
    }

    @Override
    public List<Employee> getActiveEmployees() {
        // query: SELECT * FROM employees WHERE status = 'ACTIVE'
    }
}
```

---

## 2. PayrollService

```java
package com.hranalytics.integration.service;

import com.hranalytics.integration.external.PayrollRecord;
import java.time.LocalDate;
import java.util.List;

public interface PayrollService {
    List<PayrollRecord> getAllPayrollRecords();
    List<PayrollRecord> getPayrollByEmployee(String employeeId);
    List<PayrollRecord> getPayrollByDateRange(LocalDate start, LocalDate end);
}
```

**What each method must return:**

| Method | Returns | Notes |
|--------|---------|-------|
| `getAllPayrollRecords()` | All payroll records | Never return null |
| `getPayrollByEmployee(id)` | All payroll records for that employee | All-time, not date-scoped |
| `getPayrollByDateRange(start, end)` | Records where `paymentDate` falls in `[start, end]` | Both dates inclusive |

**The `PayrollRecord` type (do not modify):**

```java
package com.hranalytics.integration.external;

import java.time.LocalDate;

public class PayrollRecord {
    // Constructor — netSalary is computed as grossSalary - deductions
    public PayrollRecord(String payrollId, String employeeId,
                         double grossSalary, double deductions, LocalDate paymentDate)

    public String getPayrollId()      // e.g. "PAY-001"
    public String getEmployeeId()     // must match Employee.employeeId
    public double getGrossSalary()    // before deductions
    public double getDeductions()     // total deductions
    public double getNetSalary()      // grossSalary - deductions (computed)
    public LocalDate getPaymentDate() // date salary was paid
}
```

**Example implementation skeleton:**

```java
public class PayrollRepository implements PayrollService {

    @Override
    public List<PayrollRecord> getAllPayrollRecords() {
        // SELECT * FROM payroll
    }

    @Override
    public List<PayrollRecord> getPayrollByEmployee(String employeeId) {
        // SELECT * FROM payroll WHERE employee_id = ?
    }

    @Override
    public List<PayrollRecord> getPayrollByDateRange(LocalDate start, LocalDate end) {
        // SELECT * FROM payroll WHERE payment_date BETWEEN ? AND ?
    }
}
```

---

## 3. AttendanceService

```java
package com.hranalytics.integration.service;

import com.hranalytics.integration.external.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    List<AttendanceRecord> getAttendanceByEmployee(String employeeId);
    List<AttendanceRecord> getAttendanceByDateRange(LocalDate start, LocalDate end);
    double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end);
}
```

**What each method must return:**

| Method | Returns | Notes |
|--------|---------|-------|
| `getAttendanceByEmployee(id)` | All attendance records for that employee | All-time |
| `getAttendanceByDateRange(start, end)` | Records where `date` falls in `[start, end]` | All employees, both dates inclusive |
| `getTotalHoursWorked(id, start, end)` | Sum of `hoursWorked` in the date range | Return `0.0` if no records |

**The `AttendanceRecord` type (do not modify):**

```java
package com.hranalytics.integration.external;

import java.time.LocalDate;

public class AttendanceRecord {
    public AttendanceRecord(String attendanceId, String employeeId,
                            LocalDate date, String status, double hoursWorked)

    public String getAttendanceId() // e.g. "ATT-001"
    public String getEmployeeId()   // must match Employee.employeeId
    public LocalDate getDate()      // the working day
    public String getStatus()       // "PRESENT" | "ABSENT" | "HALF_DAY" | "ON_LEAVE"
    public double getHoursWorked()  // 0.0 – 8.0 (0 if ABSENT)
}
```

**Valid `status` values — use exactly these strings:**

| Value | Meaning |
|-------|---------|
| `"PRESENT"` | Full day worked |
| `"ABSENT"` | Not present, no hours |
| `"HALF_DAY"` | Half day worked |
| `"ON_LEAVE"` | Approved leave |

**Example implementation skeleton:**

```java
public class AttendanceRepository implements AttendanceService {

    @Override
    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) {
        // SELECT * FROM attendance WHERE employee_id = ?
    }

    @Override
    public List<AttendanceRecord> getAttendanceByDateRange(LocalDate start, LocalDate end) {
        // SELECT * FROM attendance WHERE date BETWEEN ? AND ?
    }

    @Override
    public double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end) {
        // SELECT SUM(hours_worked) FROM attendance
        // WHERE employee_id = ? AND date BETWEEN ? AND ?
    }
}
```

---

## 4. PerformanceService

```java
package com.hranalytics.integration.service;

import com.hranalytics.integration.external.PerformanceRecord;
import java.util.List;

public interface PerformanceService {
    List<PerformanceRecord> getPerformanceByEmployee(String employeeId);
    List<PerformanceRecord> getPerformanceByCycle(String cycle);
    double getAveragePerformanceScore(String employeeId);
}
```

**What each method must return:**

| Method | Returns | Notes |
|--------|---------|-------|
| `getPerformanceByEmployee(id)` | All performance reviews for that employee | All cycles |
| `getPerformanceByCycle(cycle)` | All reviews for that cycle label | e.g. `"Q1-2025"` |
| `getAveragePerformanceScore(id)` | Average score across all reviews | Return `0.0` if no records |

**The `PerformanceRecord` type (do not modify):**

```java
package com.hranalytics.integration.external;

import java.time.LocalDate;

public class PerformanceRecord {
    public PerformanceRecord(String performanceId, String employeeId,
                             double score, String feedback,
                             LocalDate reviewDate, String reviewer, String cycle)

    public String getPerformanceId() // e.g. "PERF-001"
    public String getEmployeeId()    // must match Employee.employeeId
    public double getScore()         // 0.0 – 5.0
    public String getFeedback()      // free-text review comments
    public LocalDate getReviewDate() // date of the review
    public String getReviewer()      // reviewer's name or ID
    public String getCycle()         // e.g. "Q1-2025", "Q2-2025"
}
```

**Cycle label format:** `"Q{quarter}-{year}"` — e.g. `"Q1-2025"`, `"Q2-2025"`.
We derive the cycle from the filter date range, so the format must match exactly.

**Example implementation skeleton:**

```java
public class PerformanceRepository implements PerformanceService {

    @Override
    public List<PerformanceRecord> getPerformanceByEmployee(String employeeId) {
        // SELECT * FROM performance_reviews WHERE employee_id = ?
    }

    @Override
    public List<PerformanceRecord> getPerformanceByCycle(String cycle) {
        // SELECT * FROM performance_reviews WHERE cycle = ?
    }

    @Override
    public double getAveragePerformanceScore(String employeeId) {
        // SELECT AVG(score) FROM performance_reviews WHERE employee_id = ?
    }
}
```

---

## 5. Wiring — How We Connect Your Implementations

At startup, your team passes the four concrete classes to us:

```java
// Their code hands us the real implementations
EmployeeService   employeeRepo   = new EmployeeRepository();
PayrollService    payrollRepo    = new PayrollRepository();
AttendanceService attendanceRepo = new AttendanceRepository();
PerformanceService perfRepo      = new PerformanceRepository();

// We wire them into the facade — nothing else changes
HRAnalyticsFacade facade = new HRAnalyticsFacade(
        accessControl,
        chartFactory,
        employeeRepo,
        payrollRepo,
        attendanceRepo,
        new PerformanceManagementClient(perfRepo),
        essPublisher);
```

---

## 6. Data Constraints

| Field | Type | Constraint |
|-------|------|-----------|
| `employeeId` | `String` | Must be consistent across all 4 services |
| `score` (Performance) | `double` | Must be in range `0.0 – 5.0` |
| `status` (Attendance) | `String` | Must be one of the 4 valid values |
| `hoursWorked` | `double` | Must be `0.0 – 8.0` |
| `grossSalary`, `baseSalary` | `double` | Must be positive |
| `deductions` | `double` | Must be `≥ 0` and `< grossSalary` |
| All dates | `LocalDate` | Must not be null |
| All ID fields | `String` | Must not be null or blank |

---

## 7. Error Handling

- **Never throw checked exceptions** from your implementations — wrap them in a `RuntimeException` if needed. Our pipeline catches `RuntimeException` and raises `INVALID_HRMS_DATA_SOURCE`.
- **Never return null lists** — return `Collections.emptyList()` instead.
- **Null for single records is fine** — `getEmployeeById()` returning `null` means not found.

---

## 8. Files to Copy Into Your Project

Your implementations will need these types — copy them exactly:

```
com/hranalytics/integration/service/EmployeeService.java
com/hranalytics/integration/service/PayrollService.java
com/hranalytics/integration/service/AttendanceService.java
com/hranalytics/integration/service/PerformanceService.java
com/hranalytics/domain/Employee.java
com/hranalytics/integration/external/PayrollRecord.java
com/hranalytics/integration/external/AttendanceRecord.java
com/hranalytics/integration/external/PerformanceRecord.java
```

---

*Contact: Prem M Thakur (PES1UG23AM214) for integration queries.*
