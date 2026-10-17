package ru.practicum.moviehub.model;

/**
 * Объект для создания нового фильма (Data Transfer Object).
 * Используется для передачи данных из HTTP-запроса в сервисный слой.
 *
 * @param title название фильма
 * @param year  год выпуска фильма
 */
public record MovieDto(String title, int year) {
}
