package com.hranalytics.exceptions;

import java.util.List;

/**
 * Raised when the requested export format is not registered in ExportSharingModule.
 * Category: WARNING — show supported formats to user, prompt to select an alternative.
 * Owner: R G Rhrishi.
 */
public class ExportFormatUnsupportedException extends HRAnalyticsException {

    public static final String ERROR_CODE = "EXPORT_FORMAT_UNSUPPORTED";

    private final String requestedFormat;
    private final List<String> supportedFormats;

    public ExportFormatUnsupportedException(String requestedFormat, List<String> supportedFormats) {
        super(ERROR_CODE, Category.WARNING,
              "Export format '" + requestedFormat + "' is not supported. Supported: " + supportedFormats);
        this.requestedFormat = requestedFormat;
        this.supportedFormats = supportedFormats;
    }

    public String getRequestedFormat()       { return requestedFormat; }
    public List<String> getSupportedFormats(){ return supportedFormats; }
}
