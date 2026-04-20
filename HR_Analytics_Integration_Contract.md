# HR Analytics & Reporting Sub-System — Integration Contract

**Version:** 1.0  
**Date:** 2026-04-16  
**Owner team:** R G Rhrishi (PES1UG23AM222), Prem M Thakur (PES1UG23AM214), Raihan Naeem (PES1UG23AM227)  
**For:** ESS Portal team / Manager Dashboard team

---

## How to Integrate

1. Copy the **four Java source files** listed in §3 into your project (or add them as a shared library JAR once we publish one).
2. Your code depends **only** on `HRAnalyticsService` — never on `HRAnalyticsFacade` or any internal class.
3. Obtain an instance of `HRAnalyticsService` via dependency injection or the object your team lead provides at wiring time.

---

## 1. The Interface — `HRAnalyticsService`

```
package com.hranalytics.integration.service;
```

| Method | Description |
|--------|-------------|
| `DashboardSnapshot loadDashboard(String userId, FilterCriteria filters)` | Full pipeline run — returns a rendered dashboard with KPI cards, chart widget IDs, and AI-generated insights. |
| `ReportSummary generateReport(String reportType, FilterCriteria filters)` | Generates a named report. Returns metadata including a `reportId` for later export. |
| `String exportReport(String reportId, String format)` | Downloads a previously generated report. Returns the file path. |
| `List<KPISnapshot> getKPISnapshot(String userId, String deptId)` | Lightweight KPI pull for a single department widget. |

---

## 2. Input Types

### `FilterCriteria`
```
package com.hranalytics.domain;
```

Construct one with `new FilterCriteria()` and set only the fields you care about. All fields are optional — defaults give you all data.

| Setter | Type | Default | Description |
|--------|------|---------|-------------|
| `setFilterDepartment(List<String>)` | `List<String>` | empty (all) | Department codes, e.g. `List.of("Engineering", "HR")` |
| `setDateRange(DateRange)` | `DateRange` | null (all time) | See DateRange below |
| `setEmployeeCategory(List<String>)` | `List<String>` | empty (all) | e.g. `"ACTIVE"`, `"ON_LEAVE"` |
| `setFilterStatus(String)` | `String` | `"ALL"` | `"ALL"`, `"ACTIVE"`, or `"INACTIVE"` |

**Example:**
```java
FilterCriteria f = new FilterCriteria();
f.setDateRange(new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)));
f.setFilterDepartment(List.of("Engineering"));
f.setFilterStatus("ACTIVE");
```

### `DateRange`
```
package com.hranalytics.domain;
```

```java
new DateRange(LocalDate startDate, LocalDate endDate)
```

Both dates are inclusive. `startDate` must not be after `endDate`.

---

## 3. Return Types (DTOs)

### `DashboardSnapshot`
```
package com.hranalytics.integration.dto;
```

Returned by `loadDashboard()`.

| Getter | Type | Description |
|--------|------|-------------|
| `getDashboardId()` | `String` | Unique ID for this dashboard render |
| `getWidgetIds()` | `List<String>` | IDs of chart widgets included in the dashboard |
| `getKpiCards()` | `List<KPISnapshot>` | One entry per computed metric |
| `getAttritionRate()` | `double` | Convenience shortcut — attrition % |
| `getEmployeeGrowth()` | `double` | Convenience shortcut — growth % |
| `getAvgPerformance()` | `double` | Convenience shortcut — avg score out of 5.0 |
| `getInsights()` | `List<String>` | Human-readable analytical summaries |
| `getGeneratedAt()` | `LocalDateTime` | When this snapshot was created |

---

### `KPISnapshot`
```
package com.hranalytics.integration.dto;
```

Returned inside `DashboardSnapshot.getKpiCards()` and as the list from `getKPISnapshot()`.

| Getter | Type | Description |
|--------|------|-------------|
| `getMetricName()` | `String` | e.g. `"Attrition Rate"`, `"Employee Growth"` |
| `getCurrentValue()` | `double` | Current computed value |
| `getPreviousValue()` | `double` | Value from the previous period (for trend arrows) |
| `getTrend()` | `String` | `"UP"`, `"DOWN"`, or `"STABLE"` |
| `getUnit()` | `String` | `"%"`, `"/5.0"`, `"$"`, etc. |
| `isFlagged()` | `boolean` | `true` if data was unavailable for this metric |

---

### `ReportSummary`
```
package com.hranalytics.integration.dto;
```

Returned by `generateReport()`.

| Getter | Type | Description |
|--------|------|-------------|
| `getReportId()` | `String` | Pass this to `exportReport()` |
| `getReportTitle()` | `String` | Human-readable title |
| `getReportType()` | `String` | Mirrors the `reportType` you passed in |
| `getGeneratedDate()` | `LocalDateTime` | Generation timestamp |
| `getSectionCount()` | `int` | Number of sections in the report |

---

## 4. Valid Enum / String Values

### `reportType` strings (for `generateReport`)

| Value | Generates |
|-------|-----------|
| `"ATTRITION"` | Attrition rate report |
| `"EMPLOYEE_GROWTH"` | Headcount growth report |
| `"COMPENSATION"` | Payroll and compensation analytics |
| `"DEPARTMENT_METRICS"` | Per-department KPI breakdown |
| `"PERFORMANCE"` | Performance scores and reviews |
| `"FULL_HR_SUMMARY"` | All of the above combined |

### `format` strings (for `exportReport`)

| Value | Output |
|-------|--------|
| `"csv"` | Comma-separated values file |
| `"pdf"` | Formatted PDF text file |
| `"xlsx"` | Tab-separated Excel-compatible file |

---

## 5. Exceptions You May Catch

