package com.hranalytics.integration.stub;

import com.hranalytics.integration.external.PerformanceRecord;
import com.hranalytics.integration.service.PerformanceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stub implementation of PerformanceService backed by hardcoded simulation data.
 * Swap for the Performance Management team's real implementation with no other changes.
 */
public class PerformanceServiceStub implements PerformanceService {

    private final List<PerformanceRecord> records = buildSimulatedRecords();

    @Override
    public List<PerformanceRecord> getPerformanceByEmployee(String employeeId) {
        return records.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceRecord> getPerformanceByCycle(String cycle) {
        return records.stream()
                .filter(r -> r.getCycle().equalsIgnoreCase(cycle))
                .collect(Collectors.toList());
    }

    @Override
    public double getAveragePerformanceScore(String employeeId) {
        return records.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .mapToDouble(PerformanceRecord::getScore)
                .average()
                .orElse(0.0);
    }

    private static List<PerformanceRecord> buildSimulatedRecords() {
        LocalDate reviewDate = LocalDate.of(2025, 3, 31);
        String cycle = "Q1-2025";
        String reviewer = "David Lee";
        String[] empIds = {"E001","E002","E003","E004","E005","E006",
                           "E007","E008","E009","E010","E011","E012","E013","E014"};
        double[] scores = {4.5, 3.8, 4.8, 4.2, 3.9, 4.0, 4.9, 4.1, 3.6, 3.7, 4.3, 3.5, 4.4, 3.9};
        String[] feedback = {
            "Excellent technical leadership and delivery.",
            "Good progress; needs to improve code review participation.",
            "Outstanding delivery and mentorship of junior engineers.",
            "Strong HR partnership; led successful Q1 recruitment drive.",
            "Reliable coordination; scope for improvement in HRIS reporting.",
            "Solid analytical skills; well-regarded by the Finance team.",
            "Exceptional strategic financial oversight and board reporting.",
            "Strong infrastructure work; led zero-downtime deployment pipeline.",
            "Good data pipeline delivery; growing domain expertise.",
            "Proactive recruiter; exceeded Q1 hiring targets.",
            "Accurate bookkeeping and compliance; low error rate.",
            "Quality focus appreciated; test coverage improved by 18%.",
            "Effective budget management and forecasting.",
            "Promising first two quarters; needs production deployment experience."
        };

        List<PerformanceRecord> list = new ArrayList<>();
        for (int i = 0; i < empIds.length; i++) {
            list.add(new PerformanceRecord(
                    "PF" + String.format("%03d", i + 1),
                    empIds[i], scores[i], feedback[i], reviewDate, reviewer, cycle));
        }
        return list;
    }
}
