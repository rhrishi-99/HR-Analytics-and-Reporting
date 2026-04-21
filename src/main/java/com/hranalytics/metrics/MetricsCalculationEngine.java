package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Delegates metric calculations to registered MetricCalculator instances.
 * Uses Map<MetricType, MetricCalculator> — no switch statements or if-else chains.
 * To add a new metric: extend MetricCalculator, register here. Do not modify this class.
 * Pattern: Template Method (Behavioural). Owner: Raihan Naeem.
 */
public class MetricsCalculationEngine {

    private static final Logger LOG = Logger.getLogger(MetricsCalculationEngine.class.getName());

    private final Map<MetricType, MetricCalculator> calculators = new EnumMap<>(MetricType.class);

    public MetricsCalculationEngine() {
        calculators.put(MetricType.ATTRITION_RATE,         new AttritionRateCalculator());
        calculators.put(MetricType.EMPLOYEE_GROWTH,        new EmployeeGrowthCalculator());
        calculators.put(MetricType.AVERAGE_PERFORMANCE,    new AveragePerformanceCalculator());
        calculators.put(MetricType.DEPARTMENT_METRICS,     new DepartmentMetricsCalculator());
        calculators.put(MetricType.COMPENSATION_ANALYTICS, new CompensationAnalyticsCalculator());
    }

    /** Calculates a single metric by delegating to the registered calculator. */
    public MetricResult calculate(MetricType type, ProcessedData data) {
        MetricCalculator calc = calculators.get(type);
        if (calc == null) throw new IllegalArgumentException(
                "No calculator registered for MetricType: " + type);
        MetricResult result = calc.calculate(data);
        LOG.info("Calculated: " + result);
        return result;
    }

    /** Calculates all registered metrics and returns the full result map. */
    public Map<MetricType, MetricResult> calculateAll(ProcessedData data) {
        Map<MetricType, MetricResult> results = new EnumMap<>(MetricType.class);
        calculators.keySet().forEach(type -> results.put(type, calculate(type, data)));
        LOG.info("MetricsCalculationEngine: all " + results.size() + " metrics calculated.");
        return results;
    }

    /** Registers an additional calculator at runtime — extends without modifying this class. */
    public void registerCalculator(MetricType type, MetricCalculator calculator) {
        calculators.put(type, calculator);
        LOG.info("Registered new calculator for: " + type);
    }
}
