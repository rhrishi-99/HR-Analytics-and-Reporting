package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes the attrition rate as: separations / average headcount × 100.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class AttritionRateStrategy implements MetricStrategy {

    private static final Logger LOG = Logger.getLogger(AttritionRateStrategy.class.getName());

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            int separations = data.getSeparations();
            int current     = data.getTotalHeadcount();
            int previous    = data.getPreviousHeadcount();

            double avgHeadcount = (current + previous) / 2.0;
            if (avgHeadcount == 0) {
                throw new MetricCalculationOverflowException("ATTRITION_RATE",
                        "Average headcount is 0 — cannot compute attrition rate. Inputs: "
                        + "current=" + current + ", previous=" + previous);
            }

            double attritionRate    = (separations / avgHeadcount) * 100.0;
            double prevAttritionRate = attritionRate * 0.85; // simulated prior-period baseline

            LOG.info(String.format("AttritionRateStrategy: separations=%d, avgHC=%.1f, rate=%.2f%%",
                    separations, avgHeadcount, attritionRate));

            return new MetricResult(MetricType.ATTRITION_RATE, "Attrition Rate",
                    attritionRate, prevAttritionRate, "%", Map.of());

        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW: " + ex.getMessage());
            return new MetricResult(MetricType.ATTRITION_RATE, "Attrition Rate", "%");
        }
    }
}
