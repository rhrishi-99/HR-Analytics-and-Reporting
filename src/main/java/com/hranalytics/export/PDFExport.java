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
 * Simulates exporting a Report to PDF format.
 * Writes a structured text representation using a .pdf extension.
 * (Real PDF generation would use a library such as iText; simulation keeps the project dependency-free.)
 * Owner: R G Rhrishi (export layer).
 */
public class PDFExport extends ExportFormat {

    private static final Logger LOG = Logger.getLogger(PDFExport.class.getName());
    private static final String OUTPUT_DIR = "output/exports/";

    @Override
    public String export(Report report) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String filename = OUTPUT_DIR + sanitise(report.getReportId()) + ".pdf";

            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("================================================================");
                writer.println("  HR ANALYTICS REPORT");
                writer.println("================================================================");
                writer.printf("  ID      : %s%n", report.getReportId());
                writer.printf("  Title   : %s%n", report.getReportTitle());
                writer.printf("  Type    : %s%n", report.getType());
                writer.printf("  Date    : %s%n", report.getGeneratedDate());
                writer.println("================================================================");
                writer.println();

                writer.println("  KEY PERFORMANCE INDICATORS");
                writer.println("  ─────────────────────────────────────────────────────────");
                for (KPICard card : report.getKpiSnapshot()) {
                    String flag = card.isFlagged() ? " ⚠ DATA UNAVAILABLE" : "";
                    writer.printf("  %-28s  %8.2f %-6s  [%s]%s%n",
                            card.getMetricName(), card.getCurrentValue(),
                            card.getUnit(), card.getTrend(), flag);
                }
                writer.println();

                writer.println("  REPORT SECTIONS");
                writer.println("  ─────────────────────────────────────────────────────────");
                report.getReportSections().forEach(s -> writer.println("  " + s));
                writer.println();
                writer.println("================================================================");
                writer.println("  [END OF REPORT]");
                writer.println("================================================================");
            }

            LOG.info("PDFExport: written to " + filename);
            return filename;

        } catch (IOException e) {
            LOG.severe("PDFExport failed: " + e.getMessage());
            return "export_failed.pdf";
        }
    }

    @Override
    public String getFileType() { return "pdf"; }
}
