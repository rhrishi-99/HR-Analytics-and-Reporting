package com.hranalytics.web;

import com.hranalytics.access.AccessControlModule;
import com.hranalytics.charts.EmployeeGrowthChartFactory;
import com.hranalytics.facade.HRAnalyticsFacade;
import com.hranalytics.integration.ESSPortalPublisher;
import com.hranalytics.integration.PerformanceManagementClient;
import com.hranalytics.integration.stub.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Embedded HTTP server that serves the HR Analytics web UI and REST API.
 * Uses JDK's built-in com.sun.net.httpserver — zero external dependencies.
 * Routes: GET / → index.html, /api/* → ApiHandler
 */
public class WebServer {

    public static void start(int port) throws IOException {
        AccessControlModule acm = new AccessControlModule();
        acm.loadDefaultUsers();

        HRAnalyticsFacade facade = new HRAnalyticsFacade(
                acm,
                new EmployeeGrowthChartFactory(),
                new EmployeeServiceStub(),
                new PayrollServiceStub(),
                new AttendanceServiceStub(),
                new PerformanceManagementClient(new PerformanceServiceStub()),
                new ESSPortalPublisher());

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", new ApiHandler(facade));
        server.createContext("/", ex -> {
            byte[] bytes = loadHtml().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        });
        server.setExecutor(null);
        server.start();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  HR Analytics Web UI is running          ║");
        System.out.printf( "║  http://localhost:%-23s║%n", port + "  ");
        System.out.println("║  Press Ctrl+C to stop                    ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    private static String loadHtml() {
        try (InputStream is = WebServer.class.getResourceAsStream("/web/index.html")) {
            if (is != null) return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
        return "<html><body><h1>Error</h1><p>index.html not found on classpath. Run build.bat web</p></body></html>";
    }
}
