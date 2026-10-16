package ru.practicum.moviehub.exception;

import java.util.Collections;
import java.util.List;

/**
 * Объект ошибки, возвращаемый в теле HTTP-ответа.
 * Сериализуется в JSON с полями "error" и "details".
 *
 * @see #error    краткое описание ошибки
 * @see #details  список деталей об ошибке
 */
public class ErrorResponse {
    private String error;
    private List<String> details;

    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = details != null ? details : Collections.emptyList();
    }

    /**
     * Возвращает краткое описание ошибки.
     */
    public String getError() {
        return error;
    }

    /**
     * Возвращает список деталей об ошибке.
     */
    public List<String> getDetails() {
        return details;
    }
}
