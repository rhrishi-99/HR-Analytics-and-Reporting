package com.hranalytics.integration.mapper;

import com.hranalytics.domain.Performance;
import com.hranalytics.integration.external.PerformanceRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts PerformanceRecord (Performance Management Sub-System schema) to domain Performance objects.
 * The external score is a double; our domain Performance.score is a float — cast is applied here.
 * Only this class needs updating if the Performance team changes their schema.
 */
public class PerformanceMapper {

    /** Converts a single PerformanceRecord to a domain Performance. */
    public static Performance toDomain(PerformanceRecord record) {
        return new Performance(
                record.getPerformanceId(),
                record.getEmployeeId(),
                (float) record.getScore(),   // external double → domain float
                record.getFeedback(),
                record.getReviewDate(),
                record.getReviewer()
        );
    }

    /** Converts a list of PerformanceRecords to domain Performance objects. */
    public static List<Performance> toDomainList(List<PerformanceRecord> records) {
        return records.stream()
                .map(PerformanceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
