package com.hranalytics.dashboard;

import com.hranalytics.charts.Chart;

/**
 * A dashboard widget wraps a Chart and adds positional/layout metadata.
 * If rendering fails, the widget stores a placeholder message instead of a chart.
 * Owner: R G Rhrishi (dashboard layer).
 */
public class Widget {

    private final String widgetId;
    private Chart chart;
    private String placeholder;  // shown when chart rendering fails
    private final boolean available;

    /** Constructor for a successfully rendered widget. */
    public Widget(String widgetId, Chart chart) {
        this.widgetId = widgetId;
        this.chart = chart;
        this.available = true;
    }

    /** Constructor for a widget that failed to render (shows placeholder). */
    public Widget(String widgetId, String placeholder) {
        this.widgetId = widgetId;
        this.placeholder = placeholder;
        this.available = false;
    }

    public String getWidgetId()   { return widgetId; }
    public Chart getChart()       { return chart; }
    public String getPlaceholder(){ return placeholder; }
    public boolean isAvailable()  { return available; }

    /** Renders the widget — either the chart or the 'Data Unavailable' placeholder. */
    public String render() {
        if (available && chart != null) {
            return chart.render();
        }
        return "\n  ╔══════════════════════════════════╗\n"
             + "  ║   ⚠  Data Unavailable  [" + widgetId + "]  ║\n"
             + "  ╚══════════════════════════════════╝\n";
    }
}
