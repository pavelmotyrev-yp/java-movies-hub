package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDto;

import java.util.Collection;
import java.util.Optional;

/**
 * Хранилище фильмов (Data Access Layer).
 * Обеспечивает сохранение и получение фильмов из in-memory хранилища.
 */
public interface MoviesStore {
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
    Movie addMovie(MovieDto movie);

    /**
     * Возвращает фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @return Optional с фильмом, либо пустой Optional если не найден
     */
    Optional<Movie> getMovie(int id);

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @return Optional с удалённым фильмом, либо пустой Optional если не найден
     */
    Optional<Movie> removeMovie(int id);

    /**
     * Удаляет все фильмы из хранилища.
     * Используется в тестировании.
     */
    void removeAllMovies();
}
