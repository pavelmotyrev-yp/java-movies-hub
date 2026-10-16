package ru.practicum.moviehub.model;

/**
 * Представляет фильм в системе.
 *
 * @param id    уникальный идентификатор фильма
 * @param title название фильма
 * @param year  год выпуска фильма
 */
public record Movie(int id, String title, int year) {
}