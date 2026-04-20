# HR Analytics & Reporting Sub-System — Full Knowledge Export
> Paste this entire document at the start of a new Claude chat to restore full project context.

---

## 1. Project Overview

**Name:** HR Analytics & Reporting Sub-System  
**Team:**
- Prem M Thakur — PES1UG23AM214
- Raihan Naeem — PES1UG23AM227
- R G Rhrishi — PES1UG23AM222

**Objective:** Design and develop a minimal yet functional HR Analytics and Reporting module. It processes raw HRMS data and transforms it into meaningful insights through dashboards, charts, tables, and reports. Enables data-driven decision-making via visual analytics (employee growth trends, attrition rates, workforce metrics).

---

## 2. System Architecture

### 2a. Backend Architecture (Data Pipeline)

Raw data flows through the following pipeline:

```
HRMS Core Modules
  ├── Employee
  ├── Payroll
  ├── Attendance
  └── Performance
        ↓
Data Collection Module
        ↓
Data Integration Layer
        ↓
Data Processing Engine
        ↓
Metrics Calculation Engine
        ↓
Analytics Engine  ──────────────────────────┐
        ↓                                   │
Dashboard Manager                   Report Generator
        ↓                                   ↓
                Export & Sharing Module
                        ↑
         Access Control & Security Module (governs all layers)
```

### 2b. Frontend UI Wireframe (Dashboard)

Layout structure:
1. **Header** — Navigation, branding
2. **Filters** — Department selector, Date range picker, Employee category filter
3. **KPI Cards** — Total Headcount, Attrition Rate, Average Performance Score
4. **Charts Section** — Employee Growth chart, Attrition Analysis chart
5. **Action Bar** — Generate Report button, Export Analytics button

---

## 3. Class Diagram Summary

The class diagram (file: `HR_Analytics__ReportingClass.pdf`) covers the following module hierarchy:

### Core Domain Classes

| Class | Key Attributes | Key Methods |
|---|---|---|
| `Employee` | employeeId, name, dept, role, joinDate, status | getDetails(), getDept() |
| `Department` | deptId, name, managerId, headcount | getEmployees(), getMetrics() |
| `Payroll` | payrollId, employeeId, salary, bonus, deductions, period | calculate(), getSummary() |
| `Attendance` | attendanceId, employeeId, date, status, hoursWorked | getMonthlySummary(), getRate() |
| `Performance` | performanceId, employeeId, reviewPeriod, score, feedback | getScore(), getHistory() |

### Analytics & Metrics Layer

| Class | Key Attributes | Key Methods |
|---|---|---|
| `MetricsEngine` | — | calculateAttrition(), calcHeadcount(), calcAvgPerformance(), calcPayrollStats() |
| `AnalyticsEngine` | — | generateInsights(), getTrends(), getAttritionAnalysis() |
| `DashboardManager` | dashboardId, filters, kpiCards, charts | loadDashboard(), applyFilters(), refreshKPIs() |

### Chart Hierarchy (Abstract Factory Candidate)

```
Chart (abstract)
  ├── LineChart   — title, xData, yData, color
  ├── BarChart    — title, categories, values, color
  └── PieChart    — title, labels, values
```
All have: `render()`, `update()`, `exportAsImage()`

### Report & Export Classes

| Class | Key Attributes | Key Methods |
|---|---|---|
| `Report` | reportId, type, generatedAt, filters, data | generate(), preview() |
| `ExportFormat` (abstract) | — | export(), getFileType() |
| `CSVExport` | — | export() → .csv |
| `PDFExport` | — | export() → .pdf |
| `ExcelExport` | — | export() → .xlsx |

### Filter & Access Control

| Class | Key Attributes | Key Methods |
|---|---|---|
| `FilterCriteria` | departments, dateRange, employeeCategories | apply(), reset() |
| `DateRange` | startDate, endDate | isValid(), getDurationDays() |
| `User` | userId, name, role, permissions | login(), hasPermission() |
| `AccessControl` | — | authorize(), getRole(), checkPermission() |

---

## 4. Design Pattern Work

### Abstract Factory — Two Identified Candidates

