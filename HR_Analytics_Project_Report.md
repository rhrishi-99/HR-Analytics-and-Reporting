# HR Analytics & Reporting Sub-System
## Project Report — Object Oriented Analysis & Design (OOAD)

**Course:** Object Oriented Analysis & Design  
**Language:** Java 17 (no external dependencies)

| Member | USN | Contribution |
|--------|-----|-------------|
| Prem M Thakur | PES1UG23AM214 | Data pipeline, Access control, Facade, Integration boundaries |
| Raihan Naeem | PES1UG23AM227 | Metrics engine, Proxy pattern, Analytics engine |
| R G Rhrishi | PES1UG23AM222 | Charts, Dashboard, Reports, Exports, Web UI, MVC controllers |

---

## 1. Problem Statement

Modern organizations generate vast amounts of HR data across multiple systems — employee records, payroll runs, attendance logs, and performance reviews. This data exists in isolated silos, making it difficult for HR managers and analysts to gain a consolidated view of workforce health.

**The challenge:** The parent HRMS product (Feature #12) needed a dedicated sub-system that could:

- **Aggregate** raw data from four independent HRMS modules — Employee, Payroll, Attendance, and Performance — without directly coupling to their internal implementations.
- **Compute** meaningful workforce KPIs such as attrition rate, employee growth, average performance score, department headcount distribution, and average compensation.
- **Present** this information as interactive dashboards and KPI cards that HR managers and analysts can act on immediately.
- **Distribute** computed insights to external consumers — the ESS Portal, Manager Dashboard, and Attrition-Risk Sub-System — without exposing internal implementation details.
- **Enforce** role-based access control so that viewers, analysts, managers, and admins each see only what they are permitted to see.
- **Export** reports in standard formats (CSV, PDF, XLSX) and support scheduled dispatch.

The core constraint was that the sub-system must integrate with four external teams (Database, Performance Management, ESS Portal, Attrition-Risk), each with their own delivery schedules, while remaining internally testable and evolvable at all times.

---

## 2. Key Features

### 2.1 Multi-Source Data Ingestion
The system collects raw data from four service interfaces — `EmployeeService`, `PayrollService`, `AttendanceService`, and `PerformanceService`. External records are mapped to internal domain objects via dedicated Mapper classes, isolating the system from upstream data format changes. In development, all four services are backed by in-memory stubs that can be swapped for real implementations with a single-line change.

### 2.2 Data Validation and Deduplication
Before any analytics are computed, the `DataIntegrationLayer` validates every record for null or blank IDs, skipping invalid entries and logging per-field errors. Duplicate records are resolved by last-write-wins with a conflict flag raised (`DATA_INTEGRATION_MERGE_CONFLICT`). This ensures the analytics pipeline always operates on clean data.

### 2.3 Flexible Filtering
Users can scope any request through `FilterCriteria`: filter by department list, employee status (ACTIVE / INACTIVE / ALL), and date range. Filters are applied at the `DataProcessingEngine` stage, producing a `ProcessedData` object with derived statistics (total headcount, attrition count, average salary, etc.) that all downstream components consume.

### 2.4 Five KPI Metrics

| Metric | What it measures |
|--------|-----------------|
| **Attrition Rate** | Separations as a percentage of average headcount |
| **Employee Growth** | Headcount change from the previous period |
| **Avg Performance Score** | Mean review score across all employees (0–5 scale) |
| **Dept Headcount Distribution** | Each department's share of total workforce |
| **Avg Compensation** | Mean gross salary with per-department breakdown |

Each metric is computed independently, returns trend data (UP / DOWN / STABLE), and degrades gracefully — if data is unavailable, a flagged result with value 0.0 is returned rather than crashing the dashboard.

### 2.5 AI-Style Insight Generation
The `AnalyticsEngine` reads the computed metric map and produces human-readable insight strings (e.g., *"Attrition rate is above the 5% threshold — HR intervention recommended."*). These appear as an Insights panel on the dashboard.

### 2.6 Themed Chart Dashboards
Dashboards are built by `DashboardManager` using three themed chart families: `EmployeeGrowthChartFactory`, `AttritionChartFactory`, and `CompensationChartFactory`. Each factory produces a matched set of line, bar, and pie charts. Switching the analytics context swaps the entire chart family without touching the dashboard manager.

### 2.7 Report Generation (6 Report Types)
`ReportGenerator` produces structured reports for six types: `ATTRITION`, `EMPLOYEE_GROWTH`, `COMPENSATION`, `DEPARTMENT_METRICS`, `PERFORMANCE`, and `FULL_HR_SUMMARY`. Each report contains titled sections with data rows derived from the metrics.

### 2.8 Multi-Format Export
Reports can be exported as:
- **CSV** — comma-separated values for spreadsheet tools
- **PDF** — formatted text output
- **XLSX** — tab-separated Excel-compatible format

New formats can be added by implementing the `ExportFormat` interface only — the dispatch module never changes.

### 2.9 Role-Based Access Control
`AccessControlModule` enforces three permission levels — `VIEW_DASHBOARD`, `GENERATE_REPORT`, and `VIEW_KPI` — before any pipeline stage executes. Unauthorized requests are terminated immediately and logged. Accounts are locked after five consecutive failures.

### 2.10 Four External Integrations

| Integration | Direction | Interface |
|-------------|-----------|-----------|
| Performance Management | Inbound (we consume) | `PerformanceService` |
| ESS Portal / Manager Dashboard | Outbound (we publish) | `HRAnalyticsService` |
| Attrition-Risk Sub-System | Bidirectional | `IHRAnalyticsReportingService` |
| Database Sub-System | Inbound (we consume) | `EmployeeService`, `PayrollService`, `AttendanceService` |

All external teams interact only with `HRAnalyticsFacade` — no internal engine or module is ever exposed.

### 2.11 Web UI
A browser-based dashboard served at `http://localhost:8080` using JDK's built-in HTTP server. Three tabs: Dashboard (KPI cards, charts, insights), Reports (generate + export), and KPI Snapshot (per-department metric table).

---

## 3. Models

### 3.1 Domain Models
Core business entities sourced from HRMS core modules. These are **read-only** within this sub-system.

| Class | Key Fields | Role |
|-------|-----------|------|
| `Employee` | `employeeId`, `name`, `department`, `designation`, `joinDate`, `status`, `baseSalary` | Central entity; all metrics trace back to employees |
| `Payroll` | `payrollId`, `employeeId`, `grossSalary`, `deductions`, `netSalary`, `paymentDate` | Source of compensation metrics |
| `Attendance` | `attendanceId`, `employeeId`, `hoursWorked`, `attendanceStatus` | Source of workforce presence data |
| `Performance` | `performanceId`, `employeeId`, `score`, `feedback`, `reviewDate` | Source of performance KPI |
| `Department` | `deptId`, `deptName`, `manager`, `headcount` | Grouping axis for all department-level breakdowns |
| `User` | `userId`, `username`, `userRole`, `permissions` | Subject of access control |
| `FilterCriteria` | `filterDepartment`, `filterStatus`, `dateRange`, `employeeCategory` | User's query scope |
| `DateRange` | `startDate`, `endDate` | Temporal window for any filter |

### 3.2 Pipeline Models
Intermediate data containers that flow through the analytics pipeline. These are **internal** — never exposed outside the sub-system.

| Class | What it holds |
|-------|--------------|
| `RawHRData` | Unvalidated bundle of all four domain collections as received from services |
| `ProcessedData` | Filtered, validated, and enriched data with derived statistics (headcount, separations, salary totals, etc.) ready for metric calculation |

### 3.3 Metric Models

| Class | Description |
|-------|-------------|
| `MetricType` | Enum: `ATTRITION_RATE`, `EMPLOYEE_GROWTH`, `AVERAGE_PERFORMANCE`, `DEPARTMENT_METRICS`, `COMPENSATION_ANALYTICS` |
| `MetricResult` | Immutable value object: `currentValue`, `previousValue`, `trend` (UP/DOWN/STABLE), `unit`, `breakdown` map, `overflowed` flag |

### 3.4 Dashboard Models

| Class | Description |
|-------|-------------|
| `Dashboard` | Top-level object: `dashboardId`, list of `KPICard`, list of `Widget`, list of insight strings |
| `KPICard` | One metric card: `metricName`, `currentValue`, `previousValue`, `trend`, `unit`, `flagged` |
| `Widget` | Wrapper around a `Chart`; degrades to a placeholder widget if rendering fails |
| `Chart` (abstract) | Base for `LineChart`, `BarChart`, `PieChart` — each holds title, data points, and color |

### 3.5 Report Models

| Class | Description |
|-------|-------------|
| `ReportType` | Enum of six report types |
| `Report` | `reportId`, `reportTitle`, `type`, `generatedDate`, list of titled sections with data rows |

### 3.6 Integration DTOs
Outbound data transfer objects published to external consumers. These are the only objects external teams receive.

| Class | Carries |
|-------|---------|
| `DashboardSnapshot` | `dashboardId`, `widgetIds`, list of `KPISnapshot`, `attritionRate`, `employeeGrowth`, `avgPerformance`, insights |
| `KPISnapshot` | `metricName`, `currentValue`, `previousValue`, `trend`, `unit`, `flagged` |
| `ReportSummary` | `reportId`, `reportTitle`, `reportType`, `generatedDate`, section count |

### 3.7 Exception Model
All exceptions extend `HRAnalyticsException extends RuntimeException` with a `Category` enum (`MAJOR`, `MINOR`, `WARNING`).

| Exception | Category | Trigger |
|-----------|----------|---------|
| `INVALID_HRMS_DATA_SOURCE` | MAJOR | External service unreachable |
| `DATA_SCHEMA_VALIDATION_FAILURE` | MAJOR | Null/invalid fields in incoming records |
| `UNAUTHORIZED_ACCESS_ATTEMPT` | MAJOR | User lacks required permission |
| `METRIC_CALCULATION_OVERFLOW` | MINOR | Arithmetic overflow in a calculator |
| `REPORT_GENERATION_TIMEOUT` | MINOR | Report takes too long |
| `DATA_INTEGRATION_MERGE_CONFLICT` | MINOR | Duplicate record IDs detected |
| `SCHEDULED_REPORT_DISPATCH_FAILURE` | MINOR | Scheduled export fails |
| `EXPORT_FORMAT_UNSUPPORTED` | WARNING | Unknown format string passed |
| `DASHBOARD_WIDGET_RENDER_FAILURE` | WARNING | One widget fails to render |
| `FILTER_CRITERIA_INVALID` | WARNING | Invalid date range or filter value |

---

## 4. Design Principles

### 4.1 SOLID Principles

#### Single Responsibility Principle (SRP)
Every class in the system has exactly one reason to change.

- `DataCollectionModule` only pulls raw data from external services.
- `DataIntegrationLayer` only validates and deduplicates records.
- `DataProcessingEngine` only applies filters and derives statistics.
- `MetricsCalculationEngine` only dispatches to registered calculators — it never computes a metric itself.
- `MetricCalculatorProxy` only handles cross-cutting concerns (logging, overflow recovery) — it contains zero domain logic.
- `ExportSharingModule` only dispatches to export format implementations.
- `ApiHandler` only routes HTTP requests and serialises responses to JSON — it contains zero business logic.

Violating SRP would mean a class has multiple reasons to change, making changes risky and test coverage incomplete.

#### Open/Closed Principle (OCP)
Classes are open for extension but closed for modification.

- **Adding a new metric:** Implement `MetricCalculator`, call `registerCalculator()`. `MetricsCalculationEngine` and `MetricCalculatorProxy` are never modified.
- **Adding a new chart theme:** Implement `ChartFactory` with three chart-creating methods. `DashboardManager` is never modified.
- **Adding a new export format:** Extend `ExportFormat`. `ExportSharingModule` is never modified.

This principle is the direct motivation for the Abstract Factory and Proxy patterns — each allows the system to grow without disturbing working code.

#### Liskov Substitution Principle (LSP)
All implementations of an interface are fully substitutable for each other.

- Any `MetricCalculator` implementation (real subject or proxy) can replace any other — the engine holds only `MetricCalculator` references.
- Any `ChartFactory` implementation (`EmployeeGrowthChartFactory`, `AttritionChartFactory`, `CompensationChartFactory`) can replace any other — `DashboardManager` never notices the difference.
- Any `ExportFormat` implementation can replace any other — `ExportSharingModule` calls only `export()`.

#### Interface Segregation Principle (ISP)
Interfaces are kept narrow so no implementation is forced to implement methods it does not use.

- `ChartFactory` — 3 methods only (`createLineChart`, `createBarChart`, `createPieChart`).
- `HRAnalyticsService` — 4 methods only (`loadDashboard`, `generateReport`, `exportReport`, `getKPISnapshot`).
- `MetricCalculator` — 4 methods only (`calculate`, `getMetricType`, `getMetricName`, `getUnit`).
- `EmployeeService` — 4 methods only.

Wide interfaces would force concrete classes to implement unneeded methods with empty stubs, a code smell.

#### Dependency Inversion Principle (DIP)
High-level modules depend on abstractions, never on concrete implementations.

- `DashboardManager` depends on `ChartFactory` (interface), not on any concrete factory class.
- `MetricsCalculationEngine` depends on `MetricCalculator` (interface), not on any specific calculator.
- `DataCollectionModule` depends on `EmployeeService`, `PayrollService`, `AttendanceService`, `PerformanceService` (all interfaces), not on stubs or real implementations.
- MVC Controllers depend on `HRAnalyticsFacade` (via the `HRAnalyticsService` interface), not on pipeline internals.

---

### 4.2 GRASP Principles

#### Information Expert
Responsibility is assigned to the class that holds the information needed to fulfil it.

- `Employee` owns `getDepartment()`, `getBaseSalary()` — it holds those fields.
- `MetricResult` owns `getTrend()` — it computes UP/DOWN/STABLE from its own `currentValue` and `previousValue`.
- `Dashboard` owns `getKpiCards()` and `getInsights()` — it aggregates those objects.

#### Creator
An object is created by the class that aggregates, contains, or closely uses it.

- `DashboardManager` creates `Dashboard`, `KPICard`, and `Widget` objects.
- `ReportGenerator` creates `Report` objects.
- Concrete `ChartFactory` implementations create `LineChart`, `BarChart`, and `PieChart` objects.

#### Controller
A dedicated controller class handles system events rather than scattering that logic across the UI.

- `HRAnalyticsFacade` is the system controller — all external events enter through it.
- `DashboardController`, `ReportController`, and `KPIController` are UI controllers — each handles one category of HTTP request and delegates to the Facade.

#### Low Coupling
Dependencies between classes are minimised.

- External sub-systems depend only on `HRAnalyticsFacade` (one dependency).
- Pipeline stages communicate only with adjacent stages via bundled data objects (`RawHRData`, `ProcessedData`), not by calling each other's internal methods.

#### High Cohesion
Each class does exactly one job, making it understandable and maintainable.

- `DataCollectionModule` — collect raw data.
- `DataIntegrationLayer` — validate and deduplicate.
- `DataProcessingEngine` — filter and derive statistics.
- `AnalyticsEngine` — generate insight strings.
- `ExportSharingModule` — dispatch to export format implementations.

#### Polymorphism
Type-specific behaviour is handled through polymorphism rather than if/else chains or switch statements.

- `MetricCalculator` implementations handle type-specific computation without any branching in the engine.
- `ChartFactory` implementations handle context-specific chart creation without any branching in `DashboardManager`.
- `ExportFormat` implementations handle format-specific serialisation without any branching in `ExportSharingModule`.

#### Pure Fabrication
Classes that don't represent domain concepts are invented to achieve Low Coupling and High Cohesion.

- `DataIntegrationLayer`, `DataProcessingEngine`, `AnalyticsEngine` — none of these are real-world HR concepts, but each is necessary to keep the pipeline cohesive and the Facade decoupled from raw computation logic.

#### Indirection
Intermediate objects are placed between collaborators to reduce direct coupling.

- `HRAnalyticsFacade` sits between all external callers and all internal pipeline modules.
- `DataIntegrationLayer` sits between raw collected data and the processing engine.

#### Protected Variations
Design so that variation in one component does not force changes in other components.

- The **Proxy** pattern shields `MetricsCalculationEngine` from changes in overflow-handling logic — adding a new recovery strategy requires only editing `MetricCalculatorProxy`.
- The **Abstract Factory** pattern shields `DashboardManager` from changes in chart families — adding a new visual theme requires only a new factory class.

---

## 5. Design Patterns

### 5.1 Facade Pattern (Structural)

**GoF intent:** Provide a unified interface to a set of interfaces in a subsystem. Facade defines a higher-level interface that makes the subsystem easier to use.

**Problem in this project:**  
The analytics pipeline has nine internal modules — `AccessControlModule`, `DataCollectionModule`, `DataIntegrationLayer`, `DataProcessingEngine`, `MetricsCalculationEngine`, `AnalyticsEngine`, `DashboardManager`, `ReportGenerator`, and `ExportSharingModule`. Without a Facade, every external subsystem (ESS Portal, Attrition-Risk, Manager Dashboard) would need to import, instantiate, and orchestrate all nine. Any internal refactoring would break all callers.

**How it is applied:**  
`HRAnalyticsFacade` is the single external entry point. It implements the `HRAnalyticsService` interface that external teams program to, and internally orchestrates the full nine-stage pipeline for every request. External teams never see — or import — any internal class.

```
External Caller (ESS Portal, Manager Dashboard, MVC Controllers)
      │
      ▼
HRAnalyticsFacade  implements HRAnalyticsService
      │
      ├── loadDashboard(userId, filters)        → DashboardSnapshot
      ├── generateReport(reportType, filters)   → ReportSummary
      ├── exportReport(reportId, format)        → file path
      └── getKPISnapshot(userId, deptId)        → List<KPISnapshot>
```

**Four public methods internally orchestrate all nine pipeline stages in sequence.**

**Why this was the right choice:**  
The Facade freezes the external API surface. Internals can be restructured, stages can be reordered, and new engines can be added without breaking any external integration. It also provides a natural enforcement point for access control — every external call is checked before the pipeline executes.

**GRASP principles satisfied:** Controller, Low Coupling, Indirection.

---

### 5.2 Abstract Factory Pattern (Creational)

**GoF intent:** Provide an interface for creating families of related or dependent objects without specifying their concrete classes.

**Problem in this project:**  
The dashboard needs different visual contexts. An attrition-focused view requires charts configured for attrition data. A compensation-focused view requires charts configured for salary data. Hardcoding chart construction in `DashboardManager` would mean editing core code every time a new analytics theme is needed.

**How it is applied:**  
`ChartFactory` is the factory interface with three product-creation methods:

```java
interface ChartFactory {
    LineChart createLineChart(MetricResult result);
    BarChart  createBarChart(MetricResult result);
    PieChart  createPieChart(MetricResult result);
}
```

Three concrete factories each produce a themed family of charts:

| Factory | Chart family |
|---------|-------------|
| `EmployeeGrowthChartFactory` | Growth-focused line, bar, pie charts |
| `AttritionChartFactory` | Attrition-focused line, bar, pie charts |
| `CompensationChartFactory` | Salary-focused line, bar, pie charts |

`DashboardManager` is constructed with a `ChartFactory` reference. It calls `factory.createLineChart()`, `factory.createBarChart()`, `factory.createPieChart()` — never knowing or caring which concrete factory it holds. Swapping the factory changes the entire chart theme without modifying `DashboardManager`.

**Why this was the right choice:**  
Adding a new chart theme (e.g., a performance-focused family) requires only one new class implementing `ChartFactory`. `DashboardManager` is never touched (OCP). The factory also guarantees that the three chart types within a family are always consistent — it is impossible to accidentally mix an attrition line chart with a compensation pie chart.

**GRASP principles satisfied:** Creator, Protected Variations, Polymorphism.

---

### 5.3 Proxy Pattern (Structural)

**GoF intent:** Provide a surrogate or placeholder for another object to control access to it, adding behaviour transparently without changing the real object.

**Problem in this project:**  
Every metric calculation requires two cross-cutting concerns: logging the call before and after execution, and catching `MetricCalculationOverflowException` to return a gracefully degraded flagged result instead of propagating the error. Placing this logic inside all five calculators would scatter the recovery code, violate SRP (each calculator would have two jobs), and mean that any change to overflow policy requires editing five classes.

**How it is applied:**  
`MetricCalculator` is an interface (the Subject):

```java
interface MetricCalculator {
    MetricResult calculate(ProcessedData data);
    MetricType getMetricType();
    String getMetricName();
    String getUnit();
}
```

Five Real Subject classes (`AttritionRateCalculator`, `EmployeeGrowthCalculator`, `AveragePerformanceCalculator`, `DepartmentMetricsCalculator`, `CompensationAnalyticsCalculator`) implement this interface and contain **only domain computation logic**. They throw `MetricCalculationOverflowException` freely on bad data — no try/catch.

`MetricCalculatorProxy` implements the same interface and wraps any real calculator:

```java
class MetricCalculatorProxy implements MetricCalculator {
    private final MetricCalculator real;

    public MetricResult calculate(ProcessedData data) {
        LOG.info("Proxy: delegating for " + real.getMetricType());
        try {
            MetricResult result = real.calculate(data);   // delegate
            LOG.info("Proxy: completed → " + result);
            return result;
        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("OVERFLOW [" + real.getMetricType() + "]: " + ex.getMessage());
            return new MetricResult(real.getMetricType(), real.getMetricName(), real.getUnit()); // flagged
        }
    }
}
```

`MetricsCalculationEngine` wraps every calculator in a proxy automatically on registration:

```java
public void registerCalculator(MetricCalculator calculator) {
    calculators.put(calculator.getMetricType(), new MetricCalculatorProxy(calculator));
}
```

The engine always holds `MetricCalculatorProxy` references — the real calculators are never directly accessible.

**Why this was the right choice:**  
The real calculators are kept pure — they express only the domain formula. Overflow recovery and logging live in exactly one class (`MetricCalculatorProxy`). Changing the overflow policy (e.g., substituting the previous period's value instead of 0.0) requires editing only the proxy. Adding a new metric calculator requires no changes to the proxy or the engine. The pattern is invisible to callers — they interact with `MetricCalculator` references and never know a proxy is involved.

**GRASP principles satisfied:** Protected Variations, Polymorphism, High Cohesion, SRP.

---

### 5.4 MVC Pattern (Architectural)

**GoF / Architectural intent:** Separate an application into three components — Model (data), View (presentation), Controller (request handling) — so each can evolve independently.

**Problem in this project:**  
The original `ApiHandler` was responsible for parsing HTTP query parameters, applying business logic, calling `HRAnalyticsFacade`, and serialising the result to JSON — four separate concerns in one class. This violated SRP and made the web layer untestable in isolation.

**How it is applied:**

| Layer | Classes | Responsibility |
|-------|---------|---------------|
| **Model** | `DashboardSnapshot`, `ReportSummary`, `KPISnapshot` | Data returned from the pipeline; plain Java objects with no behaviour |
| **View** | `index.html` + JSON serialisers in `ApiHandler` | Renders Model to the browser; contains zero business logic |
| **Controller** | `DashboardController`, `ReportController`, `KPIController` | Validates parsed parameters, calls `HRAnalyticsFacade`, returns the Model |
| **Router** | `ApiHandler` | Maps URL paths to controllers; serialises controller output to JSON |

```
Browser
  │  HTTP GET /api/dashboard?userId=U002
  ▼
ApiHandler (Router)
  │  parsed Map<String, String>
  ▼
DashboardController.handle(params)
  │  calls HRAnalyticsFacade.loadDashboard(userId, filters)
  ▼
DashboardSnapshot (Model)
  │  returned to ApiHandler
  ▼
ApiHandler serialises to JSON → HTTP 200
  │
  ▼
Browser renders dashboard
```

**Why this was the right choice:**  
Each layer has a single reason to change. Adding a new API endpoint means adding a new Controller — `ApiHandler` gains one route entry but is otherwise unchanged. Redesigning the frontend requires changing only `index.html`. Changing how `loadDashboard` works requires changing only `DashboardController` and the Facade — the router and the view are unaffected.

**GRASP principles satisfied:** Controller, SRP, Low Coupling.
