package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStoreImpl implements MoviesStore {
    private static int id;
    private Map<Integer, Movie> movies;

    public MoviesStoreImpl() {
        id = 0;
        this.movies = new HashMap<>();
    }

    public Collection<Movie> getMovies() {
        return movies.values();
    }

    public Collection<Movie> getMoviesByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.year() == year)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Movie addMovie(Movie movie) {
        int movieId = getNextId();
        Movie savedMovie = new Movie(movieId, movie.title(), movie.year());
        movies.put(movieId, savedMovie);
        return savedMovie;
    }

    public Optional<Movie> getMovie(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public Optional<Movie> removeMovie(int id) {
        return Optional.ofNullable(movies.remove(id));
    }

    private int getNextId() {
        id++;
        return id;
    }
}