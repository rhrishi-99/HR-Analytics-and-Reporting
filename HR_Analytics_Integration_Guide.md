# HR Analytics & Reporting Sub-System — External Integration Guide

**Version:** 1.0  
**Date:** 2026-04-19  
**Team:** R G Rhrishi (PES1UG23AM222), Prem M Thakur (PES1UG23AM214), Raihan Naeem (PES1UG23AM227)  
**Contact owner:** Prem M Thakur (integration boundary)

---

## Overview

The HR Analytics & Reporting Sub-System ingests raw HR data (employees, payroll,
attendance, performance) and produces dashboards, KPI cards, charts, and exportable
reports. It exposes **two integration surfaces** depending on who you are:

| You are | Interface to use | Direction |
|---------|-----------------|-----------|
| ESS Portal / Manager Dashboard | `HRAnalyticsService` | You call us |
| Attrition-Risk Sub-System | `IHRAnalyticsReportingService` | Bidirectional |

**Golden rule:** Never reference `HRAnalyticsFacade` or any internal class.
Always program to the interface we give you.

---

## PART A — ESS Portal & Manager Dashboard

### A1. What you get

An instance of `HRAnalyticsService` at wiring time. This is the only object
your subsystem needs to import from us.

```
Package:   com.hranalytics.integration.service
Interface: HRAnalyticsService
```

### A2. Files to copy into your project

```
com/hranalytics/integration/service/HRAnalyticsService.java
com/hranalytics/integration/dto/DashboardSnapshot.java
com/hranalytics/integration/dto/KPISnapshot.java
com/hranalytics/integration/dto/ReportSummary.java
com/hranalytics/domain/FilterCriteria.java
com/hranalytics/domain/DateRange.java
com/hranalytics/exceptions/HRAnalyticsException.java
com/hranalytics/exceptions/UnauthorizedAccessException.java
com/hranalytics/exceptions/ExportFormatUnsupportedException.java
com/hranalytics/exceptions/DashboardWidgetRenderFailureException.java
```

### A3. Declare the dependency (DIP — never instantiate directly)

```java
public class ESSPortalController {

    private final HRAnalyticsService hrAnalytics;

    // Injected at startup — never call `new HRAnalyticsFacade(...)`
    public ESSPortalController(HRAnalyticsService hrAnalytics) {
        this.hrAnalytics = hrAnalytics;
    }
}
```

### A4. Build a filter

All methods accept a `FilterCriteria`. All fields are optional — defaults return all data.

```java
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.domain.DateRange;
import java.time.LocalDate;

// Scoped filter
FilterCriteria filters = new FilterCriteria();
filters.setDateRange(new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)));
filters.setFilterDepartment(List.of("Engineering", "HR"));
filters.setFilterStatus("ACTIVE"); // "ALL" | "ACTIVE" | "INACTIVE"

// No filter — returns all data
FilterCriteria all = new FilterCriteria();
```

### A5. Load a dashboard

```java
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.exceptions.UnauthorizedAccessException;

try {
    DashboardSnapshot snap = hrAnalytics.loadDashboard("U002", filters);

    // Summary metrics
    System.out.println(snap.getDashboardId());
    System.out.printf("Attrition:   %.2f%%%n", snap.getAttritionRate());
    System.out.printf("Growth:      %.2f%%%n", snap.getEmployeeGrowth());
    System.out.printf("Performance: %.2f/5.0%n", snap.getAvgPerformance());

    // KPI cards
    for (KPISnapshot kpi : snap.getKpiCards()) {
        System.out.printf("%s: %.2f%s [%s]%n",
            kpi.getMetricName(), kpi.getCurrentValue(),
            kpi.getUnit(), kpi.getTrend()); // trend: "UP" | "DOWN" | "STABLE"
        if (kpi.isFlagged()) {
            // Data was unavailable — show placeholder
        }
    }

    // AI-generated insight strings
    snap.getInsights().forEach(System.out::println);

    // Widget IDs for rendering chart components
    snap.getWidgetIds().forEach(System.out::println);

} catch (UnauthorizedAccessException ex) {
    // userId does not have VIEW_DASHBOARD permission
    System.err.println(ex.getUserId() + " is not authorised: " + ex.getAttemptedAction());
}
```

