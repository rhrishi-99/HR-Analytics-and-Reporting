package com.hranalytics.metrics;

import com.hranalytics.pipeline.ProcessedData;

/**
 * Strategy interface for all metric calculations.
 * Each concrete strategy computes exactly one metric from ProcessedData.
 * MetricsCalculationEngine delegates to this interface — no switch/if-else chains.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public interface MetricStrategy {

    /**
     * Computes the metric and returns a MetricResult.
     * Implementations must catch arithmetic errors and return a flagged result
     * (via MetricCalculationOverflowException handling) rather than propagating them.
     *
     * @param data fully processed HR data bundle
     * @return computed metric result
     */
    MetricResult calculate(ProcessedData data);
}
