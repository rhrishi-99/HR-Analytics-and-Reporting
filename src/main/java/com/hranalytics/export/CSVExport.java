package com.hranalytics.export;

import com.hranalytics.dashboard.KPICard;
import com.hranalytics.reports.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Exports a Report to CSV format.
 * Each KPI card becomes a row; report sections are appended as a trailing narrative block.
 * Owner: R G Rhrishi (export layer).
 */
public class CSVExport extends ExportFormat {

    private static final Logger LOG = Logger.getLogger(CSVExport.class.getName());
    private static final String OUTPUT_DIR = "output/exports/";

    @Override
    public String export(Report report) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String filename = OUTPUT_DIR + sanitise(report.getReportId()) + ".csv";

            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                // Header
                writer.println("Report ID,Report Title,Report Type,Generated Date");
                writer.printf("%s,%s,%s,%s%n",
                        report.getReportId(), quote(report.getReportTitle()),
                        report.getType(), report.getGeneratedDate());
                writer.println();

                // KPI section
                writer.println("Metric Name,Current Value,Previous Value,Trend,Unit,Flagged");
                for (KPICard card : report.getKpiSnapshot()) {
                    writer.printf("%s,%.4f,%.4f,%s,%s,%s%n",
                            quote(card.getMetricName()),
                            card.getCurrentValue(), card.getPreviousValue(),
                            card.getTrend(), card.getUnit(), card.isFlagged());
                }
                writer.println();

                // Narrative sections
                writer.println("Section");
                report.getReportSections().forEach(s -> writer.println(quote(s)));
            }

            LOG.info("CSVExport: written to " + filename);
            return filename;

        } catch (IOException e) {
            LOG.severe("CSVExport failed: " + e.getMessage());
            return "export_failed.csv";
        }
    }

    @Override
    public String getFileType() { return "csv"; }

    private String quote(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
