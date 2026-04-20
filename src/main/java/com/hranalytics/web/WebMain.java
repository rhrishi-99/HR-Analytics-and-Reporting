package com.hranalytics.web;

/**
 * Entry point for the HR Analytics web UI mode.
 * Starts an embedded HTTP server on port 8080 — no external libraries needed.
 */
public class WebMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        WebServer.start(port);
        // Keep main thread alive
        Thread.currentThread().join();
    }
}
