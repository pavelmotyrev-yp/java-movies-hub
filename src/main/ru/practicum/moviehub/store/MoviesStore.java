package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;

import java.util.Collection;
import java.util.Optional;

public interface MoviesStore {
    Collection<Movie> getMovies();

    Collection<Movie> getMoviesByYear(int year);

    Movie addMovie(MovieDTO movie);

    Optional<Movie> getMovie(int id);

    Optional<Movie> removeMovie(int id);
}