**DashboardSnapshot fields:**

| Getter | Type | Description |
|--------|------|-------------|
| `getDashboardId()` | `String` | Unique ID for this render |
| `getAttritionRate()` | `double` | Attrition % |
| `getEmployeeGrowth()` | `double` | Headcount growth % |
| `getAvgPerformance()` | `double` | Average score out of 5.0 |
| `getKpiCards()` | `List<KPISnapshot>` | One entry per metric |
| `getWidgetIds()` | `List<String>` | Chart widget IDs |
| `getInsights()` | `List<String>` | Analytical summaries |
| `getGeneratedAt()` | `LocalDateTime` | Render timestamp |

**KPISnapshot fields:**

| Getter | Type | Description |
|--------|------|-------------|
| `getMetricName()` | `String` | e.g. "Attrition Rate" |
| `getCurrentValue()` | `double` | Current computed value |
| `getPreviousValue()` | `double` | Previous period value |
| `getTrend()` | `String` | `"UP"` / `"DOWN"` / `"STABLE"` |
| `getUnit()` | `String` | `"%"` / `"/5.0"` / `"$"` |
| `isFlagged()` | `boolean` | `true` = data unavailable |

### A6. Generate a report

```java
import com.hranalytics.integration.dto.ReportSummary;

ReportSummary summary = hrAnalytics.generateReport("FULL_HR_SUMMARY", filters);

System.out.println(summary.getReportId());    // use this to export
System.out.println(summary.getReportTitle());
System.out.println(summary.getReportType());
System.out.println(summary.getSectionCount());
System.out.println(summary.getGeneratedDate());
```

**Valid reportType values:**

| Value | Description |
|-------|-------------|
| `"ATTRITION"` | Attrition rate report |
| `"EMPLOYEE_GROWTH"` | Headcount growth report |
| `"COMPENSATION"` | Payroll and compensation analytics |
| `"DEPARTMENT_METRICS"` | Per-department KPI breakdown |
| `"PERFORMANCE"` | Performance scores and reviews |
| `"FULL_HR_SUMMARY"` | All of the above combined |

### A7. Export a report

```java
import com.hranalytics.exceptions.ExportFormatUnsupportedException;

try {
    String filePath = hrAnalytics.exportReport(summary.getReportId(), "pdf");
    System.out.println("Exported to: " + filePath);
} catch (ExportFormatUnsupportedException ex) {
    System.out.println("Supported formats: " + ex.getSupportedFormats());
}
```

**Valid format values:** `"csv"` · `"pdf"` · `"xlsx"`

### A8. Get a KPI snapshot for a department widget

```java
import com.hranalytics.exceptions.UnauthorizedAccessException;

try {
    List<KPISnapshot> kpis = hrAnalytics.getKPISnapshot("U003", "Engineering");
    kpis.forEach(k -> System.out.printf(
        "%s: %.2f%s [%s]%n",
        k.getMetricName(), k.getCurrentValue(), k.getUnit(), k.getTrend()));
} catch (UnauthorizedAccessException ex) {
    System.err.println("Not authorised: " + ex.getMessage());
}
```

Pass `null` as `deptId` to get KPIs across all departments.

### A9. Exceptions reference

All exceptions extend `HRAnalyticsException extends RuntimeException`.

| Exception | Thrown by | When |
|-----------|-----------|------|
| `UnauthorizedAccessException` | `loadDashboard`, `getKPISnapshot` | userId lacks permission |
| `ExportFormatUnsupportedException` | `exportReport` | format not in csv/pdf/xlsx |
| `DashboardWidgetRenderFailureException` | `loadDashboard` | one widget fails to render |

For `UnauthorizedAccessException`:
```java
ex.getUserId()          // who was rejected
ex.getAttemptedAction() // what they tried to do
ex.getErrorCode()       // "UNAUTHORIZED_ACCESS_ATTEMPT"
```

