package ru.practicum.moviehub.service;

import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;

import java.util.Collection;

public interface MoviesService {
    Collection<Movie> getMovies();

    Collection<Movie> getMoviesByYear(int year);

    Movie addMovie(MovieDTO movie);

    Movie getMovie(int id);

    void removeMovie(int id);
}
