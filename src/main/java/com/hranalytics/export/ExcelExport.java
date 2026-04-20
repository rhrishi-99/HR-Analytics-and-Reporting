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
 * Simulates exporting a Report to Excel format.
 * Writes tab-separated values with a .xlsx extension (real Excel generation would use Apache POI).
 * Owner: R G Rhrishi (export layer).
 */
public class ExcelExport extends ExportFormat {

    private static final Logger LOG = Logger.getLogger(ExcelExport.class.getName());
    private static final String OUTPUT_DIR = "output/exports/";

    @Override
    public String export(Report report) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String filename = OUTPUT_DIR + sanitise(report.getReportId()) + ".xlsx";

            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                // Sheet 1: Summary
                writer.println("[Sheet: Summary]");
                writer.println("Report ID\tReport Title\tType\tGenerated Date");
                writer.printf("%s\t%s\t%s\t%s%n",
                        report.getReportId(), report.getReportTitle(),
                        report.getType(), report.getGeneratedDate());
                writer.println();

                // Sheet 2: KPIs
                writer.println("[Sheet: KPIs]");
                writer.println("Metric Name\tCurrent Value\tPrevious Value\tTrend\tUnit\tFlagged");
                for (KPICard card : report.getKpiSnapshot()) {
                    writer.printf("%s\t%.4f\t%.4f\t%s\t%s\t%s%n",
                            card.getMetricName(),
                            card.getCurrentValue(), card.getPreviousValue(),
                            card.getTrend(), card.getUnit(), card.isFlagged());
                }
                writer.println();

                // Sheet 3: Sections
                writer.println("[Sheet: Report Content]");
                report.getReportSections().forEach(writer::println);
            }

            LOG.info("ExcelExport: written to " + filename);
            return filename;

        } catch (IOException e) {
            LOG.severe("ExcelExport failed: " + e.getMessage());
            return "export_failed.xlsx";
        }
    }

    @Override
    public String getFileType() { return "xlsx"; }
}
