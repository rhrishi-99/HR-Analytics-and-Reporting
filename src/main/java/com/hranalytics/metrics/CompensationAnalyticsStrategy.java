package com.hranalytics.metrics;

import com.hranalytics.exceptions.MetricCalculationOverflowException;
import com.hranalytics.pipeline.ProcessedData;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes average gross compensation and per-department salary breakdown.
 * Pattern: Strategy (Behavioural). Owner: Raihan Naeem.
 */
public class CompensationAnalyticsStrategy implements MetricStrategy {

    private static final Logger LOG = Logger.getLogger(CompensationAnalyticsStrategy.class.getName());

    @Override
    public MetricResult calculate(ProcessedData data) {
        try {
            int headcount = data.getTotalHeadcount();
            if (headcount == 0) {
                throw new MetricCalculationOverflowException("COMPENSATION_ANALYTICS",
                        "Headcount is 0 — cannot compute average compensation.");
            }

            double avgCompensation     = data.getTotalGrossSalary() / headcount;
            double prevAvgCompensation = avgCompensation * 0.97; // simulated prior-period

            Map<String, Double> avgByDept = data.getAvgSalaryByDepartment();

            LOG.info(String.format("CompensationAnalyticsStrategy: avgComp=%.2f, deptBreakdown=%s",
                    avgCompensation, avgByDept));

            return new MetricResult(MetricType.COMPENSATION_ANALYTICS, "Avg Compensation",
                    avgCompensation, prevAvgCompensation, "$", avgByDept);

        } catch (MetricCalculationOverflowException ex) {
            LOG.warning("METRIC_CALCULATION_OVERFLOW: " + ex.getMessage());
            return new MetricResult(MetricType.COMPENSATION_ANALYTICS, "Avg Compensation", "$");
        }
    }
}
