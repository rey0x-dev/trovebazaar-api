package com.trovebazaar.trovebazaarapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {

    private String errorMessage;
    private LocalDateTime errorTimestamp;
    private Map<String, String> errors;
    private Integer status;

    public ApiError(String errorMessage, LocalDateTime errorTimestamp, Map<String, String> errors, Integer status) {
        this.errorMessage = errorMessage;
        this.errorTimestamp = errorTimestamp;
        this.errors = errors;
        this.status = status;
    }

    public ApiError(String errorMessage, LocalDateTime errorTimestamp, Integer status) {
        this.errorMessage = errorMessage;
        this.errorTimestamp = errorTimestamp;
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getErrorTimestamp() {
        return errorTimestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public Integer getStatus() {
        return status;
    }
}
