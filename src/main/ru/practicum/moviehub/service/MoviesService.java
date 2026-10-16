package ru.practicum.moviehub.service;

import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;

import java.util.Collection;

/**
 * Сервис для управления коллекцией фильмов.
 * Определяет методы для CRUD-операций и фильтрации.
 */
public interface MoviesService {
    /**
     * Возвращает все фильмы.
     *
     * @return коллекция всех фильмов
     */
    Collection<Movie> getMovies();

    /**
     * Возвращает фильмы, отфильтрованные по году выпуска.
     *
     * @param year год выпуска
     * @return коллекция фильмов за указанный год
     */
    Collection<Movie> getMoviesByYear(int year);

    /**
     * Добавляет новый фильм в хранилище.
     *
     * @param movie объект с данными о фильме
     * @return добавленный фильм с присвоенным идентификатором
     */
    Movie addMovie(MovieDTO movie);

    /**
     * Возвращает фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @return фильм с указанным идентификатором
     * @throws ru.practicum.moviehub.exception.NotFoundException если фильм не найден
     */
    Movie getMovie(int id);

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @throws ru.practicum.moviehub.exception.NotFoundException если фильм не найден
     */
    void removeMovie(int id);
}
