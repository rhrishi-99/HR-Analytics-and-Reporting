package com.hranalytics.web;

import com.hranalytics.domain.DateRange;
import com.hranalytics.domain.FilterCriteria;
import com.hranalytics.exceptions.HRAnalyticsException;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.dto.DashboardSnapshot;
import com.hranalytics.integration.dto.KPISnapshot;
import com.hranalytics.integration.dto.ReportSummary;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP handler for /api/* routes. Delegates to HRAnalyticsFacade and returns JSON.
 * Manually builds JSON to avoid requiring external libraries.
 * Routes: /api/dashboard, /api/report, /api/export, /api/kpi
 */
class ApiHandler implements HttpHandler {

    private final HRAnalyticsFacade facade;

    ApiHandler(HRAnalyticsFacade facade) {
        this.facade = facade;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        try {
            Map<String, String> p = parseQuery(ex.getRequestURI());
            String json = switch (path) {
                case "/api/dashboard" -> dashboardJson(facade.loadDashboard(
                        p.getOrDefault("userId", "U002"), buildFilter(p)));
                case "/api/report"    -> reportJson(facade.generateReport(
                        p.getOrDefault("type", "FULL_HR_SUMMARY"), buildFilter(p)));
                case "/api/export"    -> "{\"path\":\"" + escape(facade.exportReport(
                        p.getOrDefault("reportId", ""), p.getOrDefault("format", "csv"))) + "\"}";
                case "/api/kpi"       -> kpiListJson(facade.getKPISnapshot(
                        p.getOrDefault("userId", "U003"), p.getOrDefault("dept", "Engineering")));
                default -> throw new IllegalArgumentException("Unknown endpoint: " + path);
            };
            send(ex, 200, json);
        } catch (HRAnalyticsException e) {
            send(ex, 403, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            send(ex, 500, "{\"error\":\"" + escape(String.valueOf(e.getMessage())) + "\"}");
        }
    }

    // ── Filter builder ───────────────────────────────────────────────────────

    private FilterCriteria buildFilter(Map<String, String> p) {
        FilterCriteria f = new FilterCriteria();
        f.setFilterStatus(p.getOrDefault("status", "ALL"));
        String dept = p.getOrDefault("dept", "");
        if (!dept.isBlank()) f.setFilterDepartment(List.of(dept.split(",")));
        String start = p.get("start"), end = p.get("end");
        if (start != null && end != null && !start.isBlank() && !end.isBlank())
            f.setDateRange(new DateRange(LocalDate.parse(start), LocalDate.parse(end)));
        return f;
    }

    // ── JSON builders ────────────────────────────────────────────────────────

    private String dashboardJson(DashboardSnapshot d) {
        return "{" +
            "\"dashboardId\":\"" + d.getDashboardId() + "\"," +
            "\"attritionRate\":"  + d.getAttritionRate()   + "," +
            "\"employeeGrowth\":" + d.getEmployeeGrowth()  + "," +
            "\"avgPerformance\":" + d.getAvgPerformance()  + "," +
            "\"widgetIds\":"      + strListJson(d.getWidgetIds())  + "," +
            "\"insights\":"       + strListJson(d.getInsights())   + "," +
            "\"kpiCards\":"       + kpiListJson(d.getKpiCards())   +
            "}";
    }

    private String reportJson(ReportSummary r) {
        return "{" +
            "\"reportId\":\""     + r.getReportId()                    + "\"," +
            "\"reportTitle\":\""  + escape(r.getReportTitle())         + "\"," +
            "\"reportType\":\""   + r.getReportType()                  + "\"," +
            "\"sectionCount\":"   + r.getSectionCount()                + "," +
            "\"generatedDate\":\"" + r.getGeneratedDate()              + "\"" +
            "}";
    }

    private String kpiListJson(List<KPISnapshot> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            KPISnapshot k = list.get(i);
            sb.append("{")
              .append("\"metricName\":\"")   .append(escape(k.getMetricName()))  .append("\",")
              .append("\"currentValue\":")   .append(k.getCurrentValue())        .append(",")
              .append("\"previousValue\":")  .append(k.getPreviousValue())       .append(",")
              .append("\"trend\":\"")        .append(k.getTrend())               .append("\",")
              .append("\"unit\":\"")         .append(escape(k.getUnit()))        .append("\",")
              .append("\"flagged\":")        .append(k.isFlagged())
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

    // ── Utilities ────────────────────────────────────────────────────────────

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
