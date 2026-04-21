# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project

**HR Analytics & Reporting Sub-System** — a minimal but functional HRMS sub-module that ingests raw HR data (Employee, Payroll, Attendance, Performance) and transforms it into dashboards, KPIs, charts, and exportable reports.

**Team & ownership (single-owner rule — no duplicate code across owners):**

| Member | USN | Owns | Pattern |
|---|---|---|---|
| Prem M Thakur | PES1UG23AM214 | Data pipeline, access control, Facade, integration boundaries | Facade (Structural) |
| Raihan Naeem | PES1UG23AM227 | Metrics engine, all MetricCalculator subclasses, AnalyticsEngine | Template Method (Behavioural) |
| R G Rhrishi | PES1UG23AM222 | Charts, dashboards, reports, exports, filters, Web UI, MVC controllers | Abstract Factory (Creational) + MVC (Architectural) |

When asked to modify code, respect these boundaries. If a change crosses boundaries, flag it explicitly rather than silently editing another owner's module.

---

## Architecture

Data flows one way through the pipeline:

```
HRMS Core Modules (Employee / Payroll / Attendance / Performance)
    → DataCollectionModule
    → DataIntegrationLayer
    → DataProcessingEngine
    → MetricsCalculationEngine   (Template Method pattern lives here)
    → AnalyticsEngine
    → DashboardManager  +  ReportGenerator
    → ExportSharingModule

AccessControlModule governs every layer.
HRAnalyticsFacade is the single external entry point.

Web layer (MVC):
    ApiHandler (Router + View)
        → DashboardController → HRAnalyticsFacade
        → ReportController    → HRAnalyticsFacade
        → KPIController       → HRAnalyticsFacade
```

**Rule:** External subsystems never call pipeline internals. They call `HRAnalyticsFacade` only. Do not expose internal engines, modules, or managers outside the package.

---

## Mandated Design Patterns

Four patterns are implemented; three are course-mandated (one per category), plus MVC for the web layer.

### Creational — Abstract Factory (Chart Factory)
- Interface `ChartFactory` with `createLineChart()`, `createBarChart()`, `createPieChart()`.
- Concrete factories group charts by analytics context: `EmployeeGrowthChartFactory`, `AttritionChartFactory`, `CompensationChartFactory`.
- `DashboardManager` consumes a `ChartFactory`, never a concrete `Chart`.
- To add a new chart theme: new concrete factory only. Never modify `DashboardManager`.
- Owner: R G Rhrishi. Files: `src/main/java/com/hranalytics/charts/`.

### Structural — Facade (HRAnalyticsFacade)
- Public methods: `loadDashboard(userId, filters)`, `generateReport(reportType, filters)`, `exportReport(reportId, format)`, `getKPISnapshot(userId, deptId)`.
- Internally orchestrates the full pipeline.
- This is also the integration contract — see Integration section.
- No new external API may be added without routing through the Facade.
- Owner: Prem M Thakur. File: `src/main/java/com/hranalytics/facade/HRAnalyticsFacade.java`.

### Behavioural — Template Method (MetricCalculator)
- Abstract base class `MetricCalculator` with a `final calculate(ProcessedData)` method that defines the skeleton.
- Abstract hook methods subclasses must implement: `computeCurrentValue()`, `computePreviousValue()`, `getMetricType()`, `getMetricName()`, `getUnit()`.
- Optional override hook: `computeBreakdown()` — defaults to `Map.of()`.
- Five concrete calculators: `AttritionRateCalculator`, `EmployeeGrowthCalculator`, `AveragePerformanceCalculator`, `DepartmentMetricsCalculator`, `CompensationAnalyticsCalculator`.
- `MetricsCalculationEngine` holds `Map<MetricType, MetricCalculator>` and delegates via `registerCalculator()`.
- To add a metric: new `MetricCalculator` subclass + register in engine. Never edit `MetricsCalculationEngine` logic.
- Owner: Raihan Naeem. Files: `src/main/java/com/hranalytics/metrics/`.

### Architectural — MVC (Web Layer)
- **Model:** DTOs — `DashboardSnapshot`, `KPISnapshot`, `ReportSummary` (from `com.hranalytics.integration.dto`).
- **View/Router:** `ApiHandler` — parses HTTP, routes to controller, serialises DTO → JSON, writes HTTP response. Contains zero business logic.
- **Controllers:** `DashboardController`, `ReportController`, `KPIController` — receive parsed params, delegate to `HRAnalyticsFacade`, return the Model.
- Owner: R G Rhrishi. Files: `src/main/java/com/hranalytics/web/`.

---

## GRASP & SOLID — Principles to Preserve

The design was evaluated against these; do not break them when editing.

