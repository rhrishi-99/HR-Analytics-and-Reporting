package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

import java.util.EnumMap;
import java.util.Map;

/**
 * Engine that manages and executes metric calculations.
 * Each calculator is wrapped in a MetricCalculatorProxy for logging and safety.
 * Pattern: Proxy (Structural). Owner: Raihan Naeem.
 */
public class MetricsCalculationEngine {

    private final Map<MetricType, MetricCalculator> calculators = new EnumMap<>(MetricType.class);

    public MetricsCalculationEngine() {
        registerCalculator(new AttritionRateCalculator());
        registerCalculator(new EmployeeGrowthCalculator());
        registerCalculator(new AveragePerformanceCalculator());
        registerCalculator(new DepartmentMetricsCalculator());
        registerCalculator(new CompensationAnalyticsCalculator());
    }

    /** Dispatches to the registered calculator (which is automatically proxied). */
    public MetricResult calculate(MetricType type, ProcessedData data) {
        MetricCalculator calc = calculators.get(type);
        if (calc == null) {
            throw new IllegalArgumentException("No calculator for: " + type);
        }
        return calc.calculate(data);
    }

    /** Calculates all registered metrics. */
    public Map<MetricType, MetricResult> calculateAll(ProcessedData data) {
        Map<MetricType, MetricResult> results = new EnumMap<>(MetricType.class);
        calculators.keySet().forEach(type -> results.put(type, calculate(type, data)));
        return results;
    }

    /** Registers a calculator, wrapping it in a Proxy automatically. */
    public void registerCalculator(MetricCalculator calculator) {
        calculators.put(calculator.getMetricType(), new MetricCalculatorProxy(calculator));
    }
}