**Candidate 1 (PRIORITY): Chart Factory**
- Existing inheritance: `Chart → LineChart / BarChart / PieChart`
- Only needs factory layer added on top
- Factory interface: `ChartFactory` with `createLineChart()`, `createBarChart()`, `createPieChart()`
- Concrete factories: `EmployeeGrowthChartFactory`, `AttritionChartFactory`, etc.

**Candidate 2: Report/Export Factory**
- Pairs `Report` objects with `ExportFormat` variants
- Factory creates matching report + export format combos (CSV, PDF, Excel)

### Recommendation
Implement Chart Factory first — the class hierarchy already exists; only the factory abstraction needs adding.

---

## 5. Data Fields Inventory (~50 fields across modules)

| Module | Fields |
|---|---|
| Employee | employeeId, name, department, role, joinDate, status |
| Payroll | payrollId, employeeId, salary, bonus, deductions, period |
| Attendance | attendanceId, employeeId, date, status, hoursWorked |
| Performance | performanceId, employeeId, reviewPeriod, score, feedback |
| Department | deptId, name, managerId, headcount |
| FilterCriteria | departments[], employeeCategories[] |
| DateRange | startDate, endDate |
| Metrics | attritionRate, totalHeadcount, avgPerformanceScore, payrollStats |
| Dashboard/Chart | dashboardId, filters, kpiCards[], charts[], chartType, xData, yData, labels, values |
| Report | reportId, type, generatedAt, filters, data |
| Export | fileType (csv/pdf/xlsx) |
| User/Access Control | userId, name, role, permissions[] |

Read/Write flags: Employee/Dept/Payroll/Attendance/Performance are Read (sourced from HRMS core). Metrics, Dashboard, Report, Export are Write (generated by the sub-system).

---

## 6. Exception Categories (from prior documentation work)

Three severity levels documented:

| Category | Examples |
|---|---|
| **MAJOR** | DB connection failure, authentication failure, data corruption, unauthorized access |
| **MINOR** | Missing optional fields, partial data load, export format mismatch |
| **WARNING** | Stale cache, slow query, deprecated filter used |

Exception table format: Category | Exception Name | Error Message | Handling Plan  
Output formats produced previously: PDF (landscape A4, color-coded rows) and DOCX.

---

## 7. File Extraction Method (important for this project)

Project files at `/mnt/project/` are ZIP-formatted files with `.pdf` extensions. Standard PDF tools (`pdftotext`, `pdftoppm`) do NOT work.

**Correct extraction method:**
```bash
cp /mnt/project/HR_Analytics__ReportingClass.pdf /tmp/class.zip
cp /mnt/project/HR_Analytics_Reporting_SubSystem_1_1.pdf /tmp/arch.zip
mkdir -p /tmp/class_ex /tmp/arch_ex
unzip -o /tmp/class.zip -d /tmp/class_ex
unzip -o /tmp/arch.zip -d /tmp/arch_ex
# Then read: /tmp/class_ex/1.txt, /tmp/arch_ex/1.txt, 2.txt, 3.txt
# Images: /tmp/class_ex/1.jpeg, /tmp/arch_ex/3.jpeg (backend diagram), /tmp/arch_ex/2.jpeg (UI wireframe)
```

---

## 8. Communication & Workflow Notes

- Rhrishi communicates tersely — single-word requests signal format changes (e.g., "docx", "make shorter", "landscape")
- Iterative refinement is standard: generate → adjust format/conciseness → regenerate
- Document preferences: landscape A4, color-coded category labels, alternating row shading, concise 1–2 sentence descriptions
- Skills available: `docx`, `pdf`, `xlsx`, `pptx`, `frontend-design`, `pdf-reading`
- npm library for DOCX: `docx`; PDF and Excel generation also in use

---

## 9. Completed Deliverables (prior sessions)

1. **Exception Table PDF** — Landscape A4, color-coded MAJOR/MINOR/WARNING rows
2. **Exception Table DOCX** — Same content, Word format
3. **Data_details.xlsx** — ~50 fields organized by module with data types and R/W flags
4. **Design pattern analysis** — Abstract Factory recommendation with implementation guidance

---
*Export generated: April 16, 2026*
