package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.exception.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;
import ru.practicum.moviehub.service.MoviesService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    private static final int STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;
    private static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final String PARAM_YEAR = "year";
    private static final String PARAM_TITLE = "title";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";


    private MoviesService moviesService;
    private Gson gson;

    public MoviesHandler(MoviesService moviesService) {
        this.moviesService = moviesService;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            runController(ex);
        } catch (ValidationException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), List.of(e.getMessage()));
            sendJson(ex, STATUS_UNPROCESSABLE_ENTITY, gson.toJson(errorResponse));
        } catch (NotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), List.of(e.getMessage()));
            sendJson(ex, STATUS_NOT_FOUND, gson.toJson(errorResponse));
        } catch (UnsupportedMediaTypeException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),
                    List.of("Content-Type must be application/json"));
            sendJson(ex, STATUS_UNSUPPORTED_MEDIA_TYPE, gson.toJson(errorResponse));
        } catch (BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), List.of(e.getMessage()));
            sendJson(ex, STATUS_BAD_REQUEST, gson.toJson(errorResponse));
        } catch (JsonSyntaxException e) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid JSON", List.of(e.getMessage()));
            sendJson(ex, STATUS_BAD_REQUEST, gson.toJson(errorResponse));
        } catch (NumberFormatException e) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid path param value",
                    List.of("Invalid field value: " + PARAM_YEAR));
            sendJson(ex, STATUS_BAD_REQUEST, gson.toJson(errorResponse));
        }
    }

    private void runController(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        switch (method) {
            case METHOD_POST:
                runMethodPost(ex);
                break;
            case METHOD_DELETE:
                runMethodDelete(ex);
                break;
            case METHOD_GET:
                runMethodGet(ex);
                break;
            default:
                ErrorResponse errorResponse = new ErrorResponse("Method not allowed", List.of("Method: " + method));
                sendJson(ex, STATUS_METHOD_NOT_ALLOWED, gson.toJson(errorResponse));
        }
    }

    private void runMethodDelete(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String id = parseMoviesIdValue(path);
        int movieId = parseInt(id);
        moviesService.removeMovie(movieId);
        sendNoContent(ex);
    }

    private void runMethodPost(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst(CONTENT_TYPE);
        if (contentType == null || !contentType.contains("application/json")) {
            throw new UnsupportedMediaTypeException("Unsupported Media Type");
        }
        String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        MovieDTO movieDTO = gson.fromJson(requestBody, MovieDTO.class);
        Movie movie = moviesService.addMovie(movieDTO);
        sendJson(ex, STATUS_CREATED, gson.toJson(movie));
    }

    private void runMethodGet(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        if (query != null) {
            runMethodGetWithFilterByYear(ex, query);
        } else {
            String path = ex.getRequestURI().getPath();
            String stringId = parseMoviesIdValue(path);
            if (stringId.isEmpty()) {
                runMethodGetAllMovies(ex);
            } else {
                runMethodGetById(ex, stringId);
            }
        }
    }

    private void runMethodGetAllMovies(HttpExchange ex) throws IOException {
        Collection<Movie> movies = moviesService.getMovies();
        sendJson(ex, STATUS_OK, gson.toJson(movies));
    }

    private void runMethodGetById(HttpExchange ex, String stringId) throws IOException {
        int movieId = parseInt(stringId);
        Movie movie = moviesService.getMovie(movieId);
        sendJson(ex, STATUS_OK, gson.toJson(movie));
    }

    private void runMethodGetWithFilterByYear(HttpExchange ex, String query) throws IOException {
        Map<String, String> queryMap = parseQueryParam(query);
        String stringYear = queryMap.get(PARAM_YEAR);
        int year = parseInt(stringYear);
        Collection<Movie> movies = moviesService.getMoviesByYear(year);
        sendJson(ex, STATUS_OK, gson.toJson(movies));
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Value is not a number: " + value);
        }
    }
}