For `ExportFormatUnsupportedException`:
```java
ex.getRequestedFormat()  // what you passed
ex.getSupportedFormats() // List<String> of valid values
```

### A10. Valid user IDs (demo/stub environment)

| User ID | Role | Permissions |
|---------|------|-------------|
| `U001` | Admin | All |
| `U002` | HR Manager | VIEW_DASHBOARD, GENERATE_REPORT, VIEW_KPI |
| `U003` | Analyst | VIEW_DASHBOARD, VIEW_KPI |
| `U004` | Viewer | VIEW_DASHBOARD only |

---

## PART B — Attrition-Risk Sub-System

### B1. What you get

An instance of your own `IHRAnalyticsReportingService` interface, implemented by us.
You already have the interface — we implement it.

```
Package:   com.hrms.service
Interface: IHRAnalyticsReportingService  (you own this)
Impl:      com.hranalytics.integration.attrition.HRAnalyticsReportingServiceImpl  (we provide)
```

### B2. Declare the dependency

```java
public class AttritionRiskEngine {

    private final IHRAnalyticsReportingService hrAnalytics;

    // Injected at wiring time
    public AttritionRiskEngine(IHRAnalyticsReportingService hrAnalytics) {
        this.hrAnalytics = hrAnalytics;
    }
}
```

### B3. Pull data from us

```java
// Organisation-wide turnover rate (%)
double turnover = hrAnalytics.getOrganisationTurnoverRate();
// e.g. 7.69 → means 7.69%

// Active headcount per department
Map<String, Integer> headcount = hrAnalytics.getHeadcountByDepartment();
// e.g. {Engineering=7, HR=3, Finance=4}

// Engagement score for one department (0–100, or -1 if no data)
double engagement = hrAnalytics.getEngagementScore("Engineering");
// e.g. 80.6 → derived from avg performance score × 20

// Aggregated metrics over a date window
Map<String, Double> metrics = hrAnalytics.getAggregatedMetrics(
        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
// Keys guaranteed: "avgTenureYears", "avgSatisfactionScore", "absenteeismRate"
double tenure       = metrics.get("avgTenureYears");       // e.g. 4.50
double satisfaction = metrics.get("avgSatisfactionScore"); // e.g. 81.07
double absenteeism  = metrics.get("absenteeismRate");      // e.g. 4.20
```

### B4. Push a risk report to our dashboard

Implement `AttritionRiskReport` as an anonymous class or a concrete class:

```java
IHRAnalyticsReportingService.AttritionRiskReport report =
    new IHRAnalyticsReportingService.AttritionRiskReport() {
        public String getReportId()             { return "ARR-2025-Q1"; }
        public LocalDate getGeneratedOn()        { return LocalDate.now(); }
        public int getHighRiskCount()            { return 3; }
        public int getMediumRiskCount()          { return 8; }
        public int getLowRiskCount()             { return 24; }
        public List<String> getTopRootCauses()   { return List.of("attendance", "tenure", "compensation"); }
        public String getSummary()               { return "3 employees require immediate intervention."; }
    };

hrAnalytics.publishAttritionRiskReport(report);
// Report appears in the HR Analytics dashboard automatically
```

**AttritionRiskReport fields we read:**

| Method | Type | Description |
|--------|------|-------------|
| `getReportId()` | `String` | Unique report identifier |
| `getGeneratedOn()` | `LocalDate` | Date of generation |
| `getHighRiskCount()` | `int` | Employees at HIGH risk |
| `getMediumRiskCount()` | `int` | Employees at MEDIUM risk |
| `getLowRiskCount()` | `int` | Employees at LOW risk |
| `getTopRootCauses()` | `List<String>` | Root causes, impact-ordered |
| `getSummary()` | `String` | Human-readable caption |

### B5. Wiring at startup

```java
// Your team's startup / main class
HRAnalyticsReportingServiceImpl hrService =
        new HRAnalyticsReportingServiceImpl(facade, employeeService);

// Then inject into your engine
AttritionRiskEngine engine = new AttritionRiskEngine(hrService);
```

