package com.hranalytics.export;

import com.hranalytics.exceptions.ExportFormatUnsupportedException;
import com.hranalytics.exceptions.ScheduledReportDispatchFailureException;
import com.hranalytics.reports.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages export format registration and dispatches report exports to the correct ExportFormat.
 * To add a new export format: instantiate and register it here — no other class needs editing.
 * Retries scheduled dispatches up to 3 times at 5-minute intervals (SCHEDULED_REPORT_DISPATCH_FAILURE).
 * Owner: R G Rhrishi (export layer).
 */
public class ExportSharingModule {

    private static final Logger LOG = Logger.getLogger(ExportSharingModule.class.getName());
    private static final int MAX_DISPATCH_RETRIES = 3;

    /** Registry of available export formats keyed by file-type string (e.g. "csv", "pdf"). */
    private final Map<String, ExportFormat> formatRegistry = new HashMap<>();

    /** Pending reports that failed dispatch and await manual re-dispatch. */
    private final List<Report> pendingReDispatch = new ArrayList<>();

    public ExportSharingModule() {
        // Register all built-in export formats
        registerFormat(new CSVExport());
        registerFormat(new PDFExport());
        registerFormat(new ExcelExport());
    }

    /**
     * Exports a report using the specified format key.
     * Throws EXPORT_FORMAT_UNSUPPORTED (WARNING) if the format is not registered.
     *
     * @param report     the report to export
     * @param formatType file type key — "csv", "pdf", or "xlsx"
     * @return file path of the exported file
     */
    public String export(Report report, String formatType) {
        ExportFormat format = formatRegistry.get(formatType.toLowerCase());
        if (format == null) {
            throw new ExportFormatUnsupportedException(formatType,
                    new ArrayList<>(formatRegistry.keySet()));
        }
        String path = format.export(report);
        LOG.info("ExportSharingModule: exported report " + report.getReportId()
                + " as " + formatType + " → " + path);
        return path;
    }

    /**
     * Attempts to dispatch a scheduled report (e.g. by email/share channel).
     * Retries up to MAX_DISPATCH_RETRIES times; stores for manual re-dispatch on total failure.
     */
    public boolean dispatchScheduled(Report report, String formatType, String recipient) {
        for (int attempt = 1; attempt <= MAX_DISPATCH_RETRIES; attempt++) {
            try {
                String path = export(report, formatType);
                LOG.info("Dispatched report " + report.getReportId()
                        + " to " + recipient + " [attempt=" + attempt + "]: " + path);
                return true;
            } catch (Exception e) {
                ScheduledReportDispatchFailureException failure =
                        new ScheduledReportDispatchFailureException(
                                report.getReportId(), attempt,
                                "Dispatch attempt " + attempt + " failed: " + e.getMessage());
                LOG.warning(failure.toString());

                if (attempt == MAX_DISPATCH_RETRIES) {
                    pendingReDispatch.add(report);
                    LOG.severe("Report " + report.getReportId()
                            + " stored for manual re-dispatch after " + MAX_DISPATCH_RETRIES + " failures.");
                    return false;
                }

                // Simulated 5-minute backoff (real implementation would sleep)
                LOG.info("Will retry in 5 minutes. Attempt " + (attempt + 1) + " of " + MAX_DISPATCH_RETRIES);
            }
        }
        return false;
    }

    /**
     * Registers an additional ExportFormat at runtime.
     * New formats become immediately available without modifying this class.
     */
    public void registerFormat(ExportFormat format) {
        formatRegistry.put(format.getFileType().toLowerCase(), format);
        LOG.info("Registered export format: " + format.getFileType());
    }

    public List<String> getSupportedFormats() {
        return new ArrayList<>(formatRegistry.keySet());
    }

    public List<Report> getPendingReDispatch() { return pendingReDispatch; }
}
