package com.hranalytics.web;

import com.hranalytics.exceptions.HRAnalyticsException;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;
import com.hranalytics.web.controller.DashboardController;
import com.hranalytics.web.controller.KPIController;
import com.hranalytics.web.controller.ReportController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MVC — View + Router layer for the web API.
 * Routes incoming HTTP requests to the appropriate Controller,
 * receives the Model (DTO), and serialises it to JSON for the View (browser).
 *
 * Responsibilities of this class (View/Router only):
 *   - Parse URL query parameters
 *   - Route path → correct Controller method
 *   - Serialise DTO Model → JSON string
 *   - Write HTTP response
 *
 * It does NOT contain any business logic — that lives in the Controllers and Facade.
 * Pattern: MVC — View/Router. Owner: R G Rhrishi.
 */
class ApiHandler implements HttpHandler {

    // MVC Controllers — business logic delegates live here
    private final DashboardController dashboardController;
    private final ReportController    reportController;
    private final KPIController       kpiController;

    ApiHandler(HRAnalyticsFacade facade) {
        this.dashboardController = new DashboardController(facade);
        this.reportController    = new ReportController(facade);
        this.kpiController       = new KPIController(facade);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        try {
            Map<String, String> params = parseQuery(ex.getRequestURI());

            // Router — delegates to the correct MVC Controller
            String json = switch (path) {
                case "/api/dashboard" -> dashboardJson(dashboardController.handle(params));
                case "/api/report"    -> reportJson(reportController.handleGenerate(params));
                case "/api/export"    -> "{\"path\":\"" + escape(reportController.handleExport(params)) + "\"}";
                case "/api/kpi"       -> kpiListJson(kpiController.handle(params));
                default -> throw new IllegalArgumentException("Unknown endpoint: " + path);
            };
            send(ex, 200, json);

        } catch (HRAnalyticsException e) {
            send(ex, 403, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            send(ex, 500, "{\"error\":\"" + escape(String.valueOf(e.getMessage())) + "\"}");
        }
    }

    // ── View: JSON serialisers (Model → JSON string) ─────────────────────────

    private String dashboardJson(DashboardSnapshot d) {
        return "{" +
            "\"dashboardId\":\""  + d.getDashboardId()    + "\"," +
            "\"attritionRate\":"  + d.getAttritionRate()  + "," +
            "\"employeeGrowth\":" + d.getEmployeeGrowth() + "," +
            "\"avgPerformance\":" + d.getAvgPerformance() + "," +
            "\"widgetIds\":"      + strListJson(d.getWidgetIds())  + "," +
            "\"insights\":"       + strListJson(d.getInsights())   + "," +
            "\"kpiCards\":"       + kpiListJson(d.getKpiCards())   +
            "}";
    }

    private String reportJson(ReportSummary r) {
        return "{" +
            "\"reportId\":\""      + r.getReportId()          + "\"," +
            "\"reportTitle\":\""   + escape(r.getReportTitle()) + "\"," +
            "\"reportType\":\""    + r.getReportType()         + "\"," +
            "\"sectionCount\":"    + r.getSectionCount()       + "," +
            "\"generatedDate\":\"" + r.getGeneratedDate()      + "\"" +
            "}";
    }

    private String kpiListJson(List<KPISnapshot> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            KPISnapshot k = list.get(i);
            sb.append("{")
              .append("\"metricName\":\"")  .append(escape(k.getMetricName()))  .append("\",")
              .append("\"currentValue\":")  .append(k.getCurrentValue())        .append(",")
              .append("\"previousValue\":") .append(k.getPreviousValue())       .append(",")
              .append("\"trend\":\"")       .append(k.getTrend())               .append("\",")
              .append("\"unit\":\"")        .append(escape(k.getUnit()))        .append("\",")
              .append("\"flagged\":")       .append(k.isFlagged())
              .append("}");
        }
        return sb.append("]").toString();
    }

    private String strListJson(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(list.get(i))).append("\"");
        }
        return sb.append("]").toString();
    }

    // ── Router utilities ─────────────────────────────────────────────────────

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String q = uri.getQuery();
        if (q == null) return map;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2)
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return map;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