You receive the `HRAnalyticsReportingServiceImpl` instance from us at integration time.
Store it as `IHRAnalyticsReportingService` — never as the concrete type.

---

## PART C — Common Rules for All Integrators

1. **Never import** `HRAnalyticsFacade` or any class from `com.hranalytics.pipeline`,
   `com.hranalytics.metrics`, `com.hranalytics.dashboard`, `com.hranalytics.reports`,
   or `com.hranalytics.export`. Those are internal.

2. **Always inject** the service via constructor — do not call `new` on the implementation
   class inside your business logic.

3. **Exceptions are `RuntimeException`** — you are not required to declare them, but you
   should catch them at your boundary to avoid propagating them to your own callers.

4. **Thread safety** — the current implementation is single-threaded (demo context).
   Do not call methods from multiple threads concurrently without external synchronisation.

5. **Null safety** — `getEngagementScore()` returns `-1` (not `null`) when no data is
   available. `getKPISnapshot()` with a null `deptId` returns all departments.

6. **Stub vs production** — in the current demo build all data comes from in-memory stubs.
   The interfaces stay identical when the DB team wires real implementations.

---

## PART D — Data Field Reference

Fields returned by our interfaces and their types:

| Field | Type | Source |
|-------|------|--------|
| `dashboardId` | `String` | Generated (UUID prefix) |
| `widgetId` | `String` | Generated per chart |
| `attritionRate` | `double` | Computed — % employees left |
| `employeeGrowth` | `double` | Computed — % headcount change |
| `avgPerformance` | `double` | Computed — avg score 0–5 |
| `metricName` | `String` | e.g. "Attrition Rate" |
| `currentValue` | `double` | Current period value |
| `previousValue` | `double` | Previous period value |
| `trend` | `String` | "UP" / "DOWN" / "STABLE" |
| `unit` | `String` | "%" / "/5.0" / "$" |
| `reportId` | `String` | Generated (UUID prefix) |
| `reportTitle` | `String` | Human-readable |
| `sectionCount` | `int` | Number of report sections |
| `avgTenureYears` | `double` | Years from joinDate to window end |
| `avgSatisfactionScore` | `double` | Avg performance score × 20 |
| `absenteeismRate` | `double` | % absent days (stub: 4.2) |

---

## PART E — Quick Reference Card

```
┌─────────────────────────────────────────────────────────────────┐
│              HR ANALYTICS — INTEGRATION QUICK REFERENCE         │
├──────────────────────┬──────────────────────────────────────────┤
│ ESS Portal /         │ Interface : HRAnalyticsService           │
│ Manager Dashboard    │ Methods   : loadDashboard()              │
│                      │            generateReport()              │
│                      │            exportReport()                │
│                      │            getKPISnapshot()              │
├──────────────────────┼──────────────────────────────────────────┤
│ Attrition-Risk       │ Interface : IHRAnalyticsReportingService │
│                      │ Pull      : getOrganisationTurnoverRate()│
│                      │            getHeadcountByDepartment()    │
│                      │            getEngagementScore(dept)      │
│                      │            getAggregatedMetrics(from,to) │
│                      │ Push      : publishAttritionRiskReport() │
├──────────────────────┼──────────────────────────────────────────┤
│ Valid report types   │ ATTRITION, EMPLOYEE_GROWTH, COMPENSATION │
│                      │ DEPARTMENT_METRICS, PERFORMANCE,         │
│                      │ FULL_HR_SUMMARY                          │
├──────────────────────┼──────────────────────────────────────────┤
│ Valid export formats │ csv · pdf · xlsx                         │
├──────────────────────┼──────────────────────────────────────────┤
│ Demo user IDs        │ U001 Admin · U002 HR Mgr · U003 Analyst  │
└──────────────────────┴──────────────────────────────────────────┘
```

---

*For queries contact Prem M Thakur (PES1UG23AM214) — integration boundary owner.*
