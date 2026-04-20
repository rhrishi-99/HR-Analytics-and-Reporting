# HR Analytics & Reporting Sub-System

An HRMS sub-module that ingests raw HR data (Employee, Payroll, Attendance, Performance)
and transforms it into dashboards, KPI cards, charts, and exportable reports.

**Course:** Object Oriented Analysis & Design (OOAD)  
**Team:**

| Member | USN | Owns |
|--------|-----|------|
| Prem M Thakur | PES1UG23AM214 | Data pipeline, Access control, Facade, Integration boundaries |
| Raihan Naeem | PES1UG23AM227 | Metrics engine, Strategy implementations, Analytics engine |
| R G Rhrishi | PES1UG23AM222 | Charts, Dashboard, Reports, Exports, Filters, Web UI |

---

## Design Patterns

| Pattern | Category | Where |
|---------|----------|-------|
| **Facade** | Structural | `HRAnalyticsFacade` — single external entry point for all subsystems |
| **Abstract Factory** | Creational | `ChartFactory` → `EmployeeGrowthChartFactory`, `AttritionChartFactory`, `CompensationChartFactory` |
| **Strategy** | Behavioural | `MetricStrategy` → one class per metric, dispatched via `Map<MetricType, MetricStrategy>` |

---

## Architecture

```
External Subsystems
        │
        ▼
 HRAnalyticsFacade          ← single entry point (Facade pattern)
        │
        ├── AccessControlModule
        ├── DataCollectionModule      ← pulls from 4 service interfaces
        ├── DataIntegrationLayer      ← cleans & deduplicates
        ├── DataProcessingEngine      ← applies filters
        ├── MetricsCalculationEngine  ← Strategy pattern
        ├── AnalyticsEngine           ← generates insights
        ├── DashboardManager          ← Abstract Factory (charts)
        ├── ReportGenerator
        └── ExportSharingModule       ← csv / pdf / xlsx
```

---

## Integrations

| Subsystem | Direction | Interface |
|-----------|-----------|-----------|
| Performance Management | Inbound (we consume) | `PerformanceService` |
| ESS Portal / Manager Dashboard | Outbound (we publish) | `HRAnalyticsService` |
| Attrition-Risk | Bidirectional | `IHRAnalyticsReportingService` |
| Database Team | Inbound (we consume) | `EmployeeService`, `PayrollService`, `AttendanceService` |

---

## Running the Demo

> **Prerequisite:** JDK 17 or higher installed.

### Console Demo
```
.\build.bat
```
Runs 9 steps: dashboard load → report generation → exports → KPI snapshot →
chart factory swap → attrition integration → exception scenarios.

### Web UI Demo
```
.\build.bat web
```
Open **http://localhost:8080** in a browser. Press `Ctrl+C` to stop.

> Both commands compile automatically before running — no separate build step needed.

---

## Web UI Walkthrough

| Tab | What it shows |
|-----|--------------|
| **Dashboard** | KPI cards, attrition/growth/performance highlights, AI insights |
| **Reports** | Generate any of 6 report types, export as CSV / PDF / XLSX |
| **KPI Snapshot** | Per-department metric table with trend arrows |

---

## Project Structure

```
src/main/java/com/hranalytics/
├── Main.java                        Console demo entry point
├── access/                          AccessControlModule
├── analytics/                       AnalyticsEngine
├── charts/                          ChartFactory + 3 concrete factories
├── dashboard/                       DashboardManager, KPICard, Widget
├── domain/                          Employee, Payroll, Attendance, Performance, FilterCriteria
├── exceptions/                      10 canonical exception classes
├── export/                          ExportSharingModule, CSVExport, PDFExport, ExcelExport
├── facade/                          HRAnalyticsFacade
├── integration/
│   ├── attrition/                   HRAnalyticsReportingServiceImpl (Attrition-Risk integration)
│   ├── dto/                         DashboardSnapshot, KPISnapshot, ReportSummary
│   ├── external/                    PayrollRecord, AttendanceRecord, PerformanceRecord
│   ├── mapper/                      PayrollMapper, AttendanceMapper, PerformanceMapper
│   ├── service/                     HRAnalyticsService + 4 inbound service interfaces
│   └── stub/                        In-memory stubs for all 4 services
├── metrics/                         MetricsCalculationEngine + 5 Strategy classes
├── pipeline/                        RawHRData, ProcessedData, pipeline stage classes
├── reports/                         ReportGenerator, Report, ReportType
└── web/                             WebServer, ApiHandler, WebMain

src/main/resources/web/
└── index.html                       Web UI frontend

com/hrms/service/
└── IHRAnalyticsReportingService.java  Attrition-Risk team's interface (we implement)
```

---

## Exception Handling

| Exception | Category | Handling |
|-----------|----------|---------|
| `INVALID_HRMS_DATA_SOURCE` | MAJOR | Retry 3×, alert admin, serve cached data |
| `DATA_SCHEMA_VALIDATION_FAILURE` | MAJOR | Skip invalid records, log per-field |
| `UNAUTHORIZED_ACCESS_ATTEMPT` | MAJOR | Terminate request, lock after 5 failures |
| `METRIC_CALCULATION_OVERFLOW` | MINOR | Substitute 0.0, flag metric card |
| `REPORT_GENERATION_TIMEOUT` | MINOR | Cancel, notify, offer lighter report |
| `DATA_INTEGRATION_MERGE_CONFLICT` | MINOR | Last-write-wins, flag, daily admin report |
| `SCHEDULED_REPORT_DISPATCH_FAILURE` | MINOR | 3 retries at 5-min intervals |
| `EXPORT_FORMAT_UNSUPPORTED` | WARNING | Show supported formats |
| `DASHBOARD_WIDGET_RENDER_FAILURE` | WARNING | Show placeholder, render other widgets |
| `FILTER_CRITERIA_INVALID` | WARNING | Highlight fields, reset to defaults |

---

## Integration Documents

| Document | For |
|----------|-----|
| `HR_Analytics_Integration_Contract.md` | ESS Portal / Manager Dashboard team |
| `HR_Analytics_Database_Integration_Contract.md` | Database team |
| `HR_Analytics_Integration_Guide.md` | All external subsystems (full reference) |

---

## Valid Report Types

`ATTRITION` · `EMPLOYEE_GROWTH` · `COMPENSATION` · `DEPARTMENT_METRICS` · `PERFORMANCE` · `FULL_HR_SUMMARY`

## Valid Export Formats

`csv` · `pdf` · `xlsx`

## Demo User IDs

| ID | Role | Permissions |
|----|------|-------------|
| U001 | Admin | All |
| U002 | HR Manager | VIEW_DASHBOARD, GENERATE_REPORT, VIEW_KPI |
| U003 | Analyst | VIEW_DASHBOARD, VIEW_KPI |
| U004 | Viewer | VIEW_DASHBOARD only |
