package com.hranalytics.domain;

import java.time.LocalDate;

/**
 * Domain class representing a performance review record.
 * Read-only data sourced from the Performance Management Sub-System via integration.
 */
public class Performance {

    private final String performanceId;
    private final String employeeId;
    private final float score;          // 0.0 – 5.0
    private final String feedback;
    private final LocalDate reviewDate;
    private final String reviewer;

    public Performance(String performanceId, String employeeId, float score,
                       String feedback, LocalDate reviewDate, String reviewer) {
        this.performanceId = performanceId;
        this.employeeId = employeeId;
        this.score = score;
        this.feedback = feedback;
        this.reviewDate = reviewDate;
        this.reviewer = reviewer;
    }

    public String getPerformanceId() { return performanceId; }
    public String getEmployeeId()    { return employeeId; }
    public float getScore()          { return score; }
    public String getFeedback()      { return feedback; }
    public LocalDate getReviewDate() { return reviewDate; }
    public String getReviewer()      { return reviewer; }

    @Override
    public String toString() {
        return String.format("Performance[%s, emp=%s, score=%.1f, date=%s]",
                performanceId, employeeId, score, reviewDate);
    }
}
