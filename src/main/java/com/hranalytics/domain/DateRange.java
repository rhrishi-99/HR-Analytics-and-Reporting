package com.hranalytics.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value object representing a closed date interval used in filters and queries.
 */
public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate()   { return endDate; }

    /** Returns true if both dates are set and start is not after end. */
    public boolean isValid() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    /** Returns the number of calendar days covered by this range. */
    public long getDurationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /** Returns true if the given date falls within this range (inclusive). */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public String toString() {
        return String.format("DateRange[%s → %s]", startDate, endDate);
    }
}
