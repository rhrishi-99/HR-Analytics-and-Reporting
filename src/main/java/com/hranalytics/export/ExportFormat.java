package com.hranalytics.export;

import com.hranalytics.reports.Report;

/**
 * Abstract base class for all export format implementations.
 * Participates in the export sub-system as an extensible product.
 * To add a new export format: extend this class and register in ExportSharingModule.
 * Owner: R G Rhrishi (export layer).
 */
public abstract class ExportFormat {

    /**
     * Exports the given report to the format's file type.
     *
     * @param report the report to export
     * @return the file path where the export was written
     */
    public abstract String export(Report report);

    /**
     * Returns the file-type extension this format produces (e.g. "csv", "pdf", "xlsx").
     */
    public abstract String getFileType();

    /** Shared utility: sanitises a string for use as part of a filename. */
    protected String sanitise(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
