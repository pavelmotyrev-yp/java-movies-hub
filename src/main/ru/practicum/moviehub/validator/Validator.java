package ru.practicum.moviehub.validator;

import ru.practicum.moviehub.exception.ValidationException;

import java.time.LocalDate;

/**
 * Утилита для валидации входных данных.
 * Содержит статические методы проверки корректности названия и года выпуска фильма.
 */
public class Validator {
    private static final int MIN_YEAR = 1888;

    private Validator() {
    }

    /**
     * Проверяет, что название фильма не пустое и не превышает 100 символов.
     *
     * @param title название фильма
     * @throws ValidationException если название пустое или слишком длинное
     */
    public static void validateMovieTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title is empty: " + title);
        }

        if (title.length() > 100) {
            throw new ValidationException("Title is too long: " + title);
        }
    }

    /**
     * Проверяет, что год выпуска в допустимом диапазоне (1888 — текущий год).
     *
     * @param year год выпуска
     * @throws ValidationException если год вне допустимого диапазона
     */
    public static void validateMovieYear(int year) {
        int thisYear = LocalDate.now().getYear();
        if (year < MIN_YEAR) {
            throw new ValidationException("Year should be more than " + MIN_YEAR + ". Your year: " + year);
        }

        if (year > thisYear) {
            throw new ValidationException("Year should be less than " + thisYear + ". Your year: " + year);
        }
    }
}
