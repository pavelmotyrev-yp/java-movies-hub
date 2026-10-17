package ru.practicum.moviehub.service;

import ru.practicum.moviehub.exception.NotFoundException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDto;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.validator.Validator;

import java.util.Collection;
import java.util.Optional;

public class MoviesServiceImpl implements MoviesService {
    private final MoviesStore store;

    public MoviesServiceImpl(MoviesStore store) {
        this.store = store;
    }

    public Collection<Movie> getMovies() {
        return store.getMovies();
    }

    public Movie addMovie(MovieDto movie) {
        Validator.validateMovieTitle(movie.title());
        Validator.validateMovieYear(movie.year());
        return store.addMovie(movie);
    }

    public Movie getMovie(int id) {
        Optional<Movie> movieOptional = store.getMovie(id);
        if (movieOptional.isPresent()) {
            return movieOptional.get();
        } else {
            throw new NotFoundException("Movie not found by id: " + id);
        }
    }

    public void removeMovie(int id) {
        Optional<Movie> optionalMovie = store.removeMovie(id);
        if (optionalMovie.isEmpty()) {
            throw new NotFoundException("Movie not found by id: " + id);
        }
    }

    public Collection<Movie> getMoviesByYear(int year) {
        Validator.validateMovieYear(year);
        return store.getMoviesByYear(year);
    }
}