All exceptions extend `com.hranalytics.exceptions.HRAnalyticsException` (which extends `RuntimeException`).

### `UnauthorizedAccessException`
```
package com.hranalytics.exceptions;
```

Thrown by `loadDashboard()` and `getKPISnapshot()` when the `userId` lacks the required permission.

| Method | Returns |
|--------|---------|
| `getErrorCode()` | `"UNAUTHORIZED_ACCESS_ATTEMPT"` |
| `getCategory()` | `MAJOR` |
| `getUserId()` | The user ID that was rejected |
| `getAttemptedAction()` | The action that was attempted (e.g. `"VIEW_DASHBOARD"`) |

**Example:**
```java
try {
    DashboardSnapshot snap = hrService.loadDashboard("U999", filters);
} catch (UnauthorizedAccessException ex) {
    System.err.println("Access denied: " + ex.getUserId() + " → " + ex.getAttemptedAction());
}
```

---

### `ExportFormatUnsupportedException`
```
package com.hranalytics.exceptions;
```

Thrown by `exportReport()` when the `format` string is not one of the supported values.

| Method | Returns |
|--------|---------|
| `getErrorCode()` | `"EXPORT_FORMAT_UNSUPPORTED"` |
| `getCategory()` | `WARNING` |
| `getRequestedFormat()` | The format string you passed |
| `getSupportedFormats()` | `List<String>` of valid formats |

**Example:**
```java
try {
    String path = hrService.exportReport(reportId, "pptx");
} catch (ExportFormatUnsupportedException ex) {
    System.err.println("Try one of: " + ex.getSupportedFormats());
}
```

---

### `DashboardWidgetRenderFailureException`
```
package com.hranalytics.exceptions;
```

May be thrown (or surface as a flagged KPI card) when an individual widget cannot render. The dashboard still returns partial results — only the affected widget is replaced with a placeholder.

| Method | Returns |
|--------|---------|
| `getErrorCode()` | `"DASHBOARD_WIDGET_RENDER_FAILURE"` |
| `getCategory()` | `WARNING` |
| `getWidgetId()` | ID of the widget that failed |
| `getMessage()` | Short description of the failure |

---

## 6. Full Usage Example

```java
import com.hranalytics.integration.service.HRAnalyticsService;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.domain.DateRange;
import com.hranalytics.exceptions.UnauthorizedAccessException;
import com.hranalytics.exceptions.ExportFormatUnsupportedException;

import java.time.LocalDate;
import java.util.List;

public class ESSPortalIntegrationExample {

    // Injected at startup — never instantiate HRAnalyticsFacade directly
    private final HRAnalyticsService hrService;

    public ESSPortalIntegrationExample(HRAnalyticsService hrService) {
        this.hrService = hrService;
    }

    public void renderEmployeeDashboard(String loggedInUserId) {
        // --- build filter ---
        FilterCriteria filters = new FilterCriteria();
        filters.setDateRange(new DateRange(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 31)));
        filters.setFilterStatus("ACTIVE");

        // --- load dashboard ---
        try {
            DashboardSnapshot snap = hrService.loadDashboard(loggedInUserId, filters);

            System.out.println("Dashboard: " + snap.getDashboardId());
            System.out.printf("Attrition: %.2f%%  Growth: %.2f%%%n",
                    snap.getAttritionRate(), snap.getEmployeeGrowth());

            for (KPISnapshot kpi : snap.getKpiCards()) {
                System.out.printf("  %s: %.2f%s [%s]%n",
                        kpi.getMetricName(), kpi.getCurrentValue(),
                        kpi.getUnit(), kpi.getTrend());
            }
        } catch (UnauthorizedAccessException ex) {
            showAccessDenied(ex.getUserId());
        }
    }

    public void downloadFullReport(String loggedInUserId) {
        // --- generate ---
        ReportSummary summary = hrService.generateReport(
                "FULL_HR_SUMMARY", new FilterCriteria());
        System.out.println("Report ready: " + summary.getReportId());

        // --- export ---
        try {
            String path = hrService.exportReport(summary.getReportId(), "pdf");
            System.out.println("File at: " + path);
        } catch (ExportFormatUnsupportedException ex) {
            System.out.println("Supported formats: " + ex.getSupportedFormats());
        }
    }

    public void renderKPIWidget(String loggedInUserId, String department) {
        List<KPISnapshot> kpis = hrService.getKPISnapshot(loggedInUserId, department);
        kpis.forEach(k -> System.out.println("  " + k));
    }

    private void showAccessDenied(String userId) {
        System.err.println("User " + userId + " is not authorised.");
    }
}
```

---

## 7. Java Source Files to Copy

Copy these files verbatim into your project, preserving their package paths:

```
src/main/java/com/hranalytics/integration/service/HRAnalyticsService.java
src/main/java/com/hranalytics/integration/dto/DashboardSnapshot.java
src/main/java/com/hranalytics/integration/dto/KPISnapshot.java
src/main/java/com/hranalytics/integration/dto/ReportSummary.java
src/main/java/com/hranalytics/domain/FilterCriteria.java
src/main/java/com/hranalytics/domain/DateRange.java
src/main/java/com/hranalytics/exceptions/HRAnalyticsException.java
src/main/java/com/hranalytics/exceptions/UnauthorizedAccessException.java
src/main/java/com/hranalytics/exceptions/ExportFormatUnsupportedException.java
src/main/java/com/hranalytics/exceptions/DashboardWidgetRenderFailureException.java
```

At runtime, inject an instance of `HRAnalyticsService` — the HR Analytics team will provide the concrete `HRAnalyticsFacade` wired to the live services.

---

## 8. Contact

For questions, raise them with **Prem M Thakur** (integration boundary owner) or **R G Rhrishi** (report/export/dashboard owner).
