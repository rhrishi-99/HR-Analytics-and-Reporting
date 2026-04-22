package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Delegates metric calculations to registered MetricCalculator instances.
 * Each real calculator is wrapped in a MetricCalculatorProxy before registration,
 * so logging and overflow handling are applied transparently at the proxy layer.
 * Uses Map<MetricType, MetricCalculator> — no switch statements or if-else chains.
 * To add a new metric: implement MetricCalculator, call registerCalculator(). Do not modify this class.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class MetricsCalculationEngine {

    private static final Logger LOG = Logger.getLogger(MetricsCalculationEngine.class.getName());

    private final Map<MetricType, MetricCalculator> calculators = new EnumMap<>(MetricType.class);

    public MetricsCalculationEngine() {
        registerCalculator(new AttritionRateCalculator());
        registerCalculator(new EmployeeGrowthCalculator());
        registerCalculator(new AveragePerformanceCalculator());
        registerCalculator(new DepartmentMetricsCalculator());
        registerCalculator(new CompensationAnalyticsCalculator());
    }

    /** Calculates a single metric by delegating to the registered (proxied) calculator. */
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

    /**
     * Registers a new calculator at runtime — wraps it in a MetricCalculatorProxy automatically.
     * Extends the engine without modifying its logic (OCP).
     */
    public void registerCalculator(MetricCalculator calculator) {
        MetricType type = calculator.getMetricType();
        calculators.put(type, new MetricCalculatorProxy(calculator));
        LOG.info("Registered proxied calculator for: " + type);
    }
}
