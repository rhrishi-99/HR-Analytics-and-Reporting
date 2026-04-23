# HR Analytics & Reporting Sub-System
## Complete Project Overview

**Course:** Object Oriented Analysis & Design (OOAD)  
**Language:** Java 17  
**Build:** No external dependencies — JDK only

---

## Team

| Member | USN | Role | Pattern Owned |
|--------|-----|------|---------------|
| Prem M Thakur | PES1UG23AM214 | Data pipeline, Access control, Facade, Integration boundaries | Facade (Structural) |
| Raihan Naeem | PES1UG23AM227 | Metrics engine, Proxy implementations, Analytics engine | Proxy (Structural) |
| R G Rhrishi | PES1UG23AM222 | Charts, Dashboard, Reports, Exports, Filters, Web UI, MVC controllers | Abstract Factory (Creational) + MVC (Architectural) |

---

## 1. Problem Statement

The parent HRMS product (Feature #12) requires a sub-system that:
- Ingests raw HR data from four core modules — Employee, Payroll, Attendance, Performance
- Computes analytics metrics (attrition, growth, performance, compensation, department KPIs)
- Renders dashboards and KPI cards for HR managers and analysts
- Generates reports and exports them in multiple formats
- Publishes data to external sub-systems (ESS Portal, Manager Dashboard, Attrition-Risk)
- Enforces access control on all operations

---

## 2. System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                  HRMS CORE MODULES (External)                       │
│   Employee DB  │  Payroll DB  │  Attendance DB  │  Performance Mgmt │
└────────┬────────────┬───────────────┬─────────────────┬─────────────┘
         │            │               │                 │
         ▼            ▼               ▼                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│              HR ANALYTICS & REPORTING SUB-SYSTEM                    │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   HRAnalyticsFacade                          │   │
│  │              (Single External Entry Point)                   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│         │                                                           │
│         ├── AccessControlModule                                     │
│         ├── DataCollectionModule                                    │
│         ├── DataIntegrationLayer                                    │
│         ├── DataProcessingEngine                                    │
│         ├── MetricsCalculationEngine  ← Proxy Pattern                │
│         ├── AnalyticsEngine                                         │
│         ├── DashboardManager          ← Abstract Factory Pattern    │
│         ├── ReportGenerator                                         │
│         └── ExportSharingModule                                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│              EXTERNAL CONSUMERS                                     │
│   ESS Portal  │  Manager Dashboard  │  Attrition-Risk Sub-System   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Pipeline — Step by Step

Every request entering the system flows through this pipeline in order:

### Step 1 — Access Control
`AccessControlModule` checks that the requesting user has the required permission
(`VIEW_DASHBOARD`, `GENERATE_REPORT`, `VIEW_KPI`). If not, throws
`UnauthorizedAccessException` and the request is terminated immediately.
Account is locked after 5 consecutive failures.

### Step 2 — Data Collection (`DataCollectionModule`)
Calls the four service interfaces to pull raw data:
- `EmployeeService.getAllEmployees()` / `getActiveEmployees()`
- `PayrollService.getAllPayrollRecords()` / `getPayrollByDateRange()`
- `AttendanceService.getAttendanceByDateRange()`
- `PerformanceService.getPerformanceByCycle()`

Records returned as external types (`PayrollRecord`, `AttendanceRecord`,
`PerformanceRecord`) are mapped to internal domain objects via Mapper classes.
All data is bundled into a `RawHRData` object.

### Step 3 — Data Integration (`DataIntegrationLayer`)
Validates and deduplicates the raw data:
- Null/blank ID checks — invalid records are skipped and logged
- Duplicate detection by ID — last record wins (raises `DATA_INTEGRATION_MERGE_CONFLICT`)
- Produces a clean `RawHRData` object ready for processing

### Step 4 — Data Processing (`DataProcessingEngine`)
Applies the user's `FilterCriteria`:
- Filters employees by department, status, and date range
- Derives computed stats: total employees, active count, attrition count,
  average salary, average attendance hours
- Produces a `ProcessedData` object with all derived statistics

### Step 5 — Metrics Calculation (`MetricsCalculationEngine` — Proxy Pattern)
Delegates to one `MetricCalculator` per metric type via `Map<MetricType, MetricCalculator>`.
No switch statements, no if-else chains. Each real calculator is wrapped in a
`MetricCalculatorProxy` that transparently handles logging and overflow recovery — the
real calculators contain only domain computation logic.

| MetricType | Real Subject Class | Computes |
|------------|-------------------|---------|
| `ATTRITION_RATE` | `AttritionRateCalculator` | % employees who left |
| `EMPLOYEE_GROWTH` | `EmployeeGrowthCalculator` | % headcount change |
| `AVERAGE_PERFORMANCE` | `AveragePerformanceCalculator` | Avg score 0–5 |
| `DEPARTMENT_METRICS` | `DepartmentMetricsCalculator` | Per-dept KPI breakdown |
| `COMPENSATION_ANALYTICS` | `CompensationAnalyticsCalculator` | Salary distribution |

Each calculator returns a `MetricResult` with `currentValue`, `previousValue`, `trend`, `unit`, and a `flagged` boolean (set if `METRIC_CALCULATION_OVERFLOW` was caught by the proxy).

### Step 6 — Analytics Engine (`AnalyticsEngine`)
Reads the computed `MetricResult` map and generates human-readable insight
strings, e.g. *"Attrition rate is above the 5% threshold — HR intervention recommended."*
These appear as the Insights panel on the dashboard.

### Step 7 — Dashboard Manager (`DashboardManager` — Abstract Factory Pattern)
Builds the `Dashboard` object:
- Creates `KPICard` objects from each `MetricResult`
- Uses the injected `ChartFactory` to create chart widgets:
  - `createLineChart()` → trend over time
  - `createBarChart()` → department comparison
  - `createPieChart()` → composition breakdown
- Wraps each chart in a `Widget` (falls back to placeholder if render fails)

Three chart factory families, each producing a themed set of charts:

| Factory | Charts produced |
|---------|----------------|
| `EmployeeGrowthChartFactory` | Growth-focused line, bar, pie charts |
| `AttritionChartFactory` | Attrition-focused charts |
| `CompensationChartFactory` | Salary and compensation charts |

Swapping the factory changes the entire chart theme without touching `DashboardManager`.

### Step 8 — Report Generator (`ReportGenerator`)
Generates a `Report` object for the requested `ReportType`:
- `ATTRITION` · `EMPLOYEE_GROWTH` · `COMPENSATION`
- `DEPARTMENT_METRICS` · `PERFORMANCE` · `FULL_HR_SUMMARY`

Each report contains titled sections with data rows derived from the metrics.

### Step 9 — Export Sharing Module (`ExportSharingModule`)
Exports a previously generated report to file. Dispatches to the correct
`ExportFormat` subclass via a registry — no switch statements.

| Format | Class | Output |
|--------|-------|--------|
| `csv` | `CSVExport` | Comma-separated values |
| `pdf` | `PDFExport` | Formatted text file (.pdf) |
| `xlsx` | `ExcelExport` | Tab-separated file (.xlsx) |

Adding a new format = new `ExportFormat` subclass only. `ExportSharingModule` is never modified (OCP).

---

## 4. Design Patterns

### 4.1 Facade Pattern (Structural) — Prem M Thakur

**Problem:** External subsystems should not know about the internal pipeline.
Every time an internal module changes, external callers would break.

**Solution:** `HRAnalyticsFacade` is the single external entry point.
It orchestrates the entire pipeline internally and exposes only 4 methods
via the `HRAnalyticsService` interface.

```
External Caller
      │
      ▼
HRAnalyticsFacade          ← they see only this
      │
      ├── calls AccessControlModule
      ├── calls DataCollectionModule
      ├── calls DataIntegrationLayer
      ├── calls DataProcessingEngine
      ├── calls MetricsCalculationEngine
      ├── calls AnalyticsEngine
      ├── calls DashboardManager
      ├── calls ReportGenerator
      └── calls ExportSharingModule
```

**GRASP principles satisfied:**
- **Controller** — Facade is the system controller
- **Low Coupling** — external systems depend on one interface
- **Indirection** — Facade decouples callers from pipeline internals

---

### 4.2 Abstract Factory Pattern (Creational) — R G Rhrishi

**Problem:** The dashboard needs different chart styles for different analytics contexts
(growth vs attrition vs compensation). Hardcoding chart types into `DashboardManager`
makes it impossible to change themes without editing core code.

**Solution:** `ChartFactory` interface defines `createLineChart()`, `createBarChart()`,
`createPieChart()`. Each concrete factory produces a themed family of charts.
`DashboardManager` depends on `ChartFactory`, never on a concrete chart class.

```
ChartFactory (interface)
      ├── EmployeeGrowthChartFactory   → growth-themed Line, Bar, Pie
      ├── AttritionChartFactory        → attrition-themed Line, Bar, Pie
      └── CompensationChartFactory     → compensation-themed Line, Bar, Pie

DashboardManager
      └── depends on ChartFactory only
            (injected at construction time)
```

**GRASP principles satisfied:**
- **Creator** — factories own chart construction
- **Protected Variations** — factory shields DashboardManager from chart family changes
- **Polymorphism** — all factories are substitutable

---

### 4.3 Proxy Pattern (Structural) — Raihan Naeem

**Problem:** Every metric calculation requires the same secondary logic: logging the
progress and catching `METRIC_CALCULATION_OVERFLOW` to return a safe result.
Repeating this inside every calculator clutters the domain logic.

**Solution:** `MetricCalculatorProxy` acts as a wrapper. It implements the same
interface as the real calculators. When `calculate()` is called, the proxy:
1. Logs that the calculation is starting.
2. Calls the real calculator (the "Real Subject").
3. Returns the result if successful.
4. Catches any arithmetic failure and returns a safe, flagged default.

```
MetricCalculator (Subject Interface)
      ├── MetricCalculatorProxy (Proxy) → intercepts calls
      └── AttritionRateCalculator (Real Subject)
      └── ...
```

**Key Benefit:** Real calculators remain "pure" — they only contain the math formulas.
All the "plumbing" (logging/safety) is handled in one place by the Proxy.

**Key difference from Template Method:**
Template Method placed the shared behavior (overflow catch, logging) in an abstract base
class that every calculator extended. Proxy keeps the real calculators completely free of
cross-cutting concerns — the proxy intercepts the call externally, without inheritance.

Adding a new metric = one new `MetricCalculator` implementation + `registerCalculator()`.
The engine and proxy are never modified (OCP).

**GRASP principles satisfied:**
- **Protected Variations** — Proxy shields the engine from overflow-handling changes
- **Polymorphism** — all calculators and the proxy are substitutable via the interface
- **High Cohesion** — each real calculator has exactly one domain responsibility; the proxy has one cross-cutting responsibility

---

### 4.4 MVC Pattern (Architectural) — R G Rhrishi

**Problem:** The original `ApiHandler` was doing everything: parsing HTTP parameters,
applying business logic, calling the Facade, and serialising JSON. This violated SRP
and made the web layer impossible to test or extend independently.

**Solution:** Introduce a Controller layer between the HTTP router and the Facade.

```
Browser (View)
      │  HTTP request
      ▼
ApiHandler (Router + View)
      │  parsed params Map
      ├──▶ DashboardController.handle(params)  → DashboardSnapshot (Model)
      ├──▶ ReportController.handleGenerate(params) → ReportSummary (Model)
      ├──▶ ReportController.handleExport(params)   → String path (Model)
      └──▶ KPIController.handle(params)        → List<KPISnapshot> (Model)
                  │
                  ▼
          HRAnalyticsFacade (business logic — untouched)
```

| Layer | Class(es) | Responsibility |
|-------|-----------|---------------|
| **Model** | `DashboardSnapshot`, `ReportSummary`, `KPISnapshot` | Data returned from pipeline |
| **View** | `index.html` + JSON serialisers in `ApiHandler` | Renders model to browser |
| **Controller** | `DashboardController`, `ReportController`, `KPIController` | Validates params, calls Facade, returns Model |
| **Router** | `ApiHandler` | Maps URL → Controller; serialises result → JSON |

**GRASP principles satisfied:**
- **Controller** — each controller handles one category of user request
- **SRP** — `ApiHandler` only routes and serialises; controllers only orchestrate
- **Low Coupling** — View and Controller are independent; either can change without breaking the other

---

## 5. SOLID Principles

| Principle | How it is applied |
|-----------|------------------|
| **SRP** — Single Responsibility | Every class has one job: `DataCollectionModule` only collects, `MetricsCalculationEngine` only delegates, `ExportSharingModule` only exports |
| **OCP** — Open/Closed | New metric → new `MetricCalculator` implementation, no engine edit. New chart theme → new Factory, no DashboardManager edit. New export format → new ExportFormat subclass, no ExportSharingModule edit |
| **LSP** — Liskov Substitution | All `MetricCalculator` implementations are substitutable (real subjects and proxy share the same interface). All `ChartFactory` implementations are substitutable. All `ExportFormat` implementations are substitutable |
| **ISP** — Interface Segregation | Narrow interfaces: `ChartFactory` (3 methods), `HRAnalyticsService` (4 methods), `EmployeeService` (4 methods). `MetricCalculator` exposes one public method (`calculate`) |
| **DIP** — Dependency Inversion | `DashboardManager` depends on `ChartFactory` (interface). `MetricsCalculationEngine` depends on `MetricCalculator` (interface). `DataCollectionModule` depends on `EmployeeService`, `PayrollService`, etc. (abstractions). MVC Controllers depend on `HRAnalyticsFacade` not on pipeline internals |

---

## 6. GRASP Principles

| Principle | Where applied |
|-----------|--------------|
| **Information Expert** | Domain classes own their getters (`Employee`, `Payroll`, `Attendance`) |
| **Creator** | `DashboardManager` creates `Dashboard`. `ReportGenerator` creates `Report`. Factories create charts |
| **Controller** | `HRAnalyticsFacade` is the system controller — all requests flow through it |
| **Low Coupling** | External systems access only `HRAnalyticsFacade`. Pipeline stages talk only to adjacent stages |
| **High Cohesion** | Each engine/module has exactly one job |
| **Polymorphism** | `ChartFactory`, `MetricCalculator`, `ExportFormat` all exploit polymorphism |
| **Pure Fabrication** | `DataIntegrationLayer`, `DataProcessingEngine`, `AnalyticsEngine` — not domain concepts but necessary for cohesion |
| **Indirection** | `HRAnalyticsFacade` and `DataIntegrationLayer` act as indirection layers |
| **Protected Variations** | Proxy shields the engine from overflow-handling changes. Factory shields DashboardManager from chart family changes |

---

## 7. Exception Handling

All exceptions extend `HRAnalyticsException extends RuntimeException`,
with a `Category` enum (`MAJOR`, `MINOR`, `WARNING`).

| Exception | Category | Trigger | Handling |
|-----------|----------|---------|---------|
| `INVALID_HRMS_DATA_SOURCE` | MAJOR | External service unreachable | Retry 3× with backoff; alert admin; serve cached data |
| `DATA_SCHEMA_VALIDATION_FAILURE` | MAJOR | Null/invalid fields in incoming records | Skip invalid records; log per-field; end-of-batch summary |
| `UNAUTHORIZED_ACCESS_ATTEMPT` | MAJOR | User lacks required permission | Terminate request; log; lock account after 5 failures |
| `METRIC_CALCULATION_OVERFLOW` | MINOR | Arithmetic overflow in calculator | Substitute 0.0; flag metric card; log inputs |
| `REPORT_GENERATION_TIMEOUT` | MINOR | Report takes too long | Cancel; notify user; offer lighter report type |
| `DATA_INTEGRATION_MERGE_CONFLICT` | MINOR | Duplicate record IDs detected | Last-write-wins; flag record; daily admin report |
| `SCHEDULED_REPORT_DISPATCH_FAILURE` | MINOR | Scheduled export fails | 3 retries at 5-min intervals; store for manual re-dispatch |
| `EXPORT_FORMAT_UNSUPPORTED` | WARNING | Unknown format string passed | Show supported formats; user selects alternative |
| `DASHBOARD_WIDGET_RENDER_FAILURE` | WARNING | One widget fails to render | Show placeholder; other widgets continue rendering |
| `FILTER_CRITERIA_INVALID` | WARNING | Invalid date range or filter value | Highlight invalid fields; reset to defaults; block query |

**Ownership:**
- Prem: INVALID_HRMS_DATA_SOURCE, DATA_SCHEMA_VALIDATION_FAILURE, UNAUTHORIZED_ACCESS_ATTEMPT, DATA_INTEGRATION_MERGE_CONFLICT
- Raihan: METRIC_CALCULATION_OVERFLOW
- Rhrishi: REPORT_GENERATION_TIMEOUT, EXPORT_FORMAT_UNSUPPORTED, DASHBOARD_WIDGET_RENDER_FAILURE, FILTER_CRITERIA_INVALID, SCHEDULED_REPORT_DISPATCH_FAILURE

---

## 8. Integrations

### Integration 1 — Performance Management Sub-System (Inbound)
**Direction:** They → Us  
**What we do:** Pull performance records to feed into the analytics pipeline.  
**Interface we call:** `PerformanceService`  
**Wrapper:** `PerformanceManagementClient implements PerformanceService`

```java
// We call:
perfService.getPerformanceByCycle("Q1-2025");
perfService.getLatestReview(employeeId);
```

### Integration 2 — ESS Portal / Manager Dashboard (Outbound)
**Direction:** Us → Them  
**What we do:** Publish dashboards and KPI snapshots for external display.  
**Interface they call:** `HRAnalyticsService`

```java
// They call:
hrService.loadDashboard(userId, filters);      // → DashboardSnapshot
hrService.generateReport(reportType, filters); // → ReportSummary
hrService.exportReport(reportId, format);      // → file path
hrService.getKPISnapshot(userId, deptId);      // → List<KPISnapshot>
```

### Integration 3 — Attrition-Risk Sub-System (Bidirectional)
**Direction:** Both ways  
**Interface:** `IHRAnalyticsReportingService` (defined by them, implemented by us)

```java
// They pull from us:
hrAnalytics.getOrganisationTurnoverRate();
hrAnalytics.getHeadcountByDepartment();
hrAnalytics.getEngagementScore("Engineering");
hrAnalytics.getAggregatedMetrics(from, to);

// They push to us:
hrAnalytics.publishAttritionRiskReport(report);
```

### Integration 4 — Database Sub-System (Inbound)
**Direction:** They → Us  
**What we do:** Consume employee, payroll, and attendance data from their DB.  
**Interfaces they implement:**

```
EmployeeService   → getAll, getById, getByDepartment, getActive
PayrollService    → getAll, getByEmployee, getByDateRange
AttendanceService → getByEmployee, getByDateRange, getTotalHoursWorked
```

In development, all four services are backed by in-memory stubs (`EmployeeServiceStub`,
`PayrollServiceStub`, `AttendanceServiceStub`, `PerformanceServiceStub`).
Swapping to real implementations requires changing **one line per service** at the wiring site.

---

## 9. Web UI

Built using JDK's built-in `com.sun.net.httpserver.HttpServer` — no external
frameworks or libraries required. Serves a single-page dashboard at port 8080.
Follows the **MVC architectural pattern**.

**Architecture:**
```
Browser (View — index.html)
  │  HTTP request
  ▼
WebServer
  ├── GET /       → serves index.html (View)
  └── GET /api/*  → ApiHandler (Router + View serialiser)
                        ├── DashboardController  (Controller)
                        ├── ReportController     (Controller)
                        └── KPIController        (Controller)
                                  │
                                  ▼
                          HRAnalyticsFacade → pipeline → DTOs (Model)
```

**Three tabs:**

| Tab | API endpoint | Controller | Demonstrates |
|-----|-------------|-----------|-------------|
| Dashboard | `GET /api/dashboard` | `DashboardController` | Full pipeline run, KPI cards, insights, highlights |
| Reports | `GET /api/report` + `GET /api/export` | `ReportController` | Report generation and export with exception demo |
| KPI Snapshot | `GET /api/kpi` | `KPIController` | Per-department metrics table |

---

## 10. Class Summary (81 classes)

| Package | Classes | Purpose |
|---------|---------|---------|
| `access` | 1 | Permission checks and account lock |
| `analytics` | 1 | Insight string generation |
| `charts` | 7 | Abstract Factory — ChartFactory + 3 factories + 3 chart types |
| `dashboard` | 4 | Dashboard, DashboardManager, KPICard, Widget |
| `domain` | 8 | Employee, Payroll, Attendance, Performance, Department, User, DateRange, FilterCriteria |
| `exceptions` | 10 | HRAnalyticsException base + 9 canonical exceptions |
| `export` | 4 | ExportFormat, CSVExport, PDFExport, ExcelExport, ExportSharingModule |
| `facade` | 1 | HRAnalyticsFacade |
| `integration/attrition` | 1 | HRAnalyticsReportingServiceImpl |
| `integration/dto` | 3 | DashboardSnapshot, KPISnapshot, ReportSummary |
| `integration/external` | 3 | PayrollRecord, AttendanceRecord, PerformanceRecord |
| `integration/mapper` | 3 | PayrollMapper, AttendanceMapper, PerformanceMapper |
| `integration/service` | 5 | HRAnalyticsService + 4 inbound service interfaces |
| `integration/stub` | 4 | In-memory stubs for all 4 services |
| `metrics` | 10 | MetricCalculator (interface), MetricCalculatorProxy, MetricType, MetricResult, MetricsCalculationEngine + 5 real calculators |
| `pipeline` | 5 | RawHRData, ProcessedData, DataCollectionModule, DataIntegrationLayer, DataProcessingEngine |
| `reports` | 3 | Report, ReportGenerator, ReportType |
| `web` | 3 | WebServer, ApiHandler (Router+View), WebMain |
| `web/controller` | 3 | DashboardController, ReportController, KPIController (MVC) |
| `com.hrms.service` | 1 | IHRAnalyticsReportingService (Attrition team's interface) |

---

## 11. Running the System

### Console Demo (9 steps)
```
.\build.bat
```

**Steps demonstrated:**
1. Load dashboard — full pipeline run, KPI cards, insights
2. Generate Full HR Summary report
3. Export report as CSV, PDF, XLSX
4. Generate Attrition report + CSV export
5. KPI Snapshot for ESS Portal (Engineering dept)
6. Switch to AttritionChartFactory, filter by HR dept
7. Exception — EXPORT_FORMAT_UNSUPPORTED (pptx)
8. Exception — UNAUTHORIZED_ACCESS_ATTEMPT (U004)
9. Attrition-Risk integration — pull metrics, receive risk report

### Web UI Demo
```
.\build.bat web
```
Open **http://localhost:8080**

---

## 12. Key Design Decisions

**Why Facade?**
Without it, every external subsystem would import and instantiate pipeline internals.
Any refactor of an internal module would break all callers. The Facade freezes the
external API so internals can evolve freely.

**Why Abstract Factory for charts?**
The HR team needs different visual contexts — growth, attrition, compensation.
Each context needs a consistent family of chart types. Without the factory,
adding a new chart theme would require editing `DashboardManager`.

**Why Proxy for metrics?**
It separates our "math logic" from our "system logic". The real calculators only do math.
The `MetricCalculatorProxy` handles the tracing and safety. This keeps the code clean
and ensures that a single bad data point doesn't crash the entire dashboard.

**Why MVC for the web layer?**
Without MVC, `ApiHandler` was responsible for parsing, business logic, and serialisation —
three separate concerns in one class (SRP violation). MVC separates these: Controllers
handle business logic, `ApiHandler` handles routing and serialisation, `index.html` handles
display. Each layer can change independently.

**Why no external libraries?**
The course requires pure Java. The web UI uses `com.sun.net.httpserver` (built into
JDK since Java 6). Exports simulate file formats using structured text — the
interfaces are identical to what a real PDF/Excel library would expose.

**Why stubs instead of a real database?**
The DB team delivers their implementations separately. Our service interfaces allow
us to develop and demo the full pipeline independently. Swapping stubs to real
implementations is a single-line change at the wiring site — nothing else changes.
