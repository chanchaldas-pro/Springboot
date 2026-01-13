
package com.example.demo.exception;
import java.time.Instant;

public class ErrorResponse {
    private String timestamp;
    private String path;
    private String errorCode;
    private String message;

    public ErrorResponse(String path, String errorCode, String message) {
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
}

