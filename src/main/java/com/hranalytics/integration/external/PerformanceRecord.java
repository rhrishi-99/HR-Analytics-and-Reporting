package com.hranalytics.integration.external;

import java.time.LocalDate;

/**
 * External data type returned by the Performance Subsystem via PerformanceService.
 * PerformanceMapper converts this to our domain Performance.
 */
public class PerformanceRecord {

    private final String performanceId;
    private final String employeeId;
    private final double score;       // 0.0 – 5.0
    private final String feedback;
    private final LocalDate reviewDate;
    private final String reviewer;
    private final String cycle;       // e.g. "Q1-2025"

    public PerformanceRecord(String performanceId, String employeeId, double score,
                             String feedback, LocalDate reviewDate, String reviewer, String cycle) {
        this.performanceId = performanceId;
        this.employeeId = employeeId;
        this.score = score;
        this.feedback = feedback;
        this.reviewDate = reviewDate;
        this.reviewer = reviewer;
        this.cycle = cycle;
    }

    public String getPerformanceId() { return performanceId; }
    public String getEmployeeId()    { return employeeId; }
    public double getScore()         { return score; }
    public String getFeedback()      { return feedback; }
    public LocalDate getReviewDate() { return reviewDate; }
    public String getReviewer()      { return reviewer; }
    public String getCycle()         { return cycle; }
}
