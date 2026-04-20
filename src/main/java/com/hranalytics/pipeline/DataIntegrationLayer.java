package com.hranalytics.pipeline;

import com.hranalytics.domain.*;
import com.hranalytics.exceptions.DataIntegrationMergeConflictException;
import com.hranalytics.exceptions.DataSchemaValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Second stage of the analytics pipeline.
 * Validates incoming raw data against schema rules, merges duplicate records
 * using last-write-wins, and produces a clean RawHRData ready for DataProcessingEngine.
 * Owner: Prem M Thakur (pipeline boundary).
 */
public class DataIntegrationLayer {

    private static final Logger LOG = Logger.getLogger(DataIntegrationLayer.class.getName());

    private final List<String> validationErrors = new ArrayList<>();
    private final List<String> mergeConflictLog = new ArrayList<>();

    /**
     * Validates and de-duplicates the raw data bundle.
     * Invalid records are skipped and logged (DATA_SCHEMA_VALIDATION_FAILURE).
     * Merge conflicts are resolved by last-write-wins (DATA_INTEGRATION_MERGE_CONFLICT).
     */
    public RawHRData integrate(RawHRData raw) {
        validationErrors.clear();
        mergeConflictLog.clear();

        RawHRData clean = new RawHRData();
        clean.setEmployees(validateEmployees(raw.getEmployees()));
        clean.setDepartments(raw.getDepartments());
        clean.setPayrollRecords(validateAndMergePayroll(raw.getPayrollRecords()));
        clean.setAttendanceRecords(raw.getAttendanceRecords());
        clean.setPerformanceRecords(validatePerformance(raw.getPerformanceRecords()));

        if (!validationErrors.isEmpty()) {
            LOG.warning("Batch validation summary — " + validationErrors.size() + " record(s) skipped: " + validationErrors);
        }
        if (!mergeConflictLog.isEmpty()) {
            LOG.info("Merge conflicts resolved (last-write-wins): " + mergeConflictLog);
        }
        return clean;
    }

    /** Validates employees: requires non-null employeeId, name, and department. */
    private List<Employee> validateEmployees(List<Employee> employees) {
        List<Employee> valid = new ArrayList<>();
        for (Employee e : employees) {
            try {
                if (e.getEmployeeId() == null || e.getEmployeeId().isBlank()) {
                    throw new DataSchemaValidationException("employeeId",
                            "Employee record missing employeeId — skipping.");
                }
                if (e.getName() == null || e.getName().isBlank()) {
                    throw new DataSchemaValidationException("name",
                            "Employee " + e.getEmployeeId() + " has no name — skipping.");
                }
                if (e.getDepartment() == null || e.getDepartment().isBlank()) {
                    throw new DataSchemaValidationException("department",
                            "Employee " + e.getEmployeeId() + " has no department — skipping.");
                }
                valid.add(e);
            } catch (DataSchemaValidationException ex) {
                validationErrors.add(ex.getMessage());
                LOG.warning("Schema validation: " + ex.getMessage());
            }
        }
        return valid;
    }

    /**
     * De-duplicates payroll: if two records share the same payrollId, the later one wins
     * and a DATA_INTEGRATION_MERGE_CONFLICT is logged.
     */
    private List<Payroll> validateAndMergePayroll(List<Payroll> records) {
        Map<String, Payroll> merged = new HashMap<>();
        for (Payroll p : records) {
            if (merged.containsKey(p.getPayrollId())) {
                DataIntegrationMergeConflictException conflict =
                        new DataIntegrationMergeConflictException(
                                p.getPayrollId(), "grossSalary",
                                "Duplicate payroll record " + p.getPayrollId() + " — last-write-wins.");
                mergeConflictLog.add(conflict.getMessage());
                LOG.warning(conflict.getMessage());
            }
            merged.put(p.getPayrollId(), p); // last-write-wins
        }
        return new ArrayList<>(merged.values());
    }

    /** Validates performance records: score must be in [0.0, 5.0]. */
    private List<Performance> validatePerformance(List<Performance> records) {
        List<Performance> valid = new ArrayList<>();
        for (Performance p : records) {
            try {
                if (p.getScore() < 0.0f || p.getScore() > 5.0f) {
                    throw new DataSchemaValidationException("score",
                            "Performance " + p.getPerformanceId() + " score=" + p.getScore()
                            + " out of [0,5] range — skipping.");
                }
                valid.add(p);
            } catch (DataSchemaValidationException ex) {
                validationErrors.add(ex.getMessage());
                LOG.warning("Schema validation: " + ex.getMessage());
            }
        }
        return valid;
    }

    public List<String> getValidationErrors() { return validationErrors; }
    public List<String> getMergeConflictLog() { return mergeConflictLog; }
}
