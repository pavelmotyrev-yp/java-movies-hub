package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.Collection;
import java.util.Optional;

public interface MoviesStore {
    Collection<Movie> getMovies();

    Collection<Movie> getMoviesByYear(int year);

    Movie addMovie(Movie movie);

    Optional<Movie> getMovie(int id);

    Optional<Movie> removeMovie(int id);
}
