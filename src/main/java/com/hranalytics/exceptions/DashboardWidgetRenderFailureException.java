package com.hranalytics.exceptions;

/**
 * Raised when an individual dashboard widget fails to render.
 * Category: WARNING — show 'Data Unavailable' placeholder; other widgets continue rendering.
 * Owner: R G Rhrishi.
 */
public class DashboardWidgetRenderFailureException extends HRAnalyticsException {

    public static final String ERROR_CODE = "DASHBOARD_WIDGET_RENDER_FAILURE";

    private final String widgetId;

    public DashboardWidgetRenderFailureException(String widgetId, String message) {
        super(ERROR_CODE, Category.WARNING, message);
        this.widgetId = widgetId;
    }

    public DashboardWidgetRenderFailureException(String widgetId, String message, Throwable cause) {
        super(ERROR_CODE, Category.WARNING, message, cause);
        this.widgetId = widgetId;
    }

    public String getWidgetId() { return widgetId; }
}