**GRASP:** Information Expert (domain classes own their getters), Creator (factories/generators own construction), Controller (Facade is the system controller; MVC controllers own HTTP orchestration), Low Coupling (external access via Facade only), High Cohesion (one job per engine), Polymorphism (Chart, ExportFormat, MetricCalculator), Pure Fabrication (engines aren't domain concepts), Indirection (Facade, DataIntegrationLayer), Protected Variations (Template Method shields from metric-step change, Factory from chart-family change).

**SOLID:** SRP (one class, one reason to change), OCP (new metric/chart/export = new class, no edits), LSP (all calculator/chart/export subtypes are substitutable), ISP (narrow interfaces — ChartFactory, ExportFormat), DIP (engines depend on abstractions, never concrete classes).

When reviewing a diff, check that OCP still holds — if a feature required editing an engine or the Facade, reconsider the approach.

---

## Data Contract

~50 fields across modules. Canonical list is in `docs/data-details.pdf`. Summary:

- **Read-only from HRMS core** (pipeline consumes): `employeeId`, `name`, `department`, `designation`, `joinDate`, `baseSalary`, `performanceScore`, `status`, `payrollId`, `grossSalary`, `deductions`, `netSalary`, `paymentDate`, `attendanceId`, `hoursWorked`, `attendanceStatus`, `performanceId`, `score`, `feedback`, `reviewDate`, `deptId`, `deptName`, `manager`, `headcount`, `userId`, `username`, `userRole`, `permissions`.
- **Read/Write within sub-system** (generated/managed): `filterDepartment`, `dateRange`, `employeeCategory`, `filterStatus`, `startDate`, `endDate`, `dashboardId`, `widgetId`, `chartId`, `chartType`, `dataPoints`, `reportId`, `reportTitle`, `reportSections`, `exportFormats`, `sharingChannels`.
- **Computed / derived** (write-only from pipeline): `attritionRate`, `employeeGrowth`, `avgPerformance`, `deptMetrics`, `compensationData`, `generatedDate`.

Field types must match the data-details document exactly. Do not silently change a `Float` to `Double` or a `String` to an enum without updating that doc.

---

## Exception Handling

Canonical exceptions (defined in course exception table). Each has a category and a handling plan — do not invent new ones silently.

| Exception | Category | Handling |
|---|---|---|
| `INVALID_HRMS_DATA_SOURCE` | MAJOR | Retry 3× with backoff; alert admin; serve cached data |
| `DATA_SCHEMA_VALIDATION_FAILURE` | MAJOR | Skip invalid records; log per-field; end-of-batch summary |
| `UNAUTHORIZED_ACCESS_ATTEMPT` | MAJOR | Terminate request; log; lock account after 5 failures |
| `METRIC_CALCULATION_OVERFLOW` | MINOR | Substitute 0.0; flag metric card; log inputs |
| `REPORT_GENERATION_TIMEOUT` | MINOR | Cancel; notify; offer lighter report |
| `DATA_INTEGRATION_MERGE_CONFLICT` | MINOR | Last-write-wins; flag; daily admin report |
| `SCHEDULED_REPORT_DISPATCH_FAILURE` | MINOR | 3 retries at 5-min intervals; store for manual re-dispatch |
| `EXPORT_FORMAT_UNSUPPORTED` | WARNING | Show supported formats; user selects alternative |
| `DASHBOARD_WIDGET_RENDER_FAILURE` | WARNING | Show 'Data Unavailable' placeholder; render other widgets |
| `FILTER_CRITERIA_INVALID` | WARNING | Highlight fields; reset to defaults; block query |

**Ownership of exception handling:**
- Prem: `INVALID_HRMS_DATA_SOURCE`, `DATA_SCHEMA_VALIDATION_FAILURE`, `UNAUTHORIZED_ACCESS_ATTEMPT`, `DATA_INTEGRATION_MERGE_CONFLICT`
- Raihan: `METRIC_CALCULATION_OVERFLOW`
- Rhrishi: `REPORT_GENERATION_TIMEOUT`, `EXPORT_FORMAT_UNSUPPORTED`, `DASHBOARD_WIDGET_RENDER_FAILURE`, `FILTER_CRITERIA_INVALID`, `SCHEDULED_REPORT_DISPATCH_FAILURE`

---

## Integration (required for review)

The sub-system integrates with four external subsystems. At least two are course-required.

### Integration 1 — Performance Management Sub-System (we consume)
- **Direction:** inbound data pull.
- **Interface:** `PerformanceService` (stub: `PerformanceServiceStub`).
- **Calls made:** `getPerformanceRecords(employeeIds, dateRange)`, `getLatestReview(employeeId)`.
- **Fields:** `performanceId`, `employeeId`, `score`, `reviewDate`, `feedback`, `reviewer`.
- **Failure:** raises `INVALID_HRMS_DATA_SOURCE` or `DATA_SCHEMA_VALIDATION_FAILURE`.

### Integration 2 — ESS Portal / Manager Dashboard (we publish)
- **Direction:** outbound API.
- **Interface:** `HRAnalyticsService` (we implement).
- **Exposed via:** `HRAnalyticsFacade.loadDashboard(userId, filters)`, `HRAnalyticsFacade.getKPISnapshot(userId, deptId)`.
- **Fields:** `dashboardId`, `widgetId`, `kpiCards` (with `metricName`, `currentValue`, `previousValue`, `trend`), `attritionRate`, `employeeGrowth`, `avgPerformance`.
- **Failure:** raises `UNAUTHORIZED_ACCESS_ATTEMPT` or returns partial result with `DASHBOARD_WIDGET_RENDER_FAILURE` placeholders.

### Integration 3 — Attrition-Risk Sub-System (bidirectional)
- **Direction:** we publish data to them; they push `AttritionRiskReport` back to us.
- **Interface:** `IHRAnalyticsReportingService` (their interface; we implement it).
- **Implementation:** `HRAnalyticsReportingServiceImpl` in `com.hranalytics.integration.attrition`.
- **Methods we implement:** `getOrganisationTurnoverRate()`, `getHeadcountByDepartment()`, `getEngagementScore(dept)`, `publishAttritionRiskReport(report)`, `getAggregatedMetrics(from, to)`.

### Integration 4 — Database Sub-System (we consume)
- **Direction:** inbound data pull.
- **Interfaces:** `EmployeeService`, `PayrollService`, `AttendanceService` (stubs in `integration/stub/`).
- **Contract doc:** `HR_Analytics_Database_Integration_Contract.md`.

All partners touch only the Facade. Do not let integration code reach into pipeline internals.

---

## Build & Run

Implementation language: **Java 17**. No external dependencies — uses only JDK standard library.

```bat
# Console demo (9 steps)
.\build.bat

# Web UI demo — opens http://localhost:8080
.\build.bat web
```

Build script (`build.bat`) compiles all `.java` files under `src/` into `target/classes/`, copies web resources via `xcopy`, then runs the appropriate main class.

**Demo user IDs:** U001 (Admin), U002 (HR Manager), U003 (Analyst), U004 (Viewer).

---

## Coding Conventions

- **Comments:** every public class needs a short header comment stating its role and which pattern it participates in (if any). Every non-trivial method needs a one-line purpose comment. The course requires code to be understandable to external readers.
- **Naming:** `MetricCalculator` subclasses end in `Calculator`. Factory classes end in `Factory`. MVC controllers end in `Controller`. Exception classes match canonical names.
- **No duplicate code across owners.** If Raihan needs a utility Prem already wrote, import it — do not re-implement.
- **No switch/if-else on type.** Type-based branching is a signal that Template Method or Factory should be used instead.
- **No direct access to internal modules from outside the package.** Everything external goes through `HRAnalyticsFacade`.
- **Dependency direction:** always toward abstractions. Engines hold abstract references, never concrete class references.

---

## What Claude Should Do

- When adding a new metric: create a new `MetricCalculator` subclass, register it in `MetricsCalculationEngine` via `registerCalculator()`. Never edit the engine's `calculateAll()` logic.
- When adding a new chart type: extend `Chart`, add method to `ChartFactory`, implement in each concrete factory. Never edit `DashboardManager`.
- When adding a new export format: extend `ExportFormat`. Never edit `ExportSharingModule` dispatch logic beyond registration.
- When asked about "integration": default to routing through `HRAnalyticsFacade`.
- When asked who owns a file: check the ownership table above before suggesting edits.
- When writing new code: add the header comment and method comments described in Coding Conventions.

## What Claude Should Not Do

- Do not add new public entry points outside `HRAnalyticsFacade`.
- Do not introduce new design patterns in place of the four implemented ones without explicit team approval.
- Do not rename classes away from the class-diagram names.
- Do not invent new exception types; use the canonical table.
- Do not cross ownership boundaries silently — if a task requires it, call it out.
- Do not change field types or names from the data-details document without flagging it.

---

## Reference Files (in `/docs` or project root)

- `classdiagram.png` — authoritative class structure
- `HR_Analytics_Reporting_SubSystem_1_1.pdf` — architecture + UI wireframe
- `Exception_Table.pdf` — canonical exceptions
- `Data_details.pdf` — canonical field list and types
- `HRMS_Product_2.docx` — parent HRMS product spec (HR Analytics is feature #12)
- `HR_Analytics_Integration_Guide.md` — full integration reference for all external teams
- `HR_Analytics_Integration_Contract.md` — contract for ESS Portal / Manager Dashboard team
- `HR_Analytics_Database_Integration_Contract.md` — contract for Database team
- `PROJECT_OVERVIEW.md` — full architecture and design narrative (use for presentations)

> **Note on PDF files:** The `.pdf` files in this directory are ZIP archives in disguise. Standard PDF tools (`pdftotext`, `pdftoppm`) will fail. To read them:
> ```bash
> cp SomeFile.pdf /tmp/file.zip && unzip -o /tmp/file.zip -d /tmp/file_ex
> # Content in /tmp/file_ex/1.txt, 2.txt, etc.; images as .jpeg
> ```
