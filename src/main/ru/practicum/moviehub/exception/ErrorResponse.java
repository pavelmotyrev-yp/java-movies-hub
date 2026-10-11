package ru.practicum.moviehub.exception;

import java.util.Collections;
import java.util.List;

public class ErrorResponse {
    private String error;
    private List<String> details;

    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = details != null ? details : Collections.emptyList();
    }

    public String getError() {
        return error;
    }

    public List<String> getDetails() {
        return details;
    }
}
