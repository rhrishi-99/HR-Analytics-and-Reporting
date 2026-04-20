package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Central engine that delegates metric calculations to registered MetricStrategy instances.
 * Uses a Map<MetricType, MetricStrategy> — no switch statements or if-else chains on type.
 * To add a new metric: register a new MetricStrategy implementation. Do not modify this class.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class MetricsCalculationEngine {

    private static final Logger LOG = Logger.getLogger(MetricsCalculationEngine.class.getName());

    /** Strategy registry: each MetricType maps to exactly one MetricStrategy. */
    private final Map<MetricType, MetricStrategy> strategies = new EnumMap<>(MetricType.class);

    public MetricsCalculationEngine() {
        // Register all built-in strategies
        strategies.put(MetricType.ATTRITION_RATE,        new AttritionRateStrategy());
        strategies.put(MetricType.EMPLOYEE_GROWTH,       new EmployeeGrowthStrategy());
        strategies.put(MetricType.AVERAGE_PERFORMANCE,   new AveragePerformanceStrategy());
        strategies.put(MetricType.DEPARTMENT_METRICS,    new DepartmentMetricsStrategy());
        strategies.put(MetricType.COMPENSATION_ANALYTICS,new CompensationAnalyticsStrategy());
    }

    /**
     * Calculates a single metric by delegating to the registered strategy.
     * Throws IllegalArgumentException if the metric type has no registered strategy.
     */
    public MetricResult calculate(MetricType type, ProcessedData data) {
        MetricStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for MetricType: " + type);
        }
        MetricResult result = strategy.calculate(data);
        LOG.info("Calculated: " + result);
        return result;
    }

    /**
     * Calculates all registered metrics and returns a full result map.
     * Every MetricType in the registry is computed even if some fail (flagged results are included).
     */
    public Map<MetricType, MetricResult> calculateAll(ProcessedData data) {
        Map<MetricType, MetricResult> results = new EnumMap<>(MetricType.class);
        strategies.keySet().forEach(type -> results.put(type, calculate(type, data)));
        LOG.info("MetricsCalculationEngine: all " + results.size() + " metrics calculated.");
        return results;
    }

    /**
     * Allows registration of additional strategy implementations at runtime.
     * Called to extend the engine without modifying its source.
     */
    public void registerStrategy(MetricType type, MetricStrategy strategy) {
        strategies.put(type, strategy);
        LOG.info("Registered new strategy for: " + type);
    }
}
